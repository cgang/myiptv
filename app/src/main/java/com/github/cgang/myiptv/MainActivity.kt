package com.github.cgang.myiptv

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * The base activity for playback.
 */
open class MainActivity : AppCompatActivity() {
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
        Log.d(TAG, "onTouchEvent($event)")
        val controlsLayout = findViewById<FrameLayout>(R.id.channel_container)
        if (controlsLayout.visibility == View.VISIBLE) {
            return if (event.action == MotionEvent.ACTION_UP) {
                hideControls()
                true
            } else {
                super.onTouchEvent(event)
            }
        }
        return if (event.action == MotionEvent.ACTION_UP) {
            controlsLayout.visibility = View.VISIBLE
            showControls(false)
            true
        } else {
            super.onTouchEvent(event)
        }
    }


    private fun showControls(useGroup: Boolean) {
        val frag = supportFragmentManager.findFragmentById(R.id.channel_container)
        if (frag is ControlFragment) {
            if (useGroup) {
                frag.useGroup(frag.getGroup())
            } else {
                frag.useGroup("") // disable group
            }
            supportFragmentManager.beginTransaction()
                .show(frag)
                .commit()
            frag.listView.requestFocus()
        }
    }

    fun hideControls() {
        Log.d(TAG, "Trying to hide controls")
        val frag = supportFragmentManager.findFragmentById(R.id.channel_container)
        val layout = findViewById<FrameLayout>(R.id.channel_container)
        if (layout.visibility == View.VISIBLE && frag != null) {
            supportFragmentManager.beginTransaction()
                .hide(frag)
                .commit()
            layout.visibility = View.GONE
            hideSystemUI()
        }
    }

    private fun switchList(forward: Boolean) {
        val frag = supportFragmentManager.findFragmentById(R.id.channel_container)
        if (frag is ControlFragment) {
            frag.switchGroup(forward)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate()")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        supportFragmentManager
            .beginTransaction()
            .add(R.id.channel_container, ControlFragment())
            .commit()
        // new Thread(new SelfUpdater(this)).start();
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

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        Log.d(TAG, "dispatchKeyEvent($event)")
        val controlsLayout = findViewById<FrameLayout>(R.id.channel_container)
        if (controlsLayout.visibility == View.VISIBLE) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_BACK -> {
                    if (event.action == KeyEvent.ACTION_UP) {
                        hideControls()
                    }
                    return true
                }

                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    if (event.action == KeyEvent.ACTION_UP) {
                        switchList(false)
                    }
                    return true
                }

                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    if (event.action == KeyEvent.ACTION_UP) {
                        switchList(true)
                    }
                    return true
                }
            }
            return super.dispatchKeyEvent(event)
        }

        return if (event.keyCode == KeyEvent.KEYCODE_DPAD_CENTER || event.keyCode == KeyEvent.KEYCODE_MENU) {
            if (event.action == KeyEvent.ACTION_UP) {
                controlsLayout.visibility = View.VISIBLE
                showControls(true)
            }
            true
        } else {
            super.dispatchKeyEvent(event)
        }
    }

    fun playDefault(channel: Channel?) {
        val frag = supportFragmentManager.findFragmentById(R.id.playback_fragment_root)
        if (frag is PlaybackFragment) {
            frag.playDefault(channel)
        }
    }

    fun play(channel: Channel) {
        val frag = supportFragmentManager.findFragmentById(R.id.playback_fragment_root)
        if (frag is PlaybackFragment) {
            frag.switchTo(channel)
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}
