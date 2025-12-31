package com.github.cgang.myiptv

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.UnrecognizedInputFormatException
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.github.cgang.myiptv.rtp.RtpMediaSource
import com.github.cgang.myiptv.rtp.NetworkInterfaceUtils

open class PlaybackFragment :
    Fragment(R.layout.playback), Player.Listener {
    private var exoPlayer: ExoPlayer? = null
    var lastUrl: String? = null
    private var lastMimeType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate()")
        super.onCreate(savedInstanceState)
        activity?.getPreferences(Context.MODE_PRIVATE)?.let {
            lastUrl = it.getString(LAST_URL, null)
            lastMimeType = it.getString(LAST_MIME_TYPE, null)
            Log.i(TAG, "Loading last URL from preferences: $lastUrl")
        } ?: {
            Log.w(TAG, "Unable to get last URL from preferences.")
        }
    }

    @OptIn(UnstableApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        if (view == null) {
            Log.w(TAG, "player view not created")
            return null
        }

        val playerView = view.findViewById<PlayerView>(R.id.player_view)
        if (isTvAspectRatio()) {
            Log.d(TAG, "TV screen aspect ratio found")
            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
        }

        return view
    }

    // check aspect ratio
    private fun isTvAspectRatio(): Boolean {
        val dm = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getRealMetrics(dm)
        return dm.widthPixels > 0 && dm.widthPixels * 9 == dm.heightPixels * 16
    }

    @OptIn(markerClass = [UnstableApi::class])
    private fun initializePlayer(context: Context): ExoPlayer {
        Log.d(TAG, "Initializing player")
        val renderersFactory = DefaultRenderersFactory(context)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
        val bufMs = (activity as MainActivity).getBufferDuration()
        val exoPlayer = ExoPlayer.Builder(context, renderersFactory)
            .setTrackSelector(DefaultTrackSelector(context))
            .setLoadControl(createLoadControl(bufMs))
            .build()
        exoPlayer.addListener(this)
        exoPlayer.playWhenReady = true
        // Produces DataSource instances through which media data is loaded.
        return exoPlayer
    }

    @OptIn(markerClass = [UnstableApi::class])
    protected fun createLoadControl(bufMs: Int): DefaultLoadControl {
        val bufferMs = if (bufMs < MIN_BUFFER_DURATION) MIN_BUFFER_DURATION else if (bufMs > MAX_BUFFER_DURATION) MAX_BUFFER_DURATION else bufMs

        return DefaultLoadControl.Builder()
            .setBufferDurationsMs(bufferMs, MAX_BUFFER_DURATION, bufferMs / 2, bufferMs)
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()
    }

    @OptIn(markerClass = [UnstableApi::class])
    override fun onPlayerError(error: PlaybackException) {
        Log.w(TAG, "Error occurs while playing $lastUrl", error)
        var message = error.message
        val cause = error.cause
        if (cause is UnrecognizedInputFormatException) {
            message = cause.message
        } else if (cause != null) {
            message += "\nCaused by $cause"
        }
        AlertDialog.Builder(context)
            .setTitle(R.string.play_error)
            .setMessage(message)
            .setNegativeButton(android.R.string.cancel, null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    @OptIn(markerClass = [UnstableApi::class])
    fun playDefault(channel: Channel?): Boolean {
        if (channel == null || isPlaying() || !lastUrl.isNullOrEmpty()) {
            return false
        }
        preparePlay(channel.url, channel.mimeType)
        return true
    }

    private fun isPlaying(): Boolean {
        return exoPlayer?.isPlaying ?: false
    }

    @OptIn(markerClass = [UnstableApi::class])
    fun switchTo(channel: Channel) {
        if (lastUrl != null && lastUrl == channel.url) {
            return
        }
        if (isPlaying()) {
            exoPlayer?.stop()
        }
        preparePlay(channel.url, channel.mimeType)
    }

    @OptIn(markerClass = [UnstableApi::class])
    private fun preparePlay(url: String?, mimeType: String?) {
        if (url.isNullOrEmpty()) {
            Log.w(TAG, "null or empty URL")
            return
        }

        try {
            val context = context ?: return
            val item = MediaItem.Builder().setUri(url).setMimeType(mimeType).build()

            // Check if this is an RTP multicast URL
            if (url.startsWith("rtp://") || url.startsWith("udp://")) {
                // Extract multicast address and port from URL
                val multicastInfo = extractMulticastInfo(url)
                if (multicastInfo != null) {
                    val (interfaceName, address, port) = multicastInfo
                    val rtpMediaSource = RtpMediaSource(interfaceName, address, port, context)
                    exoPlayer?.setMediaSource(rtpMediaSource.createMediaSource(), 0) // Start from beginning
                } else {
                    // Throw an error if we can't extract multicast information for RTP/UDP URLs
                    Log.e(TAG, "Unable to extract multicast information from URL: $url")
                    throw IllegalArgumentException("Invalid RTP/UDP multicast URL format: $url")
                }
            } else {
                // For non-RTP URLs, use default behavior
                exoPlayer?.setMediaItem(item)
            }

            Log.i(TAG, "Change last URL to $url")
            exoPlayer?.prepare()
            lastUrl = url
            lastMimeType = mimeType
        } catch (e: IllegalArgumentException) {
            // Handle RTP/UDP URL format errors specifically
            Log.e(TAG, "RTP/UDP URL format error for " + url + ": " + e.message)
            // Show an error dialog to the user
            context?.let { ctx ->
                androidx.appcompat.app.AlertDialog.Builder(ctx)
                    .setTitle(R.string.play_error)
                    .setMessage("Invalid RTP/UDP multicast URL format: ${e.message}")
                    .setNegativeButton(android.R.string.cancel, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Unable to play " + url + ": " + e.message)
        }
    }

    override fun onStart() {
        Log.d(TAG, "onStart()")
        super.onStart()

        val context = context
        if (context != null) {
            exoPlayer = initializePlayer(context)
            val playerView = view?.findViewById<PlayerView>(R.id.player_view)
            playerView?.player = exoPlayer
        }

        if (!lastUrl.isNullOrEmpty()) {
            Log.i(TAG, "Trying to play last URL: $lastUrl")
            preparePlay(lastUrl, lastMimeType)
        }
    }

    @OptIn(markerClass = [UnstableApi::class])
    override fun onPause() {
        Log.d(TAG, "onPause()")
        exoPlayer?.playWhenReady = false
        super.onPause()
    }

    @OptIn(markerClass = [UnstableApi::class])
    override fun onResume() {
        Log.d(TAG, "onResume()")
        super.onResume()
        exoPlayer?.playWhenReady = true
    }

    @OptIn(markerClass = [UnstableApi::class])
    override fun onStop() {
        Log.d(TAG, "onStop()")
        exoPlayer?.release()
        exoPlayer = null

        Log.i(TAG, "Saving last URL: $lastUrl")
        val activity = activity
        activity?.getPreferences(Context.MODE_PRIVATE)?.edit()
            ?.putString(LAST_URL, lastUrl)?.apply()
        super.onStop()
    }

    /**
     * Extracts multicast interface, address, and port from RTP/UDP URL
     * Expected format: rtp://interface@address:port or udp://interface@address:port
     * Or: rtp://address:port or udp://address:port (interface will be determined from settings or automatically)
     */
    private fun extractMulticastInfo(url: String): Triple<String, String, Int>? {
        try {
            // Handle both rtp:// and udp:// schemes
            val cleanUrl = url.substringAfter("://")

            // Check if interface is specified (format: interface@address:port)
            val atParts = cleanUrl.split("@", limit = 2)
            val interfaceName = if (atParts.size > 1) {
                atParts[0] // Interface name before @
            } else {
                // Get interface from settings, fallback to automatic detection
                val activity = activity as? MainActivity
                val settingInterface = activity?.getMulticastInterface() ?: "auto"

                if (settingInterface != "auto") {
                    settingInterface // Use interface from settings
                } else {
                    // Try to determine the interface automatically using NetworkInterfaceUtils
                    val context = context ?: return null
                    NetworkInterfaceUtils.getMulticastInterface(context) ?: "eth0"
                }
            }

            val addressPortPart = if (atParts.size > 1) atParts[1] else atParts[0]
            val colonParts = addressPortPart.split(":", limit = 2)

            if (colonParts.size != 2) {
                Log.e(TAG, "Invalid address:port format in URL: $url")
                return null
            }

            val address = colonParts[0]
            val port = colonParts[1].toInt()

            return Triple(interfaceName, address, port)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing multicast URL: $url", e)
            return null
        }
    }

    companion object {
        val TAG = PlaybackFragment::class.java.simpleName
        const val LAST_URL = "LastPlayingUrl"
        const val LAST_MIME_TYPE = "LastMimeType"
        const val MIN_BUFFER_DURATION = 100
        const val MAX_BUFFER_DURATION = 5000
    }
}
