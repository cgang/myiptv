package com.github.cgang.myiptv

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.github.cgang.myiptv.xmltv.Program

/**
 * The base activity for playback.
 */
open class MainActivity : AppCompatActivity() {
    private lateinit var preferences: SharedPreferences
    private lateinit var changeSettings: ActivityResultLauncher<Intent>
    private val viewModel: PlaylistViewModel by viewModels()
    private var lastPlaylistUrl: String? = null
    private var lastEpgUrl: String? = null
    private var programInfoExpired: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate()")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        supportFragmentManager
            .beginTransaction()
            .add(R.id.playlist_fragment, PlaylistFragment(viewModel))
            .add(R.id.program_info_fragment, ProgramInfoFragment(this))
            .commit()

        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        changeSettings =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                onPreferenceChanged()
            }

        viewModel.getPlayingChannel().observe(this) {
            play(it)
        }

        viewModel.getPlaylist().observe(this) {
            updatePlaylist(it)
        }
        viewModel.getTvgUrl().observe(this) {
            updateTvgUrl(it)
        }
        viewModel.getProgram().observe(this) {
            updateProgramInfo(it)
        }

        onPreferenceChanged()
    }

    fun getBufferDuration(): Int {
        val text = preferences.getString(
            BUFFER_DURATION,
            resources.getString(R.string.default_buffer_duration)
        )
        return text?.toInt() ?: 0
    }

    private fun hideSystemUI() {
        val decorView = window.decorView
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private fun showSystemUI() {
        val decorView = window.decorView
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, decorView).show(WindowInsetsCompat.Type.systemBars())
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val layout = findFrameLayout(R.id.playlist_fragment)
        if (layout.visibility == View.VISIBLE) {
            return if (event.action == MotionEvent.ACTION_UP) {
                hideControls()
                true
            } else {
                super.onTouchEvent(event)
            }
        }

        return if (event.action == MotionEvent.ACTION_UP) {
            viewModel.setGroup("") // disable group
            layout.visibility = View.VISIBLE
            showControls()
            true
        } else {
            super.onTouchEvent(event)
        }
    }

    private fun showFragment(frag: Fragment) {
        supportFragmentManager.beginTransaction()
            .show(frag)
            .commit()
    }

    private fun hideFragment(frag: Fragment) {
        supportFragmentManager.beginTransaction()
            .hide(frag)
            .commit()
    }

    private fun showControls() {
        playlistFrag()?.let {
            showFragment(it)
            it.listView.requestFocus()
        }
    }

    fun hideControls() {
        Log.d(TAG, "Trying to hide controls")
        val layout = findFrameLayout(R.id.playlist_fragment)
        playlistFrag()?.let {
            if (layout.isVisible) {
                hideFragment(it)
                layout.visibility = View.GONE
                hideSystemUI()
            }
        }
    }

    private fun playlistFrag(): PlaylistFragment? {
        val frag = supportFragmentManager.findFragmentById(R.id.playlist_fragment)
        return if (frag is PlaylistFragment) frag else null
    }

    private fun playbackFrag(): PlaybackFragment? {
        val frag = supportFragmentManager.findFragmentById(R.id.playback_fragment_root)
        return if (frag is PlaybackFragment) frag else null
    }

    private fun programInfoFrag(): ProgramInfoFragment? {
        val frag = supportFragmentManager.findFragmentById(R.id.program_info_fragment)
        return if (frag is ProgramInfoFragment) frag else null
    }

    private fun findFrameLayout(resId: Int): FrameLayout {
        return findViewById(resId)
    }

    private fun showProgramInfo(frag: ProgramInfoFragment) {
        if (findFrameLayout(R.id.playlist_fragment).isVisible) {
            return
        }

        val layout = findFrameLayout(R.id.program_info_fragment)
        if (layout.isVisible) {
            return
        }

        programInfoExpired = System.currentTimeMillis() + PROGRAM_INFO_TTL
        showFragment(frag)
        layout.visibility = View.VISIBLE

        Handler(mainLooper).postDelayed({
            hideProgramInfo()
        }, PROGRAM_INFO_TTL)
    }

    private fun hideProgramInfo() {
        if (System.currentTimeMillis() < programInfoExpired) {
            return
        }

        val layout = findFrameLayout(R.id.program_info_fragment)
        programInfoFrag()?.let {
            if (layout.isVisible) {
                hideFragment(it)
                layout.visibility = View.GONE
            }
        }
    }

    private fun showConfig() {
        val intent = Intent(this, SettingsActivity::class.java)
        changeSettings.launch(intent)
    }

    private fun onPreferenceChanged() {
        val defaultPlaylistUrl = resources.getString(R.string.default_playlist_url)
        preferences.getString(PLAYLIST_URL, defaultPlaylistUrl)?.let {
            if (lastPlaylistUrl != it) {
                viewModel.downloadPlaylist(it)
                lastPlaylistUrl = it
            }
        }

        preferences.getString(EPG_URL, null)?.let {
            if (lastEpgUrl != it) {
                viewModel.downloadEPG(it)
                lastEpgUrl = it
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        Log.d(TAG, "onWindowFocusChanged($hasFocus)")
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        } else {
            showSystemUI()
        }
    }

    private fun useAllChannels(): Boolean {
        return preferences.getBoolean(ENABLE_ALL_CHANNELS, false)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_CHANNEL_UP, KeyEvent.KEYCODE_CHANNEL_DOWN -> {
                return true
            }

            else -> super.onKeyDown(keyCode, event)
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            showConfig()
            return true
        }

        val layout = findFrameLayout(R.id.playlist_fragment)
        if (layout.isVisible) {
            return when (keyCode) {
                KeyEvent.KEYCODE_BACK -> {
                    hideControls()
                    return true
                }

                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    viewModel.switchGroup(useAllChannels(), PREV)
                    return true
                }

                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    viewModel.switchGroup(useAllChannels(), NEXT)
                    return true
                }

                else -> super.onKeyUp(keyCode, event)
            }
        }

        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_CHANNEL_UP -> {
                switchChannel(NEXT)
                return true
            }

            KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_CHANNEL_DOWN -> {
                switchChannel(PREV)
                return true
            }

            KeyEvent.KEYCODE_DPAD_CENTER -> {
                viewModel.updatePlaylist()
                layout.visibility = View.VISIBLE
                showControls()
                return true
            }

            else -> super.onKeyUp(keyCode, event)
        }
    }

    private fun updateTvgUrl(url: String) {
        if (preferences.getBoolean(PREFER_PLAYLIST_EPG, true)) {
            if (lastEpgUrl != url) {
                viewModel.downloadEPG(url)
                lastEpgUrl = url
            }
        }
    }

    private fun updatePlaylist(playlist: Playlist) {
        playlistFrag()?.setPlaylist(playlist)

        playlist.default?.let {
            playDefault(it)
        }
    }

    private fun playDefault(channel: Channel) {
        if (playbackFrag()?.playDefault(channel) == true) {
            hideControls()
        }
    }

    private fun play(channel: Channel) {
        playbackFrag()?.let {
            it.switchTo(channel)
            hideControls()
        }
    }

    private fun switchChannel(step: Int) {
        playbackFrag()?.lastUrl?.let {
            viewModel.switchChannel(it, step)
        }
    }

    private fun updateProgramInfo(program: Program?) {
        if (program == null) {
            return
        }

        programInfoFrag()?.let {
            if (it.setProgram(program)) {
                showProgramInfo(it)
            }
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        const val PLAYLIST_URL = "PlaylistUrl"
        const val EPG_URL = "EPGUrl"
        const val BUFFER_DURATION = "BufferDuration"
        const val PREFER_PLAYLIST_EPG = "PreferPlaylistEPG"
        const val ENABLE_ALL_CHANNELS = "EnableAllChannelsGroup"
        const val PREV = -1
        const val NEXT = 1
        const val PROGRAM_INFO_TTL = 5 * 1000L // milliseconds
    }
}
