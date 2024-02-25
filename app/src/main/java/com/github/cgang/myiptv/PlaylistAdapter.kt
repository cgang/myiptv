package com.github.cgang.myiptv

import android.content.Context
import android.widget.ArrayAdapter
import androidx.annotation.LayoutRes

class PlaylistAdapter(
    context: Context,
    @param:LayoutRes private val itemLayout: Int
) : ArrayAdapter<Channel>(context, itemLayout, R.id.tvName) {
    fun setChannels(channels: List<Channel>) {
        this.setNotifyOnChange(false)
        this.clear()
        this.addAll(channels)
        this.notifyDataSetChanged()
    }

    fun getChannel(position: Int): Channel? {
        if (position < 0 || position > count) {
            return null
        }

        return getItem(position)
    }

    private fun indexOf(url: String): Int {
        for (idx in 0..<this.count) {
            val ch = this.getItem(idx)
            if (ch?.url == url) {
                return idx
            }
        }
        return -1
    }

    fun switchChannel(url: String, step: Int): Channel? {
        val total = this.count
        if (total <= 1) {
            return null
        }

        var index = indexOf(url)
        if (index < 0 || index >= total) {
            return null
        }

        index += step
        index = Math.floorMod(index, total)
        return getItem(index)
    }
}
