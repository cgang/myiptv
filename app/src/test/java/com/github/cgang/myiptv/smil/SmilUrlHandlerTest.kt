package com.github.cgang.myiptv.smil

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SmilUrlHandlerTest {

    @Test
    fun `detect SMIL URLs correctly`() {
        val handler = SmilUrlHandler(mockOkHttpClient())

        assertTrue(handler.isSmilUrl("http://example.com/video.smil"))
        assertTrue(handler.isSmilUrl("http://example.com/video.smi"))
        assertTrue(handler.isSmilUrl("http://example.com/video.SMIL"))
        assertTrue(handler.isSmilUrl("http://example.com/video.SMI"))

        assertFalse(handler.isSmilUrl("http://example.com/video.mp4"))
        assertFalse(handler.isSmilUrl("http://example.com/video.m3u8"))
        assertFalse(handler.isSmilUrl("http://example.com/video"))
    }

    private fun mockOkHttpClient() = okhttp3.OkHttpClient()
}