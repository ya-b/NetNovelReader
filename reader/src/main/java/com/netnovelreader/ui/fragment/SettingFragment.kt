package com.netnovelreader.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import com.netnovelreader.R

/**
 * 设置页面
 */
class SettingFragment : PreferenceFragmentCompat() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_settings)
        findPreference(getString(R.string.themeKey)).setOnPreferenceChangeListener { _, value ->
            val intent = activity?.intent
            intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            activity?.setResult(10)
            activity?.finish()
            activity?.overridePendingTransition(0, 0)
            activity?.startActivity(intent?.apply { putExtra("themeid", getTheme(value as String)) })
            true
        }
    }

    fun getTheme(str: String): Int {
        return when (str) {
            "blue" -> R.style.AppThemeBlue
            "gray" -> R.style.AppThemeGray
            else -> R.style.AppThemeBlack
        }
    }
}