package com.github.cgang.myiptv.rtp

import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.TransferListener
import java.io.IOException

/**
 * A DataSource that reads RTP multicast streams
 */
@UnstableApi
class RtpDataSource(
    private val multicastInterface: String,
    private val uri: Uri
) : DataSource {
    private var rtpTransport: RtpTransport? = null
    private var lastPacket: RtpPacket? = null

    companion object {
        private const val TAG = "RtpDataSource"
    }

    @OptIn(UnstableApi::class)
    override fun open(dataSpec: DataSpec): Long {
        val address = uri.host
        val port = uri.port

        if (address.isNullOrEmpty()) {
            Log.e(TAG, "Invalid address in URI: $uri")
            throw IOException("Invalid address in URI: $uri")
        }

        if (port == -1) {
            Log.e(TAG, "Invalid port in URI: $uri")
            throw IOException("Invalid port in URI: $uri")
        }

        Log.d(TAG, "Opening RTP data source for $uri on interface $multicastInterface")
        // Create the RTP transport if it doesn't exist
        if (rtpTransport == null) {
            try {
                rtpTransport = RtpTransport(multicastInterface, address, port)
                Log.d(TAG, "RTP transport created successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create RTP transport: ${e.message}", e)
                throw IOException("Failed to create RTP transport: ${e.message}", e)
            }
        }

        // Start the RTP transport
        try {
            rtpTransport?.start()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start RTP transport: ${e.message}", e)
            throw IOException("Failed to start RTP transport: ${e.message}", e)
        }

        Log.i(TAG, "RTP data source for $uri opened successfully")
        return C.LENGTH_UNSET.toLong()
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        Log.i(TAG, "Reading RTP data with offset $offset, length $length")
        // If we have last packet, return the remaining data
        if (lastPacket != null) {
            val packet = lastPacket!!
            val remaining = packet.length - packet.offset
            val count = minOf(length, remaining)
            System.arraycopy(packet.data, packet.offset, buffer, offset, count)
            packet.offset += count
            if (packet.offset >= packet.length) {
                lastPacket = null
            }
            return count
        }

        // Poll a new packet from queue
        val transport = rtpTransport ?: throw IOException("RTP transport not initialized")
        val packet = transport.getNextPacket(100) // Use timeout to avoid blocking ExoPlayer thread

        if (packet != null) {
            val remaining = packet.length - packet.offset
            val count = minOf(length, remaining)
            System.arraycopy(packet.data, packet.offset, buffer, offset, count)
            packet.offset += count
            // If there's more data in the packet, keep it for next read
            if (packet.offset < packet.length) {
                lastPacket = packet
            }

            return count
        } else {
            // Throw exception otherwise
            throw IOException("No RTP packet received within timeout")
        }
    }

    override fun close() {
        Log.i(TAG, "Closing RTP data source")
        rtpTransport?.stop()
        rtpTransport = null
        lastPacket = null
    }

    override fun addTransferListener(transferListener: TransferListener) {
        // Not implemented for this custom data source
        Log.d(TAG, "Transfer listener added (not implemented)")
    }

    override fun getUri(): Uri? = null
}