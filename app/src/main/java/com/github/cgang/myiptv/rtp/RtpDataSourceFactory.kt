package com.github.cgang.myiptv.rtp

import androidx.media3.datasource.DataSource
import androidx.media3.datasource.TransferListener

/**
 * Factory for creating RTP data sources
 */
class RtpDataSourceFactory(
    private val multicastInterface: String,
    private val multicastAddress: String,
    private val port: Int,
    private val transferListener: TransferListener? = null
) : DataSource.Factory {
    override fun createDataSource(): DataSource {
        val rtpDataSource = RtpDataSource(multicastInterface, multicastAddress, port)
        transferListener?.let { rtpDataSource.addTransferListener(it) }
        return rtpDataSource
    }
}