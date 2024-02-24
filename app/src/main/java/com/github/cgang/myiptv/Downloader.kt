package com.github.cgang.myiptv

import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import com.github.cgang.myiptv.xmltv.ProgramParser
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object Downloader {
    private val TAG = Downloader::class.java.simpleName
    private val DefaultTimeout = 500 // seconds
    val handler: Handler
    private var listener: PlaylistListener? = null
    private var lastPlaylistUrl: String? = null
    private var lastEpgUrl: String? = null

    init {
        val thread = HandlerThread("download")
        thread.start()
        handler = Handler(thread.looper)
    }

    fun register(l: PlaylistListener) {
        this.listener = l
    }

    fun getPlaylist(urlStr: String) {
        if (urlStr == lastPlaylistUrl) {
            return
        }

        lastPlaylistUrl = urlStr
        handler.post {
            download(urlStr) {
                val channels = M3UParser().parse(InputStreamReader(it))
                listener?.onChannels(channels)
            }
        }
    }

    fun getEPG(urlStr: String) {
        if (urlStr == lastEpgUrl) {
            return
        }

        lastEpgUrl = urlStr
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
            val url = URL(urlStr)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = DefaultTimeout
            conn.inputStream.use(handle)
            Log.d(TAG, "${urlStr} loaded successfully")
        } catch (e: IOException) {
            Log.w(TAG, "Failed to download ${urlStr}: ${e}")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to process ${urlStr}: ${e}")
        }
    }
}
