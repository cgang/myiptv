package com.github.cgang.myiptv.epg.hunan

import com.github.cgang.myiptv.xmltv.Program
import com.github.cgang.myiptv.xmltv.Channel as XmltvChannel
import com.github.cgang.myiptv.xmltv.Programme as XmltvProgramme
import java.util.Date

data class Channel(
    val id: String,
    val name: String,
    val cNo: String, // Channel number
    val channelId: String,
    val currentPlaybill: CurrentPlaybill?
)

data class CurrentPlaybill(
    val name: String,
    val playbillInfo: PlaybillInfo
)

data class PlaybillInfo(
    val videoId: String,
    val day: String, // Format: YYYYMMDD
    val beginTime: String, // Format: HHmmss
    val timeLen: Int // Duration in seconds
)

/**
 * Converts Hunan EPG Channel to the standard Program structure used by the app
 */
fun Channel.toProgram(): Program {
    val xmltvChannel = XmltvChannel(id, name)
    val programmes = mutableListOf<XmltvProgramme>()

    // If there's current playbill information, convert it to programme
    currentPlaybill?.let { playbill ->
        val programme = playbill.toProgramme(cNo)
        programmes.add(programme)
    }

    return Program(xmltvChannel, programmes)
}

/**
 * Converts Hunan EPG PlaybillInfo to the standard Programme structure
 */
fun CurrentPlaybill.toProgramme(channelNo: String): XmltvProgramme {
    val startTime = parseDateTime(this.playbillInfo.day, this.playbillInfo.beginTime)
    val stopTime = Date(startTime.time + (this.playbillInfo.timeLen * 1000L)) // timeLen is in seconds

    return XmltvProgramme(
        channel = channelNo, // Use the user-aware channel number (c_no) as the channel identifier
        start = startTime,
        stop = stopTime,
        title = this.name,
        description = "" // Description not available in this format
    )
}

/**
 * Parses date and time from the Hunan EPG format
 * @param day Format: YYYYMMDD
 * @param time Format: HHmmss
 * @return Date object
 */
fun parseDateTime(day: String, time: String): Date {
    // Format: YYYYMMDD + HHmmss
    val dateTimeStr = "${day}${time} +0800" // Assuming China Standard Time (UTC+8)
    return try {
        val format = java.text.SimpleDateFormat("yyyyMMddHHmmss Z", java.util.Locale.getDefault())
        format.parse(dateTimeStr) ?: Date()
    } catch (e: Exception) {
        Date() // Return current date if parsing fails
    }
}