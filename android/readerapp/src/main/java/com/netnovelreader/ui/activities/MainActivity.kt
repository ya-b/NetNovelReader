package com.netnovelreader.ui.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import com.netnovelreader.R
import com.netnovelreader.utils.get
import com.netnovelreader.utils.sharedPreferences


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(sharedPreferences().get(getString(R.string.nightModeKey), false)) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }.let { AppCompatDelegate.setDefaultNightMode(it) }
        setContentView(R.layout.activity_main)
    }

}
