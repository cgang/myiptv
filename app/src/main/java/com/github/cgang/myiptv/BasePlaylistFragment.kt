package com.github.cgang.myiptv

import android.annotation.SuppressLint
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

/**
 * Base class for playlist fragments.
 * Provides common channel list functionality for both TV and handheld devices.
 */
abstract class BasePlaylistFragment() : ListFragment() {
    protected lateinit var channelAdapter: PlaylistAdapter
    protected abstract val viewModel: PlaylistViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = requireContext()
        channelAdapter = PlaylistAdapter(context, R.layout.channel_item)
        super.setListAdapter(channelAdapter)

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
        val view = inflater.inflate(getLayoutRes(), container, false)
        Log.i(TAG, "onCreateView() $view")

        view.findViewById<ListView>(android.R.id.list)?.let {
            it.onItemSelectedListener = SelectListener(this)
        }

        onViewInflated(view, savedInstanceState)
        return view
    }

    /**
     * Get the layout resource ID for this fragment.
     * Subclasses should return their specific layout.
     */
    protected abstract fun getLayoutRes(): Int

    /**
     * Called after view is inflated for additional setup.
     * Subclasses can override to initialize their specific views.
     */
    protected open fun onViewInflated(view: View, savedInstanceState: Bundle?) {
        // Subclasses can override for additional setup
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
    protected fun setGroup(group: String) {
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

    protected fun onChannelSelected(position: Int) {
        val adapter = listAdapter
        if (adapter is PlaylistAdapter) {
            val channel = adapter.getChannel(position)
            // update selected channel for view model
            viewModel.selectChannel(channel)
        }
    }

    /**
     * Update program information.
     * Subclasses implement their own display logic.
     */
    protected abstract fun updateProgram(program: Program?)

    protected fun onNothingSelected() {
        updateProgram(null)
    }

    protected class SelectListener(private val frag: BasePlaylistFragment) :
        AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            frag.onChannelSelected(position)
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            frag.onNothingSelected()
        }
    }

    companion object {
        protected val TAG = BasePlaylistFragment::class.java.simpleName
    }
}
