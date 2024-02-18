package com.github.cgang.myiptv

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.ListFragment
import java.io.IOException

class PlaylistFragment : ListFragment() {
    private var playlistUrl: String? = null
    private var playlistSource = PlaylistProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        reloadPlaylist()

        val context = requireContext()
        val channelAdapter = PlaylistAdapter(context, R.layout.list_item)
        super.setListAdapter(channelAdapter)
    }

    fun reloadPlaylist() {
        val activity = activity
        if (activity is MainActivity) {
            val listUrl = activity.preferences.getString(PLAYLIST_URL, DEFAULT_PLAYLIST_URL)
            if (listUrl == null || listUrl == playlistUrl) {
                return
            }

            playlistUrl = listUrl
            Thread {
                updatePlaylist()
            }.start()
        } else {
            Log.w(TAG, "activity not found")
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
        return view
    }

    fun switchGroup(forward: Boolean) {
        val group = playlistSource.switchGroup(forward)
        if (group == "") {
            Log.i(TAG, "Group not available")
            return
        }

        useGroup(group)
    }

    fun getGroup(): String {
        return playlistSource.getGroup()
    }

    @SuppressLint("SetTextI18n")
    fun useGroup(group: String) {
        val groupTitle = view?.findViewById<TextView>(R.id.group_title)
        if (groupTitle != null) {
            if (group == "") {
                groupTitle.visibility = View.GONE
            } else {
                groupTitle.visibility = View.VISIBLE
                groupTitle.text = "< $group >"
            }
        }

        val adapter = listAdapter
        if (adapter is PlaylistAdapter) {
            adapter.setChannels(playlistSource.getChannels())
        }
    }

    private fun updatePlaylist() {
        val listUrl = playlistUrl ?: DEFAULT_PLAYLIST_URL
        try {
            playlistSource.download(listUrl)
            Log.d(TAG, "playlist downloaded successfully")
        } catch (e: IOException) {
            Log.w(TAG, "Failed to download list: ${e}")
            return
        }

        val activity = activity
        val adapter = listAdapter
        if (activity is MainActivity && adapter is PlaylistAdapter) {
            activity.runOnUiThread(Runnable {
                adapter.setChannels(playlistSource.getChannels())
                activity.playDefault(adapter.default)
            })
        } else {
            Log.w(TAG, "Unable update channels since no activity is available.")
            return
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

    companion object {
        private val TAG = PlaylistFragment::class.java.simpleName
    }
}
