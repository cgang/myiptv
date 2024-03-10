package com.github.cgang.myiptv

import android.content.Context
import android.text.format.DateFormat
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.cgang.myiptv.xmltv.Program
import com.github.cgang.myiptv.xmltv.Programme

class ProgramInfoFragment(context: Context) : Fragment(R.layout.program_info) {
    private val timeFormat = DateFormat.getTimeFormat(context)

    fun setProgram(program: Program?): Boolean {
        val items = program?.getRecent(2)
        if (items.isNullOrEmpty()) {
            return false
        }

        view?.findViewById<TextView>(R.id.current_programme)?.let {
            it.text = format(items[0])
        }
        view?.findViewById<TextView>(R.id.next_programme)?.let {
            it.text = format(items.getOrNull(1))
        }
        return true
    }

    private fun format(prog: Programme?): String {
        return prog?.let {
            val start = timeFormat.format(it.start)
            return "$start ${prog.title}"
        } ?: ""
    }
}