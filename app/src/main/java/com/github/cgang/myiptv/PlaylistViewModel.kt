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
    private val channels = AtomicReference<List<Channel>>()

    // all groups
    private var groups = listOf<String>()
    private val playlist = MutableLiveData<Playlist>()
    private val tvgUrl = MutableLiveData<String>()
    private var currentGroup = ""

    // selected channel
    private var selectedChannel: Channel? = null

    private val playingChannel = MutableLiveData<Channel>()

    // all programs with channel name as key
    private val programMap = AtomicReference<Map<String, Program>>()
    private val program = MutableLiveData<Program?>()

    private val downloader = Downloader(application.applicationContext)

    init {
        downloader.register(this)
    }

    fun updatePlaylist() {
        var group = currentGroup
        if (group != ALL_CHANNELS_GROUP) {
            group = selectedChannel?.group ?: currentGroup
        }

        playlist.value = toPlaylist(group)
    }

    fun setGroup(group: String) {
        currentGroup = group
        playlist.value = toPlaylist(group)
    }

    fun switchGroup(useAllChannels: Boolean, step: Int) {
        if (groups.isEmpty()) {
            setGroup("")
            return
        } else if (groups.size == 1 && !useAllChannels) {
            setGroup(groups[0])
            return
        }
        var index = groups.indexOf(currentGroup)
        if (index >= 0 || useAllChannels) {
            index += step
        } else {
            index = 0
        }

        if (useAllChannels) {
            index = Math.floorMod(index, groups.size + 1)
            if (index == groups.size) {
                setGroup(ALL_CHANNELS_GROUP)
            } else {
                setGroup(groups[index])
            }
        } else {
            index = Math.floorMod(index, groups.size)
            setGroup(groups[index])
        }
    }

    private fun toPlaylist(group: String): Playlist {
        val channels = this.channels.get() ?: emptyList()
        val url = selectedChannel?.url ?: ""
        if (group == "") {
            return Playlist("", channels, url)
        } else if (group == ALL_CHANNELS_GROUP) {
            val name = application.resources.getString(R.string.all_channels)
            return Playlist(name, channels, url)
        }

        val result = mutableListOf<Channel>()
        for (ch in channels) {
            if (ch.group == group) {
                result.add(ch)
            }
        }
        return Playlist(group, result, url)
    }

    fun downloadPlaylist(url: String) {
        downloader.downloadPlaylist(url)
    }

    fun getPlayingChannel(): LiveData<Channel> {
        return playingChannel
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

    fun selectChannel(channel: Channel?) {
        this.selectedChannel = channel
        this.program.value = this.programMap.get()?.get(channel?.name)
    }

    override fun onChannels(tvgUrl: String?, channels: List<Channel>) {
        val newGroups = linkedSetOf<String>()
        for (ch in channels) {
            ch.group?.let { newGroups.add(it) }
        }

        synchronized(this) {
            this.channels.set(channels.toList())
            this.groups = newGroups.toList()
            if (currentGroup == "" && groups.isNotEmpty()) {
                currentGroup = groups[0]
            }
        }
        playlist.postValue(toPlaylist(currentGroup))
        tvgUrl?.let { this.tvgUrl.postValue(it) }
    }

    fun switchChannel(url: String, step: Int) {
        val channels = this.channels.get() ?: emptyList()
        val total = channels.size
        if (total <= 1) {
            return
        }

        var index = Playlist.indexOf(channels, url)
        if (index < 0 || index >= total) {
            return
        }

        index += step
        index = Math.floorMod(index, total)
        val channel = channels.getOrNull(index)
        this.selectChannel(channel)
        this.switchChannel(channel)
    }

    fun switchChannel(channel: Channel?) {
        if (channel != null) {
            this.playingChannel.value = channel
        }
    }

    override fun onPrograms(programs: Collection<Program>) {
        val channels = mutableMapOf<String, Channel>()
        this.channels.get()?.forEach { ch ->
            ch.tvgId?.let { channels[it] = ch }
            ch.tvgName?.let { channels[it] = ch }
            channels.putIfAbsent(ch.name, ch)
        }

        val programMap = mutableMapOf<String, Program>()
        for (program in programs) {
            val channel = channels[program.chan.id] ?: channels[program.chan.name]
            channel?.let {
                programMap[it.name] = program
            }
        }
        this.programMap.set(programMap)
    }

    companion object {
        const val ALL_CHANNELS_GROUP = "_ALL_CHANNELS_GROUP"
    }
}
