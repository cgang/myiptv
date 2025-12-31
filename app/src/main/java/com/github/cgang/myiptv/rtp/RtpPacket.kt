package com.github.cgang.myiptv.rtp

import java.nio.ByteBuffer

/**
 * Represents an RTP packet with utilities for parsing and stripping RTP headers
 */
class RtpPacket(val data: ByteArray, var offset: Int = 0, var length: Int = data.size) {
    companion object {
        const val RTP_HEADER_SIZE = 12
        const val PROFILE_MPEGTS = 0x21
        const val PROFILE_MPEG_VIDEO = 0x20
        const val PROFILE_MPEG_AUDIO = 0x0E
    }

    var sequence: Int = 0

    /**
     * Checks if this is a valid RTP packet
     */
    fun check(): Pair<Boolean, String?> {
        if (length < RTP_HEADER_SIZE) {
            return Pair(false, "Invalid packet length: $length")
        }

        val signature = getByte(0)
        if (signature == 0x47) { // Magic number for MPEG-TS
            return Pair(false, null) // Not RTP, it's MPEG-TS
        }

        val version = (signature and 0xC0) shr 6
        if (version != 2) { // Only RTP version 2 is supported
            return Pair(false, "Unsupported RTP version: $version")
        }

        val payloadType = getByte(1) and 0x7F
        return when (payloadType) {
            PROFILE_MPEGTS, PROFILE_MPEG_VIDEO, PROFILE_MPEG_AUDIO -> Pair(true, null)
            else -> Pair(false, "Unknown payload profile: $payloadType")
        }
    }

    /**
     * Strips the RTP header from the packet
     */
    fun stripRtp() {
        val payloadType = getByte(1)
        sequence = getUint16(2).toInt()

        var newOffset = RTP_HEADER_SIZE
        when (payloadType.toByte()) {
            PROFILE_MPEGTS.toByte() -> {
                // No additional offset for MPEG-TS
            }
            PROFILE_MPEG_VIDEO.toByte(), PROFILE_MPEG_AUDIO.toByte() -> {
                newOffset += 4 // Skip 4 bytes for MPEG video/audio
            }
        }

        val signature = getByte(0) // Signature bits
        val csrcCount = signature and 0x0F // CSRC count
        if (csrcCount > 0) {
            newOffset += csrcCount * 4
        }

        if ((signature and 0x10) != 0) { // Extension available - check for RTP header extensions
            val extensionLength = getUint16(newOffset + 2) // Get extension header length (after 2 bytes for extension ID)
            newOffset += 4 + extensionLength * 2 // 2 bytes for extension ID + 2 bytes for length, then extensionLength * 2 bytes for extension data
        }

        offset = newOffset
        if ((signature and 0x20) != 0) { // Padding
            val paddingSize = getByte(length - 1)
            length -= paddingSize.toInt()
        }
    }

    /**
     * Gets the next sequence number
     */
    fun nextSeq(): Int {
        return (sequence + 1) and 0xFFFF // 16-bit wraparound
    }

    private fun getByte(offset: Int): Int {
        return data[offset].toInt() and 0xFF
    }

    private fun getUint16(offset: Int): Int {
        return (data[offset + 1].toInt() and 0xFF) or ((data[offset].toInt() and 0xFF) shl 8)
    }

    /**
     * Writes the packet data to the provided buffer
     */
    fun writeTo(buffer: ByteBuffer) {
        buffer.put(data, offset, length - offset)
    }
}