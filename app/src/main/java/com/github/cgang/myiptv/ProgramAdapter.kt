package com.github.cgang.myiptv

import android.content.Context
import android.content.SharedPreferences
import android.text.format.DateFormat
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.preference.PreferenceManager
import com.github.cgang.myiptv.xmltv.Programme

class ProgramAdapter(
    context: Context
) : BaseAdapter() {
    val inflater: LayoutInflater = LayoutInflater.from(context)
    val list = mutableListOf<Programme>()
    val timeFormat = DateFormat.getTimeFormat(context)
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
    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(position: Int): Any {
        return list.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: inflater.inflate(R.layout.programme_item, parent, false)
        list.getOrNull(position)?.let {
            view.findViewById<TextView>(R.id.start_time)?.let { startTime ->
                startTime.text = timeFormat.format(it.start)
                startTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f * textSizeMultiplier)
            }
            view.findViewById<TextView>(R.id.programme_title)?.let { title ->
                title.text = it.title
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f * textSizeMultiplier)
            }
        }
        return view
    }

    fun setList(list: Collection<Programme>) {
        this.list.clear()
        this.list.addAll(list)
        this.notifyDataSetChanged()
    }
}