package com.github.cgang.myiptv.rtp

import android.util.Log
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

        private const val TAG = "RtpPacket"
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
            PROFILE_MPEGTS, PROFILE_MPEG_VIDEO, PROFILE_MPEG_AUDIO -> {
                Pair(true, null)
            }
            else -> {
                Pair(false, "Unknown payload profile: $payloadType")
            }
        }
    }

    /**
     * Strips the RTP header from the packet
     */
    fun stripRtp() {
        val signature = getByte(0) // Version (2 bits), padding (1 bit), extension (1 bit), CSRC count (4 bits)
        val payloadType = getByte(1)
        sequence = getUint16(2)

        var newOffset = RTP_HEADER_SIZE

        // Handle CSRC (Contributing Source) identifiers
        val csrcCount = signature and 0x0F // CSRC count
        if (csrcCount > 0) {
            newOffset += csrcCount * 4
        }

        // Handle RTP header extension (if present)
        if ((signature and 0x10) != 0) { // Extension bit is set
            // Extension header is 4 bytes: 2 bytes for extension ID + 2 bytes for length
            val extensionLength = getUint16(newOffset + 2) // Get the number of 16-bit words in extensions
            newOffset += 4 + extensionLength * 2 // 4 bytes for extension header + extension data
        }

        // Update offset to point to the payload
        offset = newOffset

        // Handle padding (if present)
        if ((signature and 0x20) != 0) { // Padding bit is set
            val paddingSize = getByte(length - 1)
            if (paddingSize > 0) {
                length -= paddingSize
            }
        }
    }

    /**
     * Gets the next sequence number
     */
    fun nextSeq(): Int {
        val next = (sequence + 1) and 0xFFFF // 16-bit wraparound
        return next
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