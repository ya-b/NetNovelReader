package com.netnovelreader.ui

import android.app.Fragment
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.netnovelreader.R
import com.netnovelreader.common.PreferenceManager
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : AppCompatActivity() {
    var themeId: Int = 0
    private var mFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themeId = PreferenceManager.getThemeId(this)
        setTheme(themeId)
        setContentView(R.layout.activity_setting)
        settingToolbar.setNavigationOnClickListener {
            this@SettingActivity.finish()
        }
        if(intent.getIntExtra("type", 0) == 0){
            setSupportActionBar(settingToolbar.apply { title = getString(R.string.settings) })
            mFragment = SettingFragment()
            fragmentManager.beginTransaction().replace(R.id.shelfFrameLayout, mFragment).commit()
        }else{
            setSupportActionBar(settingToolbar.apply { title = getString(R.string.edit_site) })
            mFragment = SitePreferenceFragment.newInstance()
            fragmentManager.beginTransaction().replace(R.id.shelfFrameLayout, mFragment).commit()
        }
    }

}
