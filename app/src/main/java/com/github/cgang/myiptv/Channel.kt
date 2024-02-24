package com.github.cgang.myiptv

class Channel(val name: String) {
    var group: String? = null
    var url: String? = null
    var logoUrl: String? = null // logo url
    var id: String? = null // channel id

    operator fun set(key: String?, value: String?) {
        when (key) {
            "tvg-id" -> id = value?.trim()
            "tvg-logo" -> logoUrl = value?.trim()
            "group-title" -> group = value?.trim()
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
