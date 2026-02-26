package com.github.cgang.myiptv

import android.content.Context
import android.content.SharedPreferences
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.preference.PreferenceManager

class PlaylistAdapter(
    context: Context,
    @param:LayoutRes private val itemLayout: Int
) : ArrayAdapter<Channel>(context, itemLayout, R.id.tvName) {

    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val textSizeMultiplier: Float
        get() {
            val textSize = sharedPreferences.getString("TextSize", "normal") ?: "normal"
            return when (textSize) {
                "large" -> 1.25f
                "extra_large" -> 1.5f
                else -> 1.0f
            }
        }

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

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(itemLayout, parent, false)
        val textView = view.findViewById<TextView>(R.id.tvName)

        val channel = getItem(position)
        textView.text = channel?.name

        // Apply text size multiplier
        val baseTextSize = 20f // Base text size in sp from channel_item.xml
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, baseTextSize * textSizeMultiplier)

        return view
    }
}
