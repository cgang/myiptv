package com.github.cgang.myiptv

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.ListFragment
import com.github.cgang.myiptv.xmltv.Program

class PlaylistFragment(model: PlaylistViewModel) : ListFragment() {
    private val viewModel = model

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = requireContext()
        val channelAdapter = PlaylistAdapter(context, R.layout.channel_item)
        super.setListAdapter(channelAdapter)

        viewModel.getProgram().observe(this) {
            updateProgram(it)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.i(TAG, "onAttach(), mHost=$host")
    }

    override fun onDetach() {
        Log.i(TAG, "onDetach()")
        super.onDetach()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.playlist, container, false)
        Log.i(TAG, "onCreateView() $view")

        view.findViewById<ListView>(android.R.id.list)?.let {
            it.onItemSelectedListener = SelectListener(this)
        }
        view.findViewById<ListView>(R.id.program_list)?.let {
            it.adapter = ProgramAdapter(requireContext())
            it.isEnabled = false
        }
        return view
    }

    fun setPlaylist(playlist: Playlist) {
        setGroup(playlist.group)
        setChannels(playlist.channels)
        // update program information
        onChannelSelected(listView.selectedItemPosition)
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
        val activity = activity
        if (adapter is PlaylistAdapter && activity is MainActivity) {
            val channel = adapter.getChannel(position)
            if (channel != null) {
                Log.i(TAG, "Channel selected " + channel.name)
                activity.play(channel)
            }
        }
    }

    fun onChannelSelected(position: Int) {
        val adapter = listAdapter
        if (adapter is PlaylistAdapter) {
            val channel = adapter.getChannel(position)
            if (channel != null) {
                Log.d(TAG, "Channel selected ${channel.name}")
                viewModel.setProgram(channel.id)
            } else {
                viewModel.setProgram("")
            }
        }
    }

    private fun updateProgram(program: Program?) {
        val name = program?.name ?: ""
        val list = program?.getRecent(10) ?: emptyList()

        Log.d(TAG, "set program for ${name}")
        view?.findViewById<TextView>(R.id.channel_name)?.let {
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

    class SelectListener(val frag: PlaylistFragment) : AdapterView.OnItemSelectedListener {
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
