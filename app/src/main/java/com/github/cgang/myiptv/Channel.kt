package com.github.cgang.myiptv

class Channel(val name: String) {
    var group: String? = null
    var url: String? = null

    operator fun set(key: String?, value: String?) {
        if ("group-title" == key && value != null) {
            group = value.trim { it <= ' ' }
        }
    }

    override fun toString(): String {
        return name
    }
}

class Playlist(val group: String, val channels: List<Channel>) {
    val default: Channel?
        get() = if (channels.isEmpty()) null else channels[0]
}
