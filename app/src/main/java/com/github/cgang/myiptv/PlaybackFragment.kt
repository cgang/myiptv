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
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.source.UnrecognizedInputFormatException
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

open class PlaybackFragment :
    Fragment(R.layout.playback), Player.Listener {
    private lateinit var exoPlayer: ExoPlayer
    private var dataSourceFactory: DataSource.Factory? = null
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        Log.i(TAG, "onCreateView(), returns $view")
        view?.let {
            initializePlayer(view)
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
    private fun initializePlayer(rootView: View) {
        Log.d(TAG, "Initializing player")
        var context = context
        if (context == null) {
            Log.w(TAG, "null context")
            context = rootView.context
        }
        val renderersFactory = DefaultRenderersFactory(context!!)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
        exoPlayer = ExoPlayer.Builder(context, renderersFactory)
            .setTrackSelector(DefaultTrackSelector(context))
            .setLoadControl(createLoadControl())
            .build()
        exoPlayer.addListener(this)
        exoPlayer.playWhenReady = true
        val playerView = rootView.findViewById<PlayerView>(R.id.player_view)
        if (hasAspectRatio(16, 9)) {
            Log.d(TAG, "TV screen aspect ratio found")
            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
        }
        playerView.player = exoPlayer
        // Produces DataSource instances through which media data is loaded.
        dataSourceFactory = DefaultDataSource.Factory(context)
        // Util.getUserAgent(context, "SimpleTV")
    }

    @OptIn(markerClass = [UnstableApi::class])
    protected fun createLoadControl(): DefaultLoadControl {
        return DefaultLoadControl.Builder()
            .setBufferDurationsMs(500, 5000, 100, 100)
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
        if (channel == null || exoPlayer.isPlaying || hasLastUrl()) {
            return false
        }
        preparePlay(channel.url)
        return true
    }

    @OptIn(markerClass = [UnstableApi::class])
    fun switchTo(channel: Channel) {
        if (lastUrl != null && lastUrl == channel.url) {
            return
        }
        if (exoPlayer.isPlaying) {
            exoPlayer.stop()
        }
        preparePlay(channel.url)
    }

    @OptIn(markerClass = [UnstableApi::class])
    private fun preparePlay(url: String?) {
        try {
            // This is the MediaSource representing the media to be played.
            val videoSource: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory!!)
                .createMediaSource(MediaItem.fromUri(url!!))
            // Prepare the player with the source.
            exoPlayer.setMediaSource(videoSource)
            Log.i(TAG, "Change last URL to $url")
            lastUrl = url
            exoPlayer.prepare()
        } catch (e: Exception) {
            Log.w(TAG, "Unable to play " + url + ": " + e.message)
        }
    }

    @OptIn(markerClass = [UnstableApi::class])
    override fun onDestroy() {
        Log.d(TAG, "onDestroy()")
        exoPlayer.release()
        dataSourceFactory = null
        super.onDestroy()
    }

    override fun onStart() {
        Log.d(TAG, "onStart()")
        super.onStart()
        if (hasLastUrl()) {
            Log.i(TAG, "Trying to play last URL: $lastUrl")
            preparePlay(lastUrl)
        }
        (requireActivity() as MainActivity).hideControls()
    }

    private fun hasLastUrl(): Boolean {
        return lastUrl != null && !lastUrl!!.isEmpty()
    }

    @OptIn(markerClass = [UnstableApi::class])
    override fun onPause() {
        Log.d(TAG, "onPause()")
        exoPlayer.playWhenReady = false
        super.onPause()
    }

    @OptIn(markerClass = [UnstableApi::class])
    override fun onResume() {
        Log.d(TAG, "onResume()")
        super.onResume()
        exoPlayer.playWhenReady = true
    }

    @OptIn(markerClass = [UnstableApi::class])
    override fun onStop() {
        Log.d(TAG, "onStop()")
        exoPlayer.stop()
        Log.i(TAG, "Saving last URL: $lastUrl")
        val activity = activity
        activity?.getPreferences(Context.MODE_PRIVATE)?.edit()
            ?.putString(LAST_URL, lastUrl)?.apply()
        super.onStop()
    }

    companion object {
        val TAG = PlaybackFragment::class.java.simpleName
    }
}
