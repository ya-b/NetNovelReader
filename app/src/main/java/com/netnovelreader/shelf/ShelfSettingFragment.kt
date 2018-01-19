package com.netnovelreader.shelf

import android.os.Bundle
import android.preference.PreferenceFragment
import com.netnovelreader.R

/**
 * Created by yangbo on 2018/1/24.
 */
class ShelfSettingFragment : PreferenceFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.pref_settings)
    }
}