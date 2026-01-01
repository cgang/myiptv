package com.github.cgang.myiptv.rtp

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.datasource.DefaultDataSource

/**
 * MediaSource for RTP multicast streams
 */
class RtpMediaSource(
    private val multicastInterface: String,
    private val mediaItem: MediaItem,
) {
    companion object {
        private const val TAG = "RtpMediaSource"
    }

    fun createMediaSource(): MediaSource {
        val uri = mediaItem.requestMetadata.mediaUri

        if (uri == null) {
            Log.e(TAG, "URI is null in media item")
            throw IllegalArgumentException("URI is null in media item")
        }

        Log.d(TAG, "Creating RTP media source for interface: $multicastInterface, URI: $uri")
        val rtpDataSourceFactory = RtpDataSourceFactory(
            multicastInterface,
            uri
        )

        // Use the RTP data source factory directly instead of wrapping it in DefaultDataSource
        // This ensures that RTP streams are handled by our custom data source
        val progressiveMediaSourceFactory = ProgressiveMediaSource.Factory(rtpDataSourceFactory)
        Log.d(TAG, "Progressive media source factory created")

        val mediaSource = progressiveMediaSourceFactory.createMediaSource(mediaItem)
        Log.d(TAG, "Media source created successfully")
        return mediaSource
    }
}