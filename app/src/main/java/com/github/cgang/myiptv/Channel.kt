package com.github.cgang.myiptv

class Channel(val name: String) {
    var group: String? = null
    var url: String? = null
    var logoUrl: String? = null // logo url
    var id: String? = null // channel id
    var mimeType: String? = null // mime type

    operator fun set(key: String?, value: String?) {
        val text = value?.trim() ?: return
        when (key) {
            "tvg-id" -> id = text
            "tvg-logo" -> logoUrl = text
            "group-title" -> group = text
            "mime-type" -> mimeType = text
        }
    }

    override fun toString(): String {
        return name
    }
}
