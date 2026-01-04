package com.github.cgang.myiptv.rtp

import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.TransferListener
import java.lang.IllegalStateException
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

/**
 * A DataSource that reads RTP multicast streams
 */
@UnstableApi
class RtpDataSource(
    private val multicastInterface: String,
) : DataSource {
    private var rtpTransport: RtpTransport? = null
    private var lastPacket: RtpPacket? = null
    private var uri: Uri? = null
    private val packetQueue = LinkedBlockingQueue<RtpPacket>(MAX_QUEUE_SIZE)

    companion object {
        private const val TAG = "RtpDataSource"
        private const val MAX_QUEUE_SIZE = 4096
    }

    @OptIn(UnstableApi::class)
    override fun open(dataSpec: DataSpec): Long {
        if (rtpTransport != null) {
            rtpTransport!!.stop()
            rtpTransport = null
        }

        uri = dataSpec.uri
        Log.d(TAG, "Opening RTP data source for $uri on interface $multicastInterface")
        rtpTransport = RtpTransport(packetQueue, multicastInterface, uri!!).apply {
            start()
        }

        Log.i(TAG, "RTP data source for $uri opened successfully")
        return C.LENGTH_UNSET.toLong()
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        val packet = if (lastPacket != null) {
            val last = lastPacket!!
            lastPacket = null
            last
        } else {
            packetQueue.poll(10, TimeUnit.MILLISECONDS) ?: return 0 // no packet available
        }

        val count = packet.read(buffer, offset, length)
        if (packet.offset < packet.limit) {
            lastPacket = packet
            return count
        }

        var off = offset + count
        var len = length - count
        var bytesRead = count

        while (len > 0) {
            val packet = packetQueue.poll() ?: break
            val count = packet.read(buffer, off, len)
            bytesRead += count
            off += count
            len -= count

            if (packet.offset < packet.limit) {
                lastPacket = packet
                break
            }
        }

        return bytesRead
    }

    override fun close() {
        rtpTransport?.stop()
        rtpTransport = null
        lastPacket = null
    }

    override fun addTransferListener(transferListener: TransferListener) {
        // Not implemented for this custom data source
        Log.d(TAG, "Transfer listener added (not implemented)")
    }

    override fun getUri(): Uri? {
        return uri
    }
}