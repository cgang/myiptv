package com.github.cgang.myiptv

import com.github.cgang.myiptv.xmltv.Program

interface PlaylistListener {
    fun onChannels(tvgUrl: String?, channels: List<Channel>)
    fun onPrograms(programs: Map<String, Program>)
}
