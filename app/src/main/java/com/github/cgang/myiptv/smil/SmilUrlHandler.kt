package com.github.cgang.myiptv.smil

import android.net.Uri
import com.github.cgang.myiptv.Downloader
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

/**
 * Helper class to handle SMIL URLs in M3U playlists
 */
class SmilUrlHandler(private val client: OkHttpClient) {
    
    /**
     * Check if a URL is a SMIL URL based on its extension
     */
    fun isSmilUrl(url: String): Boolean {
        return url.endsWith(".smil", ignoreCase = true) || 
               url.endsWith(".smi", ignoreCase = true)
    }
    
    /**
     * Resolve a SMIL URL to get the actual video URL(s)
     * For switch elements, returns the first (preferred) video URL
     */
    @Throws(SmilUrlParseException::class, IOException::class)
    fun resolveSmilUrl(smilUrl: String): String {
        // Download the SMIL file
        val request = Request.Builder().url(smilUrl).build()
        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val parser = SmilUrlParser()
                val videoUrls = parser.parseVideoUrls(response.body!!.byteStream())
                
                // Return the first video URL if available
                if (videoUrls.isNotEmpty()) {
                    return videoUrls[0]
                } else {
                    throw SmilUrlParseException("No video URLs found in SMIL file")
                }
            } else {
                throw IOException("Failed to download SMIL file: ${response.code}")
            }
        }
    }
}