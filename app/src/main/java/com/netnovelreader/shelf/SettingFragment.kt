package com.netnovelreader.shelf

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netnovelreader.R

class SettingFragment : PreferenceFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.pref_settings)
        findPreference(getString(R.string.themeKey)).setOnPreferenceChangeListener { preference, newValue ->
            startActivity(Intent(activity, activity.javaClass))
            activity.finish()
//            activity.recreate()
            true
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        view.setBackgroundColor(Color.WHITE)
        return view
    }
}