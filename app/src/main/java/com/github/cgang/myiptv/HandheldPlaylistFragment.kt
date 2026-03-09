package com.github.cgang.myiptv

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.fragment.app.activityViewModels
import com.github.cgang.myiptv.xmltv.Program

/**
 * Playlist fragment for handheld devices (phones/tablets).
 * Shows channel list only in a compact sidebar with settings and exit buttons.
 */
class HandheldPlaylistFragment : BasePlaylistFragment() {

    override val viewModel: PlaylistViewModel by activityViewModels()

    override fun getLayoutRes(): Int = R.layout.playlist_handheld

    override fun onViewInflated(view: View, savedInstanceState: Bundle?) {
        super.onViewInflated(view, savedInstanceState)

        // Setup settings button
        view.findViewById<ImageButton>(R.id.settings_button)?.let { btn ->
            btn.setOnClickListener {
                (activity as? MainActivity)?.showConfig()
            }
        }

        // Setup exit/close button - exit the app
        view.findViewById<ImageButton>(R.id.exit_button)?.let { btn ->
            btn.setOnClickListener {
                activity?.finish()
            }
        }
    }

    override fun updateProgram(program: Program?) {
        // Handheld layout doesn't show EPG information
        // This method is intentionally left empty
    }

    companion object {
        fun create(): HandheldPlaylistFragment {
            return HandheldPlaylistFragment()
        }
    }
}
