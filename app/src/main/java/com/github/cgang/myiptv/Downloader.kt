package com.github.cgang.myiptv

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import com.github.cgang.myiptv.smil.SmilUrlHandler
import com.github.cgang.myiptv.xmltv.Program
import com.github.cgang.myiptv.xmltv.ProgramParser
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

class Downloader(val context: Context) {
    interface Listener {
        fun onChannels(tvgUrl: String?, channels: List<Channel>)

        fun onPrograms(programs: Collection<Program>)
    }

    private var listener: Listener? = null
    private val client: OkHttpClient
    private val handler: Handler

    init {
        val thread = HandlerThread("download")
        thread.start()
        handler = Handler(thread.looper)

        val cacheSize = 500L * 1024L * 1024L // 500 MiB
        val cacheDir = context.externalCacheDir ?: context.cacheDir
        client = OkHttpClient.Builder()
            .cache(Cache(cacheDir, cacheSize))
            .build()
    }

    fun register(l: Listener) {
        this.listener = l
    }

    fun downloadPlaylist(urlStr: String, maxAge: Int) {
        handler.post {
            download(urlStr, maxAge) { inputStream ->
                // Parse as M3U
                val parser = M3UParser()
                parser.parse(InputStreamReader(inputStream))

                // Process channels to resolve SMIL URLs
                val processedChannels = parser.channels.map { channel ->
                    // Check if the channel URL is a SMIL URL
                    if (SmilUrlHandler(client).isSmilUrl(channel.url ?: "")) {
                        try {
                            // Resolve the SMIL URL to get the actual video URL
                            val resolvedUrl = SmilUrlHandler(client).resolveSmilUrl(channel.url ?: "")
                            // Create a new channel with the resolved URL
                            val resolvedChannel = Channel(channel.name)
                            resolvedChannel.group = channel.group
                            resolvedChannel.url = resolvedUrl
                            resolvedChannel.logoUrl = channel.logoUrl
                            resolvedChannel.tvgId = channel.tvgId
                            resolvedChannel.tvgName = channel.tvgName
                            resolvedChannel.mimeType = channel.mimeType
                            resolvedChannel
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to resolve SMIL URL ${channel.url}: ${e.message}")
                            // Return the original channel if resolution fails
                            channel
                        }
                    } else {
                        // Not a SMIL URL, return as is
                        channel
                    }
                }

                listener?.onChannels(parser.tvgUrl, processedChannels)
            }
        }
    }

    fun downloadEPG(urlStr: String, maxAge: Int) {
        handler.post {
            download(urlStr, maxAge) {
                val programs = ProgramParser().parse(it)
                listener?.onPrograms(programs)
            }
        }
    }

    private fun download(urlStr: String, maxAge: Int, handle: (input: InputStream) -> Unit) {
        try {
            Log.d(TAG, "Downloading ${urlStr}")
            val cc = CacheControl.Builder().maxAge(maxAge, TimeUnit.HOURS).build()
            val request = Request.Builder().cacheControl(cc).url(urlStr).build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                response.body!!.use {
                    handle(it.byteStream())
                }
                Log.d(TAG, "${urlStr} loaded successfully")
            } else {
                Log.d(TAG, "Failed to download ${urlStr}: ${response.body!!.string()}")
            }
        } catch (e: IOException) {
            Log.w(TAG, "Failed to download ${urlStr}", e)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to process ${urlStr}", e)
        }
    }

    companion object {
        val TAG = Downloader::class.java.simpleName
    }
}
