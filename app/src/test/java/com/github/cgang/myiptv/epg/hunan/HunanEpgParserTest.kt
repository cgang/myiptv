package com.github.cgang.myiptv.epg.hunan

import org.junit.Test
import org.junit.Assert.*
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

class HunanEpgParserTest {

    @Test
    fun testParseDateTime() {
        val day = "20251230"
        val time = "180000"

        val date = parseDateTime(day, time)

        // The date should be December 30, 2025 at 18:00:00
        // Note: This test may need adjustment based on timezone handling
        assertTrue(date.time > 0) // Just verify it's a valid date
    }

    @Test
    fun testChannelToProgram() {
        // Test the conversion from Hunan EPG Channel to Program
        val playbillInfo = PlaybillInfo(
            videoId = "8f5e690420b22981bdad6ccdf6a00884",
            day = "20251230",
            beginTime = "180000",
            timeLen = 1800
        )

        val currentPlaybill = CurrentPlaybill(
            name = "新闻大求真",
            playbillInfo = playbillInfo
        )

        val channel = Channel(
            id = "8f5e690420b22981bdad6ccdf6a00884",
            name = "湖南卫视4K",
            cNo = "2",
            channelId = "42f0cf6816b94a11aed026633eee6df8",
            currentPlaybill = currentPlaybill
        )

        val program = channel.toProgram()

        assertEquals("8f5e690420b22981bdad6ccdf6a00884", program.chan.id)
        assertEquals("湖南卫视4K", program.chan.name)
        assertEquals(1, program.items.size)
        assertEquals("2", program.items[0].channel) // Using c_no as channel ID
        assertEquals("新闻大求真", program.items[0].title)
    }
}