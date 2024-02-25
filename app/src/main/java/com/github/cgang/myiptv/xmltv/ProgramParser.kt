package com.github.cgang.myiptv.xmltv

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.io.InputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class ProgramParser {
    fun parse(input: InputStream): Map<String, Program> {
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(input, "UTF-8")

        val channels = mutableListOf<Channel>()
        val programmes = mutableListOf<Programme>()
        val now = LocalDateTime.now()
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            when (parser.name) {
                "tv" -> Unit
                "channel" -> channels.add(parseChannel(parser))
                "programme" -> {
                    val programme = parseProgramme(parser)
                    if (programme.stop.isAfter(now)) { // skip past programme
                        programmes.add(programme)
                    }
                }
                else -> skip(parser)
            }
        }

        val result = mutableMapOf<String, Program>()
        for (channel in channels) {
            result.put(channel.id, Program(channel, mutableListOf()))
        }

        for (item in programmes) {
            result[item.channel]?.items?.add(item)
        }
        return result
    }

    private fun parseChannel(parser: XmlPullParser): Channel {
        val id = parser.getAttributeValue(null, "id")
        var displayName = ""

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            when (parser.name) {
                "display-name" -> displayName = readTextElement(parser, "display-name")
                else -> skip(parser)
            }
        }
        return Channel(id, displayName)
    }

    private fun parseProgramme(parser: XmlPullParser): Programme {
        val start = parser.getAttributeValue(null, "start")
        val stop = parser.getAttributeValue(null, "stop")
        val channel = parser.getAttributeValue(null, "channel")
        var title = ""
        var description = ""

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "title" -> title = readTextElement(parser, "title")
                "desc" -> description = readTextElement(parser, "desc")
                else -> skip(parser)
            }
        }

        try {
            val startTime = LocalDateTime.parse(start, dateTimeFormatter)
            val stopTime = LocalDateTime.parse(stop, dateTimeFormatter)
            return Programme(channel, startTime, stopTime, title, description)
        } catch (e: DateTimeParseException) {
            throw IOException("date time parsing failed", e)
        }
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

    companion object {
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss Z")
    }
}