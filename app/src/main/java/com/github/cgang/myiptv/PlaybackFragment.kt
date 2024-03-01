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

open class PlaybackFragment :
    Fragment(R.layout.playback), Player.Listener {
    private var exoPlayer: ExoPlayer? = null
    var lastUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate()")
        super.onCreate(savedInstanceState)
        val activity = activity
        if (activity != null) {
            lastUrl = activity
                .getPreferences(Context.MODE_PRIVATE)
                .getString(LAST_URL, null)
            Log.i(TAG, "Loading last URL from preferences: $lastUrl")
        } else {
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
        if (hasAspectRatio(16, 9)) {
            Log.d(TAG, "TV screen aspect ratio found")
            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
        }

        return view
    }

    // check aspect ratio
    private fun hasAspectRatio(width: Int, height: Int): Boolean {
        val dm = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getRealMetrics(dm)
        return dm.widthPixels > 0 && dm.widthPixels * height == dm.heightPixels * width
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
        val bufferMs = if (bufMs < minBufferMs) minBufferMs else if (bufMs > maxBufferMs) maxBufferMs else bufMs

        return DefaultLoadControl.Builder()
            .setBufferDurationsMs(bufferMs, maxBufferMs, bufferMs / 2, bufferMs)
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
        preparePlay(channel.url)
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
        preparePlay(channel.url)
    }

    @OptIn(markerClass = [UnstableApi::class])
    private fun preparePlay(url: String?) {
        if (url.isNullOrEmpty()) {
            Log.w(TAG, "null or empty URL")
            return
        }

        try {
            exoPlayer?.setMediaItem(MediaItem.fromUri(url))
            Log.i(TAG, "Change last URL to $url")
            exoPlayer?.prepare()
            lastUrl = url
        } catch (e: Exception) {
            Log.w(TAG, "Unable to play " + url + ": " + e.message)
        }
    }

    @OptIn(markerClass = [UnstableApi::class])
    override fun onDestroy() {
        Log.d(TAG, "onDestroy()")
        super.onDestroy()
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
            preparePlay(lastUrl)
        }
        (requireActivity() as MainActivity).hideControls()
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

    companion object {
        val TAG = PlaybackFragment::class.java.simpleName
        val minBufferMs = 100
        val maxBufferMs = 5000
    }
}
