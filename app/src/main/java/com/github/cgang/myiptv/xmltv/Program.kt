package com.github.cgang.myiptv.xmltv

import java.util.Date

data class Channel(
    val id: String,
    val name: String,
)

data class Programme(
    val channel: String,
    val start: Date,
    val stop: Date,
    val title: String,
    val description: String,
)

class Program(
    val chan: Channel,
    val items: MutableList<Programme>,
) {
    val name: String
        get() = chan.name

    fun getRecent(limit: Int): List<Programme> {
        val now = Date()
        val result = mutableListOf<Programme>()
        for (item in items) {
            if (now.after(item.stop)) {
                continue
            }

            result.add(item)
            if (result.size >= limit) {
                break
            }
        }
        return result
    }
}

