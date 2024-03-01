package com.github.cgang.myiptv

import android.os.Bundle
import android.text.InputType
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        findPreference<EditTextPreference>("PlaylistUrl")?.setOnBindEditTextListener {
            it.inputType = InputType.TYPE_TEXT_VARIATION_URI
        }

        findPreference<EditTextPreference>("EPGUrl")?.setOnBindEditTextListener {
            it.inputType = InputType.TYPE_TEXT_VARIATION_URI
        }

        findPreference<EditTextPreference>("BufferDuration")?.setOnBindEditTextListener {
            it.inputType = InputType.TYPE_CLASS_NUMBER
        }
    }
}