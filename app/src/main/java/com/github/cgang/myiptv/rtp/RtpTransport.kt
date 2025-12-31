package com.github.cgang.myiptv.rtp

import kotlinx.coroutines.*
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import java.util.concurrent.LinkedBlockingQueue
import kotlin.coroutines.CoroutineContext

/**
 * Transport class for handling RTP over UDP multicast
 */
class RtpTransport(
    private val multicastInterface: String,
    private val multicastAddress: String,
    private val port: Int
) : CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    private val multicastSocket: MulticastSocket
    private val packetQueue = LinkedBlockingQueue<RtpPacket>()
    private var isRunning = false
    private var isRtpStream = false

    init {
        val address = InetAddress.getByName(multicastAddress)
        multicastSocket = MulticastSocket(port).apply {
            // Try to get the network interface, but don't fail if it's not available
            val networkInterface = runCatching {
                java.net.NetworkInterface.getByName(multicastInterface)
            }.getOrNull()

            if (networkInterface != null) {
                setNetworkInterface(networkInterface)
            }

            joinGroup(address)
        }
    }

    /**
     * Starts the RTP transport
     */
    suspend fun start(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            isRunning = true

            // Read first packet to determine if it's RTP
            val firstPacket = readPacket()
            if (firstPacket != null) {
                val (isRtp, error) = firstPacket.check()
                if (error != null) {
                    return@withContext Result.failure(Exception(error))
                }

                isRtpStream = isRtp
                if (isRtpStream) {
                    firstPacket.stripRtp()
                }
                packetQueue.offer(firstPacket)

                // Start processing packets
                if (isRtpStream) {
                    launch { transferRtp() }
                } else {
                    launch { transferRaw() }
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Reads a packet from the multicast socket
     */
    private fun readPacket(): RtpPacket? {
        val socket = multicastSocket ?: return null
        if (!isRunning) return null

        try {
            val buffer = ByteArray(1500) // MTU size
            val packet = DatagramPacket(buffer, buffer.size)
            socket.receive(packet)

            return RtpPacket(
                data = packet.data.copyOfRange(0, packet.length),
                offset = 0,
                length = packet.length
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Transfers raw packets (non-RTP)
     */
    private suspend fun transferRaw() {
        while (isRunning) {
            val packet = readPacket()
            if (packet != null) {
                packetQueue.offer(packet)
            } else {
                break
            }
        }
    }

    /**
     * Transfers RTP packets with sequence handling
     */
    private suspend fun transferRtp() {
        var nextSeq = 0
        val buffer = mutableMapOf<Int, RtpPacket>()
        val maxBufferSize = 8 // Reduced buffer size since disorder packets are rare

        while (isRunning) {
            val packet = readPacket()
            if (packet == null) {
                break
            }

            packet.stripRtp()
            val seq = packet.sequence

            if (seq == nextSeq) {
                // Expected packet
                nextSeq = packet.nextSeq()
                packetQueue.offer(packet)

                // Process any buffered packets in sequence
                while (buffer.containsKey(nextSeq)) {
                    val pkt = buffer[nextSeq] ?: break
                    buffer.remove(nextSeq)
                    nextSeq = pkt.nextSeq()
                    packetQueue.offer(pkt)
                }
            } else if (seq > nextSeq && (seq - nextSeq) < 100) {  // Only buffer packets within a reasonable range
                // Future packet - buffer it if there's space
                if (buffer.size < maxBufferSize) {
                    buffer[seq] = packet
                }
                // If buffer is full or sequence is too far ahead, drop the packet
            }
            // If seq < nextSeq, it's an old/duplicate packet, so we drop it
        }
    }

    /**
     * Gets the next available packet with timeout
     */
    @Throws(InterruptedException::class)
    fun getNextPacket(timeoutMs: Long = 10): RtpPacket? {
        return packetQueue.poll(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS)
    }

    /**
     * Gets the next available packet without timeout (non-blocking)
     */
    fun getNextPacket(): RtpPacket? {
        return packetQueue.poll()
    }

    /**
     * Checks if there are packets available
     */
    fun hasPackets(): Boolean {
        return packetQueue.isNotEmpty()
    }

    /**
     * Stops the RTP transport
     */
    fun stop() {
        isRunning = false
        runCatching {
            val address = InetAddress.getByName(multicastAddress)
            multicastSocket.leaveGroup(address)
            multicastSocket.close()
        }
        job.cancel()
    }
}