package com.github.cgang.myiptv.epg.hunan

import com.github.cgang.myiptv.xmltv.Program
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.io.InputStream

class HunanEpgParser {
    fun parse(input: InputStream): Collection<Program> {
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(input, "UTF-8")

        val channels = mutableListOf<Channel>()

        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            when (parser.name) {
                "i" -> channels.add(parseChannelItem(parser))
                else -> skip(parser)
            }
        }

        // Convert to the standard Program format used by the app
        return channels.map { it.toProgram() }
    }

    private fun parseChannelItem(parser: XmlPullParser): Channel {
        var id = ""
        var name = ""
        var cNo = ""
        var channelId = ""
        var currentPlaybill: CurrentPlaybill? = null

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            when (parser.name) {
                "id" -> id = readTextElement(parser, "id")
                "name" -> name = readTextElement(parser, "name")
                "arg_list" -> {
                    // Parse arg_list to get c_no and channel_id
                    val args = parseArgList(parser)
                    cNo = args["c_no"] ?: ""
                    channelId = args["channel_id"] ?: ""
                }
                "current_playbill" -> currentPlaybill = parseCurrentPlaybill(parser)
                else -> skip(parser)
            }
        }

        return Channel(id, name, cNo, channelId, currentPlaybill)
    }

    private fun parseArgList(parser: XmlPullParser): Map<String, String> {
        val args = mutableMapOf<String, String>()

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            // All elements inside arg_list are key-value pairs
            val key = parser.name
            val value = readTextElement(parser, key)
            args[key] = value
        }

        return args
    }

    private fun parseCurrentPlaybill(parser: XmlPullParser): CurrentPlaybill {
        var name = ""
        var playbillInfo: PlaybillInfo? = null

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            when (parser.name) {
                "name" -> name = readTextElement(parser, "name")
                "playbill_info" -> playbillInfo = parsePlaybillInfo(parser)
                else -> skip(parser)
            }
        }

        return if (playbillInfo != null) {
            CurrentPlaybill(name, playbillInfo)
        } else {
            // Create a default playbill info if not available
            CurrentPlaybill(name, PlaybillInfo("", "19700101", "000000", 0))
        }
    }

    private fun parsePlaybillInfo(parser: XmlPullParser): PlaybillInfo {
        var videoId = ""
        var day = ""
        var beginTime = ""
        var timeLen = 0

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            when (parser.name) {
                "video_id" -> videoId = readTextElement(parser, "video_id")
                "day" -> day = readTextElement(parser, "day")
                "begin_time" -> beginTime = readTextElement(parser, "begin_time")
                "time_len" -> timeLen = readTextElement(parser, "time_len").toIntOrNull() ?: 0
                else -> skip(parser)
            }
        }

        return PlaybillInfo(videoId, day, beginTime, timeLen)
    }

    private fun readTextElement(parser: XmlPullParser, name: String): String {
        parser.require(XmlPullParser.START_TAG, null, name)
        val text = readText(parser)
        parser.require(XmlPullParser.END_TAG, null, name)
        return text
    }

    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }

    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
}