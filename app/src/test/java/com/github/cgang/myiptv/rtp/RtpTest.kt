package com.github.cgang.myiptv.rtp

/**
 * Simple test to validate RTP packet handling logic
 */
class RtpTest {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            println("Testing RTP packet handling...")

            // Create a mock RTP packet with known values
            // RTP header: version=2, padding=0, extension=0, CSRC count=0, marker=0, payload type=0x21 (MPEG-TS)
            // Sequence number: 1234, Timestamp: 0x12345678, SSRC: 0x87654321
            val mockRtpPacket = byteArrayOf(
                (0x80 or 0x21).toByte(),  // Version=2 (10), Padding=0, Extension=0, CSRC=0, Payload Type=0x21
                0x21.toByte(),            // Payload Type
                0x04.toByte(),            // Sequence number high
                0xD2.toByte(),            // Sequence number low (1234 = 0x04D2)
                0x12.toByte(),            // Timestamp byte 1
                0x34.toByte(),            // Timestamp byte 2
                0x56.toByte(),            // Timestamp byte 3
                0x78.toByte(),            // Timestamp byte 4
                0x87.toByte(),            // SSRC byte 1
                0x65.toByte(),            // SSRC byte 2
                0x43.toByte(),            // SSRC byte 3
                0x21.toByte()             // SSRC byte 4
                // Add some payload data
            ) + "Mock MPEG-TS payload data".toByteArray()

            val rtpPacket = RtpPacket(mockRtpPacket)

            // Test packet validation
            val (isRtp, error) = rtpPacket.check()
            if (!isRtp) {
                println("Error: Not detected as RTP packet: $error")
                return
            }

            println("RTP packet validation passed")

            // Test RTP header stripping
            rtpPacket.stripRtp()
            println("RTP header stripped. Offset: ${rtpPacket.offset}, Length: ${rtpPacket.length}")

            // Test sequence number handling
            val nextSeq = rtpPacket.nextSeq()
            println("Next sequence number: $nextSeq")

            println("RTP packet handling test completed successfully!")
        }
    }
}