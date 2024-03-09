package com.github.cgang.myiptv

import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.github.cgang.myiptv.xmltv.Programme

class ProgramAdapter(
    context: Context
) : BaseAdapter() {
    val inflater: LayoutInflater = LayoutInflater.from(context)
    val list = mutableListOf<Programme>()
    val timeFormat = DateFormat.getTimeFormat(context)

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
            view.findViewById<TextView>(R.id.start_time)?.text = timeFormat.format(it.start)
            view.findViewById<TextView>(R.id.programme_title)?.text = it.title
        }
        return view
    }

    fun setList(list: Collection<Programme>) {
        this.list.clear()
        this.list.addAll(list)
        this.notifyDataSetChanged()
    }
}