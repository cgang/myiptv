package com.github.cgang.myiptv.rtp

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import java.net.NetworkInterface
import java.util.*

/**
 * Utility class for network interface detection
 */
object NetworkInterfaceUtils {
    /**
     * Gets the appropriate network interface for multicast
     * Prioritizes wired connections over wireless since multicast doesn't work well on wireless
     */
    fun getMulticastInterface(context: Context): String? {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Try to get the active network and determine its interface
        val activeNetwork = connectivityManager.activeNetwork
        val caps = connectivityManager.getNetworkCapabilities(activeNetwork)

        // Prioritize wired connections over wireless since multicast doesn't work well on wireless
        return if (caps?.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET) == true) {
            getNetworkInterfaceName("eth", "enp", "en") // Wired connections first
        } else if (caps?.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) == true) {
            getNetworkInterfaceName("wlan", "wlp", "wl") // Wireless as fallback
        } else {
            // Default to first available interface that's not loopback
            // Prioritize wired interfaces if no active network detected
            getNetworkInterfaceName("eth", "enp", "en", "wlan", "wlp", "wl")
        }
    }

    /**
     * Gets the name of the first network interface that starts with the given prefixes
     * Checks interfaces in order of preference
     */
    private fun getNetworkInterfaceName(vararg prefixes: String): String? {
        val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
        // First pass: check for interfaces that are up and match the prefixes
        for (prefix in prefixes) {
            for (networkInterface in interfaces) {
                if (networkInterface.isUp && !networkInterface.isLoopback && networkInterface.name.startsWith(prefix)) {
                    return networkInterface.name
                }
            }
        }
        return null
    }

    /**
     * Gets the first non-loopback network interface
     */
    private fun getFirstNonLoopbackInterface(): String? {
        val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
        for (networkInterface in interfaces) {
            if (networkInterface.isUp && !networkInterface.isLoopback) {
                return networkInterface.name
            }
        }
        return null
    }
}