package com.netnovelreader.editor

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.netnovelreader.R
import com.netnovelreader.common.PreferenceManager

class SiteEditorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferenceManager.setTheme(this)
        setContentView(R.layout.activity_site_editor)
    }
}
