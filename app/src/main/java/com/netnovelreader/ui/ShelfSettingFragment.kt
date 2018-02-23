package com.netnovelreader.ui

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceFragment
import com.netnovelreader.R

/**
 * 设置页面
 */
class ShelfSettingFragment : PreferenceFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.pref_settings)
        findPreference(getString(R.string.themeKey)).setOnPreferenceChangeListener { _, _ ->
            val intent = activity.intent
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            activity.finish()
            activity.overridePendingTransition(0, 0)
            activity.startActivity(intent)
            true
        }
    }
}