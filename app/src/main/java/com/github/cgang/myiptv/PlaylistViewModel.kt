package com.github.cgang.myiptv

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class PlaylistViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    // all channels
    private var channels = listOf<Channel>()

    // all groups
    private var groups = listOf<String>()

    private val playlist = MutableLiveData<Playlist>()

    private var current = ""

    fun resetGroup() {
        playlist.value = toPlaylist(current)
    }

    fun setGroup(group: String) {
        if (group != "") {
            current = group
        }
        playlist.value = toPlaylist(group)
    }

    fun switchGroup(forward: Boolean) {
        if (groups.isEmpty()) {
            setGroup("")
            return
        } else if (groups.size == 1) {
            setGroup(groups[0])
            return
        }

        var index = groups.indexOf(current)
        if (index >= 0) {
            if (forward) {
                index++
            } else {
                index--
            }
        } else {
            index = 0
        }

        index = Math.floorMod(index, groups.size)
        setGroup(groups[index])
    }

    private fun toPlaylist(group: String): Playlist {
        if (group == "") {
            return Playlist("", channels)
        }

        val result = mutableListOf<Channel>()
        for (ch in channels) {
            if (ch.group == group) {
                result.add(ch)
            }
        }
        return Playlist(group, result)
    }

    fun getPlaylist(): LiveData<Playlist> {
        return playlist
    }

    private fun update(newChannels: List<Channel>) {
        val newGroups = linkedSetOf<String>()
        for (ch in newChannels) {
            ch.group?.let { newGroups.add(it) }
        }


        synchronized(this) {
            this.channels = newChannels.toList()
            this.groups = newGroups.toList()
            if (current == "" && !groups.isEmpty()) {
                current = groups[0]
            }
        }
        playlist.postValue(toPlaylist(current))
    }

    @Throws(IOException::class)
    fun download(listUrl: String) {
        val url = URL(listUrl)
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 500
        conn.inputStream.use { input ->
            val parser = M3UParser()
            update(parser.parse(InputStreamReader(input)))
        }
    }

    fun loadPlaylist(listUrl: String) {
        Thread {
            try {
                download(listUrl)
                Log.d(TAG, "playlist downloaded successfully")
            } catch (e: IOException) {
                Log.w(TAG, "Failed to download list: ${e}")
            }
        }.start()
    }

    companion object {
        private val TAG = PlaylistViewModel::class.java.simpleName
    }
}
