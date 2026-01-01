package com.github.cgang.myiptv.smil

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SmilUrlParserTest {

    @Test
    fun `parse simple SMIL with video URLs`() {
        val smilContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <smil>
                <body>
                    <video src="http://example.com/video1.mp4" />
                    <video src="http://example.com/video2.mp4" />
                </body>
            </smil>
        """.trimIndent()

        val parser = SmilUrlParser()
        val urls = parser.parseVideoUrls(smilContent)

        assertEquals(2, urls.size)
        assertEquals("http://example.com/video1.mp4", urls[0])
        assertEquals("http://example.com/video2.mp4", urls[1])
    }

    @Test
    fun `parse SMIL with switch element`() {
        val smilContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <smil>
                <body>
                    <switch>
                        <video src="http://example.com/high_quality.mp4" />
                        <video src="http://example.com/low_quality.mp4" />
                    </switch>
                </body>
            </smil>
        """.trimIndent()

        val parser = SmilUrlParser()
        val urls = parser.parseVideoUrls(smilContent)

        assertEquals(1, urls.size)
        assertEquals("http://example.com/high_quality.mp4", urls[0])
    }
}