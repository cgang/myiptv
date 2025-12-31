package com.github.cgang.myiptv

import com.github.cgang.myiptv.PlaybackFragment

/**
 * Test class to validate multicast URL parsing
 */
class UrlParsingTest {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            println("Testing multicast URL parsing...")

            // Create a mock PlaybackFragment to test the URL parsing
            val testFragment = TestPlaybackFragment()

            // Test different URL formats
            val testUrls = listOf(
                "rtp://239.9.9.9:9999",
                "udp://wlan0@239.9.9.10:9998",
                "rtp://eth0@239.9.9.11:9997",
                "udp://239.9.9.12:9996"
            )

            for (url in testUrls) {
                val result = testFragment.testExtractMulticastInfo(url)
                if (result != null) {
                    val (interfaceName, address, port) = result
                    println("URL: $url -> Interface: $interfaceName, Address: $address, Port: $port")
                } else {
                    println("Failed to parse URL: $url")
                }
            }

            println("URL parsing test completed!")
        }
    }
}

// Mock class to test the extractMulticastInfo function
class TestPlaybackFragment {
    fun testExtractMulticastInfo(url: String): Triple<String, String, Int>? {
        try {
            // Handle both rtp:// and udp:// schemes
            val cleanUrl = url.substringAfter("://")

            // Check if interface is specified (format: interface@address:port)
            val atParts = cleanUrl.split("@", limit = 2)
            val interfaceName = if (atParts.size > 1) {
                atParts[0] // Interface name before @
            } else {
                // For testing purposes, use a default interface
                "eth0"
            }

            val addressPortPart = if (atParts.size > 1) atParts[1] else atParts[0]
            val colonParts = addressPortPart.split(":", limit = 2)

            if (colonParts.size != 2) {
                println("Invalid address:port format in URL: $url")
                return null
            }

            val address = colonParts[0]
            val port = colonParts[1].toInt()

            return Triple(interfaceName, address, port)
        } catch (e: Exception) {
            println("Error parsing multicast URL: $url, Error: ${e.message}")
            return null
        }
    }
}