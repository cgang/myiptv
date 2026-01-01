package com.github.cgang.myiptv.smil

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.io.InputStream
import java.io.StringReader

/**
 * Interface for creating XmlPullParser instances to allow for testability
 */
interface XmlPullParserFactoryProvider {
    fun createParser(): XmlPullParser
}

/**
 * Default implementation that creates actual XmlPullParser instances
 */
class DefaultXmlPullParserFactoryProvider : XmlPullParserFactoryProvider {
    override fun createParser(): XmlPullParser {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        return factory.newPullParser()
    }
}

/**
 * Simple SMIL parser for handling SMIL URLs in M3U playlists
 */
class SmilUrlParser(private val parserFactory: XmlPullParserFactoryProvider = DefaultXmlPullParserFactoryProvider()) {

    /**
     * Parse a SMIL document from a string and extract video URLs
     */
    @Throws(SmilUrlParseException::class)
    fun parseVideoUrls(smilContent: String): List<String> {
        return try {
            val parser = parserFactory.createParser()
            parser.setInput(StringReader(smilContent))
            parseDocument(parser)
        } catch (e: XmlPullParserException) {
            throw SmilUrlParseException("Failed to parse SMIL document: ${e.message}", e)
        } catch (e: IOException) {
            throw SmilUrlParseException("Failed to read SMIL document: ${e.message}", e)
        }
    }

    /**
     * Parse a SMIL document from an InputStream and extract video URLs
     */
    @Throws(SmilUrlParseException::class)
    fun parseVideoUrls(inputStream: InputStream): List<String> {
        return try {
            val parser = parserFactory.createParser()
            parser.setInput(inputStream, null)
            parseDocument(parser)
        } catch (e: XmlPullParserException) {
            throw SmilUrlParseException("Failed to parse SMIL document: ${e.message}", e)
        } catch (e: IOException) {
            throw SmilUrlParseException("Failed to read SMIL document: ${e.message}", e)
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun parseDocument(parser: XmlPullParser): List<String> {
        val videoUrls = mutableListOf<String>()

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name?.lowercase()) {
                        "video" -> {
                            val src = parser.getAttributeValue(null, "src")
                            if (!src.isNullOrEmpty()) {
                                videoUrls.add(src)
                            }
                        }
                        // Handle switch elements which might contain alternative versions
                        "switch" -> {
                            val switchVideos = parseSwitch(parser)
                            videoUrls.addAll(switchVideos)
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return videoUrls
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun parseSwitch(parser: XmlPullParser): List<String> {
        val videoUrls = mutableListOf<String>()
        var firstVideoSrc: String? = null

        var eventType = parser.eventType
        while (!(eventType == XmlPullParser.END_TAG && parser.name?.lowercase() == "switch")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name?.lowercase()) {
                        "video" -> {
                            val src = parser.getAttributeValue(null, "src")
                            if (!src.isNullOrEmpty()) {
                                // For switch elements, we typically want the first (preferred) video
                                if (firstVideoSrc == null) {
                                    firstVideoSrc = src
                                }
                                // But we'll collect all URLs in case we need them
                                videoUrls.add(src)
                            }
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        // Return just the first video if we found one, otherwise all videos
        return if (firstVideoSrc != null) listOf(firstVideoSrc) else videoUrls
    }
}
