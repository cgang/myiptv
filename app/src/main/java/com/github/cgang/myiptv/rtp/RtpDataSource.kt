package com.github.cgang.myiptv.rtp

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.util.Assertions
import androidx.media3.common.util.Util
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.TransferListener
import androidx.media3.exoplayer.source.*
import androidx.media3.exoplayer.upstream.*
import kotlinx.coroutines.*
import java.io.EOFException
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext

/**
 * A DataSource that reads RTP multicast streams
 */
class RtpDataSource(
    private val multicastInterface: String,
    private val multicastAddress: String,
    private val port: Int
) : DataSource {
    private var rtpTransport: RtpTransport? = null
    private var currentPacket: RtpPacket? = null
    private var packetOffset = 0
    private var isReading = AtomicBoolean(false)
    private var coroutineScope: CoroutineScope? = null


    override fun open(dataSpec: DataSpec): Long {
        // Create the RTP transport if it doesn't exist
        if (rtpTransport == null) {
            try {
                rtpTransport = RtpTransport(multicastInterface, multicastAddress, port)
            } catch (e: Exception) {
                throw IOException("Failed to create RTP transport: ${e.message}", e)
            }
        }

        coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        // Start the RTP transport synchronously
        val result = rtpTransport?.start()
        if (result?.isFailure == true) {
            throw IOException("Failed to start RTP transport: ${result.exceptionOrNull()?.message}")
        }

        isReading.set(false)
        return C.LENGTH_UNSET.toLong()
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        if (currentPacket == null) {
            // Try to get the next packet with timeout
            val transport = rtpTransport ?: throw IOException("RTP transport not initialized")
            try {
                currentPacket = transport.getNextPacket(10) // 10ms timeout
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt() // Restore interrupt status
                throw IOException("Interrupted while waiting for RTP packet", e)
            }
        }

        val packet = currentPacket
        if (packet == null) {
            return if (isReading.get()) {
                // Still reading but no data available yet
                0
            } else {
                // No more data
                C.RESULT_END_OF_INPUT
            }
        }

        // Calculate how much data is left in the current packet
        val remainingInPacket = packet.length - packet.offset - packetOffset
        if (remainingInPacket <= 0) {
            // Move to the next packet
            currentPacket = null
            packetOffset = 0
            return read(buffer, offset, length)
        }

        // Copy data from the packet to the buffer
        val bytesToCopy = minOf(length, remainingInPacket)
        System.arraycopy(
            packet.data,
            packet.offset + packetOffset,
            buffer,
            offset,
            bytesToCopy
        )

        packetOffset += bytesToCopy

        // If we've read all data from this packet, move to the next one
        if (packetOffset >= remainingInPacket) {
            currentPacket = null
            packetOffset = 0
        }

        isReading.set(true)
        return bytesToCopy
    }

    override fun close() {
        rtpTransport?.stop()
        rtpTransport = null
        coroutineScope?.cancel()
        coroutineScope = null
        currentPacket = null
        packetOffset = 0
        isReading.set(false)
    }

    override fun addTransferListener(transferListener: TransferListener) {
        // Not implemented for this custom data source
    }

    override fun getUri(): Uri? = null
}