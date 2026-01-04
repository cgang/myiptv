package com.github.cgang.myiptv.rtp

import java.io.IOException

/**
 * Represents an RTP packet with utilities for parsing and stripping RTP headers
 */
class RtpPacket(val data: ByteArray, var limit: Int)
    : Comparable<RtpPacket> {
    companion object {
        const val RTP_HEADER_SIZE = 12
        const val PROFILE_MPEGTS = 0x21
        const val PROFILE_MPEG_VIDEO = 0x20
        const val PROFILE_MPEG_AUDIO = 0x0E
        const val MTU_SIZE = 1500

        // Helper function to compare sequence numbers with wraparound handling
        // Returns -1 if seq1 < seq2, 0 if seq1 == seq2, 1 if seq1 > seq2, considering wraparound
        fun compareSeq(seq1: Int, seq2: Int): Int {
            if (seq1 == seq2) {
                return 0
            }

            // Handle wraparound: if the difference is large, consider the possibility of wraparound
            val diff = (seq1 - seq2) and 0xFFFF  // Ensure we're working with 16-bit values
            // If the difference is between 1 and 0x7FFF (32767), then seq1 is higher
            // If the difference is between 0x8000 (32768) and 0xFFFF (65535), then seq1 is lower
            return if (diff in 1..0x7FFF) 1 else -1
        }
    }

    var offset: Int = 0
    var sequence: Int = 0

    /**
     * Checks if this is a valid RTP packet
     */
    fun check(): Boolean {
        if (limit < RTP_HEADER_SIZE) {
            throw IOException("Invalid packet length: $limit")
        }

        val signature = getByte(0)
        if (signature == 0x47) { // Magic number for MPEG-TS
            return false // Not RTP, it's MPEG-TS
        }

        val version = (signature and 0xC0) shr 6
        if (version != 2) { // Only RTP version 2 is supported
            throw IOException("Unsupported RTP version: $version")
        }

        val payloadType = getByte(1) and 0x7F
        return when (payloadType) {
            PROFILE_MPEGTS, PROFILE_MPEG_VIDEO, PROFILE_MPEG_AUDIO -> {
                true
            }
            else -> {
                throw IOException("Unknown payload profile: $payloadType")
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
            val paddingSize = getByte(limit - 1)
            if (paddingSize > 0) {
                limit -= paddingSize
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

    fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        val remaining = this.limit - this.offset
        val count = minOf(length, remaining)
        System.arraycopy(this.data, this.offset, buffer, offset, count)
        this.offset += count
        return count
    }

    override fun compareTo(other: RtpPacket): Int {
        // Handle sequence number wraparound when comparing
        return compareSeq(this.sequence, other.sequence)
    }
}