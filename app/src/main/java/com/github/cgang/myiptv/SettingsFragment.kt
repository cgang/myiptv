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

        // Setup the multicast interface preference with available network interfaces
        val multicastInterfacePref = findPreference<androidx.preference.ListPreference>("MulticastInterface")
        if (multicastInterfacePref != null) {
            try {
                val interfaces = java.util.Collections.list(java.net.NetworkInterface.getNetworkInterfaces())
                    .filter { it.isUp && !it.isLoopback }

                // Create arrays for entries and entry values
                val entries = mutableListOf<CharSequence>()
                val entryValues = mutableListOf<CharSequence>()

                // Add "auto" option first
                entries.add("Auto-detect interface")
                entryValues.add("auto")

                // Add available interfaces
                interfaces.forEach { networkInterface ->
                    entries.add("${networkInterface.name} (${if (networkInterface.displayName != null) networkInterface.displayName else "interface"})")
                    entryValues.add(networkInterface.name)
                }

                // Set the entries and values
                multicastInterfacePref.entries = entries.toTypedArray()
                multicastInterfacePref.entryValues = entryValues.toTypedArray()
            } catch (e: Exception) {
                // Fallback to just "auto" if we can't get interfaces
                val entries = arrayOf("Auto-detect interface")
                val entryValues = arrayOf("auto")
                multicastInterfacePref.entries = entries
                multicastInterfacePref.entryValues = entryValues
            }
        }
    }
}