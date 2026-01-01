package com.github.cgang.myiptv.rtp

import android.util.Log
import kotlinx.coroutines.*
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import java.util.Collections
import java.util.concurrent.LinkedBlockingQueue
import kotlin.coroutines.CoroutineContext

/**
 * Transport class for handling RTP over UDP multicast
 * Contains the main loop for receiving and processing RTP packets
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
    private val packetQueue = LinkedBlockingQueue<RtpPacket>(MAX_QUEUE_SIZE)

    // Use a sorted list to maintain buffered packets in sequence order
    private val sortedBuffer = mutableListOf<BufferedPacket>()

    companion object {
        private const val TAG = "RtpTransport"
        private const val MAX_QUEUE_SIZE = 128  // Limit the main packet queue size
        private const val MAX_BUFFER_SIZE = 64  // Maximum packets to buffer for reordering

        // Helper function to compare sequence numbers with wraparound handling
        // Returns -1 if seq1 < seq2, 0 if seq1 == seq2, 1 if seq1 > seq2, considering wraparound
        private fun compareSeq(seq1: Int, seq2: Int): Int {
            if (seq1 == seq2) {
                return 0
            }

            // Handle wraparound: if the difference is large, consider the possibility of wraparound
            val diff = (seq1 - seq2) and 0xFFFF  // Ensure we're working with 16-bit values
            // If the difference is between 1 and 0x7FFF (32767), then seq1 is higher
            // If the difference is between 0x8000 (32768) and 0xFFFF (65535), then seq1 is lower
            return if (diff in 1..0x7FFF) 1 else -1
        }

    }

    /**
     * Helper function to move the next expected packet from buffer to main queue
     * Returns true if a packet was moved, false otherwise
     */
    private fun moveNextPacket(nextSeq: Int): Boolean {
        // Short circuit: return false immediately if buffer is empty or first packet doesn't match
        if (sortedBuffer.isEmpty() || sortedBuffer[0].packet.sequence != nextSeq) {
            return false
        }

        val buffered = sortedBuffer.removeAt(0)  // Remove first packet (which has the expected sequence)

        // Add to queue, dropping if full
        if (!packetQueue.offer(buffered.packet)) {
            Log.w(TAG, "Main packet queue full, dropping buffered packet with sequence: ${buffered.packet.sequence}")
            return false  // Indicate failure due to queue being full
        } else {
            return true  // Indicate success
        }
    }

    init {
        Log.d(TAG, "Initializing RtpTransport for $multicastAddress:$port on interface $multicastInterface")
        val address = InetAddress.getByName(multicastAddress)
        multicastSocket = MulticastSocket(port).apply {
            // Try to get the network interface, but don't fail if it's not available
            val networkInterface = runCatching {
                java.net.NetworkInterface.getByName(multicastInterface)
            }.getOrNull()

            if (networkInterface != null) {
                Log.d(TAG, "Setting network interface to: $multicastInterface")
                setNetworkInterface(networkInterface)
            } else {
                Log.w(TAG, "Could not set network interface to: $multicastInterface, using default")
            }

            Log.d(TAG, "Joining multicast group: $multicastAddress")
            joinGroup(address)
            Log.d(TAG, "Successfully joined multicast group: $multicastAddress")
        }
    }

    /**
     * Starts the RTP transport (main loop)
     */
    fun start() {
        Log.d(TAG, "Starting RTP transport")
        try {
            // Read first packet to determine if it's RTP
            Log.d(TAG, "Reading first packet to determine stream type")
            val firstPacket = readPacket()
            Log.d(TAG, "First packet received, checking if it's RTP...")
            val (isRtp, error) = firstPacket.check()
            if (error != null) {
                Log.e(TAG, "Error checking first packet: $error")
                throw Exception(error)
            }

            val isRtpStream = isRtp
            Log.d(TAG, "Stream type determined: ${if (isRtpStream) "RTP" else "Raw"}")

            if (isRtpStream) {
                Log.d(TAG, "Stripping RTP header from first packet")
                firstPacket.stripRtp()
            }
            packetQueue.offer(firstPacket)

            // Start processing packets
            if (isRtpStream) {
                Log.d(TAG, "Starting RTP packet transfer")
                launch {
                    try {
                        transferRtp(firstPacket.nextSeq())
                    } catch (e: Exception) {
                        Log.d(TAG, "RTP packet transfer loop ended due to exception: ${e.message}")
                    }
                }
            } else {
                Log.d(TAG, "Starting raw packet transfer")
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
        val buffer = ByteArray(1500) // MTU size
        val packet = DatagramPacket(buffer, buffer.size)
        multicastSocket.receive(packet)

        return RtpPacket(
            data = packet.data.copyOfRange(0, packet.length),
            offset = 0,
            length = packet.length
        )
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
     * Data class to hold packet
     */
    private data class BufferedPacket(val packet: RtpPacket) : Comparable<BufferedPacket> {
        override fun compareTo(other: BufferedPacket): Int {
            // Handle sequence number wraparound when comparing
            return compareSeq(packet.sequence, other.packet.sequence)
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
            val seq = packet.sequence

            if (seq == nextSeq) {
                // Expected packet - put directly in queue

                // Add to queue, dropping if full
                if (!packetQueue.offer(packet)) {
                    Log.w(TAG, "Main packet queue full, dropping packet with sequence: $seq")
                } else {
                    // Update next expected sequence
                    nextSeq = packet.nextSeq()

                    // Check temporary buffer for any following packets and move them in sequence order
                    // Keep moving packets as long as the next expected sequence is available in the buffer
                    while (moveNextPacket(nextSeq)) {
                        nextSeq = (nextSeq + 1) and 0xFFFF  // Update to the next expected sequence, handling wraparound
                    }
                }
            } else if (compareSeq(nextSeq, seq) > 0) {
                // Packet has smaller seq than nextSeq - ignore this packet (it's in passed time)
            } else {
                // seq > nextSeq - out of order packet, add to temporary buffer

                // Use binary search to find the position of this sequence
                val bufferedPacket = BufferedPacket(packet)
                val index = Collections.binarySearch(sortedBuffer, bufferedPacket)

                if (index >= 0) {
                    // Packet with this sequence already exists, replace it (most recent packet wins)
                    sortedBuffer[index] = bufferedPacket
                } else {
                    // Packet doesn't exist, insert at the correct position to maintain sorted order
                    val insertionPoint = -index - 1  // Calculate insertion point from the negative return value
                    sortedBuffer.add(insertionPoint, bufferedPacket)
                }

                // Check if temporary buffer is full
                if (sortedBuffer.size >= MAX_BUFFER_SIZE) {  // Changed to >= since MAX_BUFFER_SIZE is the limit
                    // Buffer is full, treat missing packets as lost
                    // Process the first packet and then try to move subsequent packets
                    Log.w(TAG, "Buffer full with ${sortedBuffer.size} packets, treating missing packets as lost...")

                    // Take the first packet (lowest sequence) and enqueue it
                    val firstBufferedPacket = sortedBuffer.removeAt(0)  // Remove first (lowest sequence)
                    val packetToProcess = firstBufferedPacket.packet

                    // Add to queue, dropping if full
                    if (!packetQueue.offer(packetToProcess)) {
                        Log.w(TAG, "Main packet queue full, dropping packet with sequence: ${packetToProcess.sequence}")
                    } else {
                        // Update nextSeq to the sequence after the one we just processed
                        nextSeq = packetToProcess.nextSeq()

                        // Keep moving packets as long as the next expected sequence is available in the buffer
                        while (moveNextPacket(nextSeq)) {
                            nextSeq = (nextSeq + 1) and 0xFFFF  // Update to the next expected sequence, handling wraparound
                        }
                    }
                }
            }

            // Log buffer status periodically for debugging
            if (sortedBuffer.isNotEmpty() && sortedBuffer.size % 10 == 0) {  // Every 10 buffered packets
                val bufferSeqs = sortedBuffer.map { it.packet.sequence }
                Log.d(TAG, "Buffer status - size: ${sortedBuffer.size}, next expected: $nextSeq, buffer sequences: $bufferSeqs")
            }
        }
    }

    /**
     * Gets the next available packet with timeout
     */
    @Throws(InterruptedException::class)
    fun getNextPacket(timeoutMs: Long = 100): RtpPacket? {
        val packet = packetQueue.poll(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS)
        return packet
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
            Log.d(TAG, "Multicast socket closed")
        }
        // Clear the packet queue to free up any waiting threads
        packetQueue.clear()
        job.cancel()
        Log.d(TAG, "RTP transport stopped")
    }
}