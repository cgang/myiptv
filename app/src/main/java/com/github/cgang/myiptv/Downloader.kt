package com.github.cgang.myiptv

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import com.github.cgang.myiptv.xmltv.Program
import com.github.cgang.myiptv.xmltv.ProgramParser
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

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

    fun downloadPlaylist(urlStr: String) {
        handler.post {
            download(urlStr) {
                val parser = M3UParser()
                parser.parse(InputStreamReader(it))
                listener?.onChannels(parser.tvgUrl, parser.channels)
            }
        }
    }

    fun downloadEPG(urlStr: String) {
        handler.post {
            download(urlStr) {
                val programs = ProgramParser().parse(it)
                listener?.onPrograms(programs)
            }
        }
    }

    private fun download(urlStr: String, handle: (input: InputStream) -> Unit) {
        try {
            Log.d(TAG, "Downloading ${urlStr}")
            val request = Request.Builder().url(urlStr).build()
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
            Log.w(TAG, "Failed to download ${urlStr}: ${e}")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to process ${urlStr}: ${e}")
        }
    }

    companion object {
        val TAG = Downloader::class.java.simpleName
    }
}
