package com.github.cgang.myiptv

import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.cgang.myiptv.xmltv.Program
import com.github.cgang.myiptv.xmltv.Programme
import com.github.cgang.myiptv.xmltv.formatTime

class ProgramInfoFragment() : Fragment(R.layout.program_info) {
    fun setProgram(program: Program?): Boolean {
        val items = program?.getRecent(2) ?: return false
        if (items.isEmpty()) {
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
            val start = formatTime(it.start)
            val stop = formatTime(it.stop)
            return "${start} - ${stop} ${prog.title}"
        } ?: ""
    }
}