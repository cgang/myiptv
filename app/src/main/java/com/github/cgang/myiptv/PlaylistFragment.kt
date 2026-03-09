package com.github.cgang.myiptv

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.ListFragment
import com.github.cgang.myiptv.xmltv.Program

class PlaylistFragment(model: PlaylistViewModel) : ListFragment() {
    private val viewModel = model
    private var isHandheldDevice = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = requireContext()
        val channelAdapter = PlaylistAdapter(context, R.layout.channel_item)
        super.setListAdapter(channelAdapter)

        // Detect device type for layout selection
        isHandheldDevice = !DeviceUtils.isTv(context)

        viewModel.getProgram().observe(this) {
            updateProgram(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        // Use different layout for handheld vs TV devices
        val layoutRes = if (isHandheldDevice) {
            R.layout.playlist_handheld
        } else {
            R.layout.playlist
        }

        val view = inflater.inflate(layoutRes, container, false)
        Log.i(TAG, "onCreateView() $view for ${if (isHandheldDevice) "handheld" else "TV"}")

        view.findViewById<ListView>(android.R.id.list)?.let {
            it.onItemSelectedListener = SelectListener(this)
        }

        // Only setup program list for TV layout (has EPG)
        if (!isHandheldDevice) {
            view.findViewById<ListView>(R.id.program_list)?.let {
                it.adapter = ProgramAdapter(requireContext())
                it.isEnabled = false
            }
        }

        // Setup action buttons for handheld devices
        if (isHandheldDevice) {
            view.findViewById<ImageButton>(R.id.settings_button)?.let { btn ->
                btn.visibility = View.VISIBLE
                btn.setOnClickListener {
                    (activity as? MainActivity)?.showConfig()
                }
            }

            view.findViewById<ImageButton>(R.id.exit_button)?.let { btn ->
                btn.setOnClickListener {
                    (activity as? MainActivity)?.hideControls()
                }
            }
        }

        return view
    }

    fun setPlaylist(playlist: Playlist) {
        setGroup(playlist.group)
        setChannels(playlist.channels)
        val offset = playlist.getSelected()
        if (offset >= 0) {
            Log.d(TAG, "using offset from playlist: $offset")
            listView.setSelection(offset)
        } else {
            Log.d(TAG, "offset not found for ${playlist.selected} in ${playlist.group}")
            // update program information
            val position = listView.selectedItemPosition
            viewModel.selectChannel(playlist.channels.getOrNull(position))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setGroup(group: String) {
        val groupTitle = view?.findViewById<TextView>(R.id.group_title)
        if (groupTitle != null) {
            if (group == "") {
                groupTitle.visibility = View.GONE
            } else {
                groupTitle.visibility = View.VISIBLE
                groupTitle.text = "< $group >"
            }
        }
    }

    private fun setChannels(channels: List<Channel>) {
        val adapter = listAdapter
        if (adapter is PlaylistAdapter) {
            adapter.setChannels(channels)
        }
    }

    override fun onListItemClick(lv: ListView, v: View, position: Int, id: Long) {
        val adapter = listAdapter
        if (adapter is PlaylistAdapter) {
            val channel = adapter.getChannel(position)
            viewModel.switchChannel(channel)
        }
    }

    fun onChannelSelected(position: Int) {
        val adapter = listAdapter
        if (adapter is PlaylistAdapter) {
            val channel = adapter.getChannel(position)
            // update selected channel for view model
            viewModel.selectChannel(channel)
        }
    }

    // update program information
    // Only used for TV layout (handheld doesn't show EPG)
    private fun updateProgram(program: Program?) {
        if (isHandheldDevice) {
            // Handheld layout doesn't have EPG views
            return
        }

        val name = program?.name ?: ""
        val list = program?.getRecent(10) ?: emptyList()

        view?.findViewById<TextView>(R.id.tvg_channel_name)?.let {
            it.text = name
        }
        view?.findViewById<ListView>(R.id.program_list)?.adapter.let {
            if (it is ProgramAdapter) {
                it.setList(list)
            }
        }
    }

    fun onNothingSelected() {
        updateProgram(null)
    }

    class SelectListener(private val frag: PlaylistFragment) : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            frag.onChannelSelected(position)
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            frag.onNothingSelected()
        }
    }

    companion object {
        private val TAG = PlaylistFragment::class.java.simpleName
    }
}
