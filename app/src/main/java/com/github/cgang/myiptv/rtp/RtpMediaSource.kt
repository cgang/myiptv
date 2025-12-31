package com.github.cgang.myiptv.rtp

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.datasource.DefaultDataSource

/**
 * MediaSource for RTP multicast streams
 */
class RtpMediaSource(
    private val multicastInterface: String,
    private val multicastAddress: String,
    private val port: Int,
    private val context: Context
) {
    fun createMediaSource(): MediaSource {
        val rtpDataSourceFactory = RtpDataSourceFactory(
            multicastInterface,
            multicastAddress,
            port
        )

        val defaultDataSourceFactory = DefaultDataSource.Factory(
            context,
            rtpDataSourceFactory
        )

        // Create a progressive media source using our RTP data source
        val mediaItem = MediaItem.fromUri(
            Uri.parse("rtp://${multicastAddress}:${port}")
        )

        val progressiveMediaSourceFactory = ProgressiveMediaSource.Factory(defaultDataSourceFactory)
        return progressiveMediaSourceFactory.createMediaSource(mediaItem)
    }
}