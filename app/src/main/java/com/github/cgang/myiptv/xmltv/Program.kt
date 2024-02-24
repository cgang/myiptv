package com.github.cgang.myiptv.xmltv

import java.time.LocalDateTime

data class Channel(
    val id: String,
    val name: String,
)

data class Programme(
    val channel: String,
    val start: LocalDateTime,
    val stop: LocalDateTime,
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
        val now = LocalDateTime.now()
        val result = mutableListOf<Programme>()
        for (item in items) {
            if (now.isAfter(item.stop)) {
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

