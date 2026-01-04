package com.github.cgang.myiptv.rtp

import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.TransferListener

/**
 * Factory for creating RTP data sources
 */
@OptIn(UnstableApi::class)
class RtpDataSourceFactory (
    private val multicastInterface: String,
    private val uri: Uri,
    private val transferListener: TransferListener? = null
) : DataSource.Factory {
    override fun createDataSource(): DataSource {
        val rtpDataSource = RtpDataSource(multicastInterface, uri)
        transferListener?.let { rtpDataSource.addTransferListener(it) }
        return rtpDataSource
    }
}