package com.github.cgang.myiptv

import java.io.BufferedReader
import java.io.IOException
import java.io.Reader
import java.util.regex.Pattern

class M3UParser {
    @Throws(IOException::class)
    fun parse(reader: Reader?): List<Channel> {
        return if (reader is BufferedReader) {
            parse(reader)
        } else {
            parse(BufferedReader(reader))
        }
    }

    @Throws(IOException::class)
    fun parse(reader: BufferedReader): List<Channel> {
        var line = reader.readLine()
        if (!line.startsWith(EXTM3U)) {
            throw IOException("Invalid header")
        }
        val channels = mutableListOf<Channel>()
        var channel: Channel? = null
        while (reader.readLine().also { line = it } != null) {
            line = line.trim { it <= ' ' }
            if (line.isEmpty()) continue
            if (line.startsWith("#")) {
                if (line.startsWith(EXTINF)) {
                    line = line.substring(EXTINF.length)
                    channel = parseTitle(line)
                }
            } else if (channel != null) {
                channel.url = line
                channels.add(channel)
                channel = null
            }
        }
        return channels
    }

    @Throws(IOException::class)
    private fun parseTitle(line: String): Channel {
        val idx = line.lastIndexOf(',')
        if (idx < 0) {
            throw IOException("Invalid EXTINF: $line")
        }
        val channel = Channel(line.substring(idx + 1).trim { it <= ' ' })
        val m = ATTR_PATTERN.matcher(line.substring(0, idx))
        while (m.find()) {
            channel[m.group(1)] = m.group(2)
        }
        return channel
    }

    companion object {
        const val EXTM3U = "#EXTM3U"
        const val EXTINF = "#EXTINF:"
        private val ATTR_PATTERN = Pattern.compile("([a-z-]+)=\"([^\"]+)\"")
    }
}
