package com.github.cgang.myiptv

import android.os.Bundle
import android.view.View
import android.widget.ListView
import androidx.fragment.app.activityViewModels
import com.github.cgang.myiptv.xmltv.Program

/**
 * Playlist fragment for TV devices.
 * Shows channel list with EPG (Electronic Program Guide) information.
 */
class TvPlaylistFragment : BasePlaylistFragment() {

    override val viewModel: PlaylistViewModel by activityViewModels()

    override fun getLayoutRes(): Int = R.layout.playlist

    override fun onViewInflated(view: View, savedInstanceState: Bundle?) {
        super.onViewInflated(view, savedInstanceState)

        // Setup EPG program list for TV
        view.findViewById<ListView>(R.id.program_list)?.let {
            it.adapter = ProgramAdapter(requireContext())
            it.isEnabled = false
        }
    }

    override fun updateProgram(program: Program?) {
        val name = program?.name ?: ""
        val list = program?.getRecent(10) ?: emptyList()

        view?.findViewById<android.widget.TextView>(R.id.tvg_channel_name)?.let {
            it.text = name
        }
        view?.findViewById<ListView>(R.id.program_list)?.adapter.let {
            if (it is ProgramAdapter) {
                it.setList(list)
            }
        }
    }

    companion object {
        fun create(): TvPlaylistFragment {
            return TvPlaylistFragment()
        }
    }
}
