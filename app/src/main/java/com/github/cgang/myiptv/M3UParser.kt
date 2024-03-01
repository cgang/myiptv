package com.github.cgang.myiptv

import java.io.BufferedReader
import java.io.IOException
import java.io.Reader
import java.util.regex.Pattern

class M3UParser {
    val channels = mutableListOf<Channel>()
    var tvgUrl: String? = null

    @Throws(IOException::class)
    fun parse(reader: Reader?) {
        if (reader is BufferedReader) {
            parse(reader)
        } else {
            parse(BufferedReader(reader))
        }
    }

    @Throws(IOException::class)
    fun parse(reader: BufferedReader) {
        var line = reader.readLine()
        if (!line.startsWith(EXTM3U)) {
            throw IOException("Invalid header")
        }
        var channel: Channel? = null
        while (reader.readLine().also { line = it } != null) {
            line = line.trim()
            if (line.startsWith(EXTM3U)) {
                parseHeader(line.substring(EXTM3U.length))
                channel = null
            } else if (line.startsWith(EXTINF)) {
                channel = parseTitle(line.substring(EXTINF.length))
            } else if (channel != null) {
                channel.url = line
                channels.add(channel)
                channel = null
            }
        }
    }

    private fun parseHeader(line: String) {
        val m = ATTR_PATTERN.matcher(line)
        while (m.find()) {
            when (m.group(1)) {
                "x-tvg-url", "url-tvg" -> {
                    this.tvgUrl = m.group(2)
                }
            }
        }
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
