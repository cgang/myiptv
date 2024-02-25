package com.github.cgang.myiptv

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.github.cgang.myiptv.xmltv.Program
import java.util.concurrent.atomic.AtomicReference

class PlaylistViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel(), PlaylistListener {
    // all channels
    private var channels = listOf<Channel>()

    // all groups
    private var groups = listOf<String>()
    private val playlist = MutableLiveData<Playlist>()
    private var current = ""

    // all programs
    private val programs = AtomicReference<Map<String, Program>>()
    private val program = MutableLiveData<Program?>()

    init {
        Downloader.register(this)
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

    fun getProgram(): LiveData<Program?> {
        return program
    }

    fun setProgram(id: String?) {
        this.program.value = this.programs.get()?.get(id)
    }

    override fun onChannels(channels: List<Channel>) {
        val newGroups = linkedSetOf<String>()
        for (ch in channels) {
            ch.group?.let { newGroups.add(it) }
        }

        synchronized(this) {
            this.channels = channels.toList()
            this.groups = newGroups.toList()
            if (current == "" && groups.isNotEmpty()) {
                current = groups[0]
            }
        }
        playlist.postValue(toPlaylist(current))
    }

    override fun onPrograms(programs: Map<String, Program>) {
        this.programs.set(programs)
    }
}
