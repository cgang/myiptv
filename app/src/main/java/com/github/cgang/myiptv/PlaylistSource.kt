package com.github.cgang.myiptv

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class PlaylistSource : Parcelable {
    // all channels
    private var channels = listOf<Channel>()

    // all groups
    private var groups = listOf<String>()

    // current group
    var current: String = ""


    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(channels.size)
        for (ch in channels) {
            dest.writeString(ch.name)
            dest.writeString(ch.url)
            dest.writeString(ch.group)
        }
        dest.writeString(current)
    }

    private fun readFromParcel(source: Parcel) {
        val count = source.readInt()
        val channels = mutableListOf<Channel>()

        for (i in 0 until count) {
            val name = source.readString()
            val channel = Channel(name!!)
            channel.group = source.readString()
            channel.url = source.readString()
            channels.add(channel)
        }

        val cur = source.readString()
        if (cur != null && cur != "") {
            this.current = cur
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    fun isEmpty(): Boolean {
        return channels.isEmpty()
    }

    fun getGroup(): String {
        if (groups.isEmpty()) {
            return ""
        } else if (current == "") {
            current = groups.get(0)
        }

        return current
    }

    fun switchGroup(forward: Boolean): String {
        if (groups.isEmpty()) {
            return ""
        } else if (groups.size == 1) {
            return groups[0]
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
        current = groups[index]
        return current
    }

    fun getChannels(): List<Channel> {
        val group = current
        if (group == "") {
            return channels
        }

        val result = mutableListOf<Channel>()
        for (ch in channels) {
            if (ch.group == group) {
                result.add(ch)
            }
        }
        return result
    }

    private fun update(newChannels: List<Channel>) {
        val newGroups = linkedSetOf<String>()
        for (ch in newChannels) {
            ch.group?.let { newGroups.add(it) }
        }
        synchronized(this) {
            this.channels = newChannels.toList()
            this.groups = newGroups.toList()
        }
    }

    fun download(listUrl: String): Boolean {
        return try {
            val url = URL(listUrl)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 500
            conn.inputStream.use { input ->
                val parser = M3UParser()
                update(parser.parse(InputStreamReader(input)))
            }
            true
        } catch (e: IOException) {
            Log.w(TAG, "Failed to download list: " + e.message)
            false
        }
    }

    companion object {
        private val TAG = PlaylistSource::class.java.simpleName

        @JvmField
        val CREATOR = object : Parcelable.Creator<PlaylistSource> {
            override fun createFromParcel(source: Parcel): PlaylistSource {
                val playlist = PlaylistSource()
                playlist.readFromParcel(source)
                return playlist
            }

            override fun newArray(size: Int): Array<PlaylistSource?> {
                return arrayOfNulls(size)
            }
        }
    }
}
