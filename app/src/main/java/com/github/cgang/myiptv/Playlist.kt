package com.github.cgang.myiptv

class Playlist(val group: String, val channels: List<Channel>, val selected: String) {
    fun getSelected(): Int {
        return indexOf(channels, selected)
    }

    val default: Channel?
        get() = channels.getOrNull(0)

    companion object{
        fun indexOf(channels: List<Channel>, url: String): Int {
            for (idx in channels.indices) {
                val ch = channels.getOrNull(idx)
                if (ch?.url == url) {
                    return idx
                }
            }
            return -1
        }

    }
}
