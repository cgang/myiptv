package com.github.cgang.myiptv

import android.content.Context
import android.widget.ArrayAdapter
import androidx.annotation.LayoutRes

class PlaylistAdapter(
    context: Context,
    @param:LayoutRes private val itemLayout: Int
) : ArrayAdapter<Channel>(context, itemLayout, R.id.tvName) {

    val default: Channel?
        get() = if (count == 0) null else getItem(0)

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
}
