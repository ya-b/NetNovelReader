package com.netnovelreader.setting

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.netnovelreader.R
import com.netnovelreader.common.PreferenceManager
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : AppCompatActivity() {
    var themeId: Int = 0
    private var settingFragment: ShelfSettingFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themeId = PreferenceManager.getThemeId(this)
        setTheme(themeId)
        setContentView(R.layout.activity_setting)


        setSupportActionBar(settingToolbar.apply {
            setTitle(R.string.settings)
            setNavigationIcon(R.drawable.icon_back)
        })
        settingToolbar.setNavigationOnClickListener {
            this@SettingActivity.finish()
        }

        settingFragment = ShelfSettingFragment()
        fragmentManager.beginTransaction().replace(R.id.shelfFrameLayout, settingFragment)
            .commit()
    }

}
