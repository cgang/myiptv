package com.github.cgang.myiptv

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.cgang.myiptv.xmltv.Program
import java.util.concurrent.atomic.AtomicReference

class PlaylistViewModel(
    private val application: Application
) : AndroidViewModel(application), Downloader.Listener {
    // all channels
    private var channels = AtomicReference<List<Channel>>()

    // all groups
    private var groups = listOf<String>()
    private val playlist = MutableLiveData<Playlist>()
    private val tvgUrl = MutableLiveData<String>()
    private var current = ""

    // all programs
    private val programs = AtomicReference<Map<String, Program>>()
    private val program = MutableLiveData<Program?>()

    private val downloader = Downloader(application.applicationContext)

    init {
        downloader.register(this)
    }

    fun resetGroup() {
        playlist.value = toPlaylist(current)
    }

    fun setGroup(group: String) {
        if (group != "") {
            current = group
        }
        playlist.value = toPlaylist(group)
    }

    fun switchGroup(step: Int) {
        if (groups.isEmpty()) {
            setGroup("")
            return
        } else if (groups.size == 1) {
            setGroup(groups[0])
            return
        }
        var index = groups.indexOf(current)
        if (index >= 0) {
            index += step
        } else {
            index = 0
        }

        index = Math.floorMod(index, groups.size)
        setGroup(groups[index])
    }

    private fun toPlaylist(group: String): Playlist {
        val channels = this.channels.get() ?: emptyList()
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

    fun downloadPlaylist(url: String) {
        downloader.downloadPlaylist(url)
    }

    fun getPlaylist(): LiveData<Playlist> {
        return playlist
    }

    fun getTvgUrl(): LiveData<String> {
        return tvgUrl
    }

    fun downloadEPG(url: String) {
        downloader.downloadEPG(url)
    }

    fun getProgram(): LiveData<Program?> {
        return program
    }

    fun setProgram(id: String?) {
        this.program.value = this.programs.get()?.get(id)
    }

    override fun onChannels(tvgUrl: String?, channels: List<Channel>) {
        val newGroups = linkedSetOf<String>()
        for (ch in channels) {
            ch.group?.let { newGroups.add(it) }
        }

        synchronized(this) {
            this.channels.set(channels.toList())
            this.groups = newGroups.toList()
            if (current == "" && groups.isNotEmpty()) {
                current = groups[0]
            }
        }
        playlist.postValue(toPlaylist(current))
        tvgUrl?.let { this.tvgUrl.postValue(it) }
    }


    private fun indexOf(url: String): Int {
        val channels = this.channels.get() ?: emptyList()
        for (idx in 0..<channels.size) {
            val ch = channels.getOrNull(idx)
            if (ch?.url == url) {
                return idx
            }
        }
        return -1
    }

    fun switchChannel(url: String, step: Int): Channel? {
        val channels = this.channels.get() ?: emptyList()
        val total = channels.size
        if (total <= 1) {
            return null
        }

        var index = indexOf(url)
        if (index < 0 || index >= total) {
            return null
        }

        index += step
        index = Math.floorMod(index, total)
        return channels.getOrNull(index)
    }

    override fun onPrograms(programs: Map<String, Program>) {
        this.programs.set(programs)
    }
}
