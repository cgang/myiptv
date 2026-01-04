package com.github.cgang.myiptv.rtp

import android.util.Log
import kotlinx.coroutines.*
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import java.util.Collections
import java.util.concurrent.BlockingQueue
import kotlin.coroutines.CoroutineContext

/**
 * Transport class for handling RTP over UDP multicast
 * Contains the main loop for receiving and processing RTP packets
 */
class RtpTransport(
    private val packetQueue: BlockingQueue<RtpPacket>,
    private val multicastInterface: String,
    private val multicastAddress: String,
    port: Int
) : CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    private val multicastSocket: MulticastSocket

    // Use a sorted list to maintain buffered packets in sequence order
    private val reordering = mutableListOf<RtpPacket>()

    companion object {
        private const val TAG = "RtpTransport"
        private const val MAX_BUFFER_SIZE = 256  // Maximum packets to buffer for reordering
    }

    init {
        Log.d(TAG, "Initializing RtpTransport for $multicastAddress:$port on interface $multicastInterface")
        val address = InetAddress.getByName(multicastAddress)
        multicastSocket = MulticastSocket(port).apply {
            // Try to get the network interface, but don't fail if it's not available
            runCatching {
                java.net.NetworkInterface.getByName(multicastInterface)
            }.onSuccess {
                Log.d(TAG, "Setting network interface to: $multicastInterface")
                networkInterface = it
            }.onFailure {
                Log.w(TAG, "Could not set network interface to $multicastInterface: $it")
            }

            Log.d(TAG, "Joining multicast group: $multicastAddress")
            joinGroup(address)
        }
    }

    /**
     * Starts the RTP transport (main loop)
     */
    fun start() {
        Log.d(TAG, "Starting RTP transport")
        try {
            // Read first packet to determine if it's RTP
            val packet = readPacket()
            val isRtp = packet.check()

            if (isRtp) {
                packet.stripRtp()
                packetQueue.offer(packet)

                launch {
                    try {
                        transferRtp(packet.nextSeq())
                    } catch (e: Exception) {
                        Log.d(TAG, "RTP packet transfer loop ended due to exception: ${e.message}")
                    }
                }
            } else {
                packetQueue.offer(packet)
                launch {
                    try {
                        transferRaw()
                    } catch (e: Exception) {
                        Log.d(TAG, "Raw packet transfer loop ended due to exception: ${e.message}")
                    }
                }
            }

            Log.d(TAG, "RTP transport started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting RTP transport: ${e.message}", e)
            throw e
        }
    }

    /**
     * Reads a packet from the multicast socket
     */
    private fun readPacket(): RtpPacket {
        val buffer = ByteArray(RtpPacket.MTU_SIZE)
        val packet = DatagramPacket(buffer, buffer.size)
        multicastSocket.receive(packet)

        return RtpPacket(buffer, packet.length)
    }

    /**
     * Transfers raw packets (non-RTP)
     */
    private suspend fun transferRaw() {
        Log.d(TAG, "Starting raw packet transfer loop")
        while (true) {
            val packet = readPacket()
            packetQueue.offer(packet)
        }
    }

    /**
     * Transfers RTP packets with sequence handling
     */
    private suspend fun transferRtp(initialNextSeq: Int) {
        var nextSeq = initialNextSeq
        Log.d(TAG, "Starting RTP packet transfer loop with nextSeq: $nextSeq")

        while (true) {
            val packet = readPacket()

            packet.stripRtp()
            val cmp = RtpPacket.compareSeq(nextSeq, packet.sequence)
            if (cmp == 0) { // equal
                nextSeq = enqueuePacket(packet)
            } else if (cmp > 0) {
                // Packet has smaller seq than nextSeq - ignore this packet (it's in passed time)
            } else {
                // seq > nextSeq - out of order packet, add to temporary buffer
                val index = reordering.binarySearch(packet)
                if (index >= 0) {
                    reordering[index] = packet
                } else {
                    val insertionPoint = -index - 1
                    reordering.add(insertionPoint, packet)
                }

                if (reordering.size >= MAX_BUFFER_SIZE) {
                    Log.w(TAG, "Buffer full with ${reordering.size} packets, treating missing packets as lost...")

                    val buffered = reordering.removeAt(0)  // Remove first (lowest sequence)
                    nextSeq = enqueuePacket(buffered)
                }
            }
        }
    }

    private fun enqueuePacket(packet: RtpPacket): Int {
        var nextSeq = packet.nextSeq()
        if (!packetQueue.offer(packet)) {
            Log.w(TAG, "Packet queue full, dropping packet: ${packet.sequence}")
            packetQueue.clear()
            reordering.clear()
            return nextSeq
        }

        while (reordering.isNotEmpty()) {
            val buffered = reordering.get(0)
            if (buffered.sequence != nextSeq) {
                break
            }

            reordering.removeAt(0)
            nextSeq = buffered.nextSeq()

            if (!packetQueue.offer(buffered)) {
                Log.w(TAG, "Packet queue full, dropping packet: ${buffered.sequence}")
                packetQueue.clear()
                reordering.clear()
                break
            }
        }

        return nextSeq
    }

    /**
     * Stops the RTP transport
     */
    fun stop() {
        Log.d(TAG, "Stopping RTP transport")
        runCatching {
            val address = InetAddress.getByName(multicastAddress)
            Log.d(TAG, "Leaving multicast group: $multicastAddress")
            multicastSocket.leaveGroup(address)
            multicastSocket.close()
        }
        job.cancel()
        Log.d(TAG, "RTP transport stopped")
    }
}