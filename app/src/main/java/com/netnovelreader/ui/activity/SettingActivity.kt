package com.netnovelreader.ui.activity

import android.arch.lifecycle.Observer
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.netnovelreader.R
import com.netnovelreader.common.PreferenceManager
import com.netnovelreader.common.obtainViewModel
import com.netnovelreader.databinding.ActivitySettingBinding
import com.netnovelreader.ui.fragment.SettingFragment
import com.netnovelreader.ui.fragment.SiteEditorFragment
import com.netnovelreader.ui.fragment.SitePreferenceFragment
import com.netnovelreader.viewmodel.SettingViewModel
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : AppCompatActivity() {
    val settingViewModel by lazy { obtainViewModel(SettingViewModel::class.java) }
    val SP = "SitePreferenceFragment"
    val SE = "SiteEditorFragment"
    var mFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(PreferenceManager.getThemeId(this))
        initView()
        initLiveData()
    }

    fun initView() {
        DataBindingUtil.setContentView<ActivitySettingBinding>(this, R.layout.activity_setting)
                .apply { viewModel = settingViewModel }
        if (intent.getIntExtra("type", 0) == 0) {
            setSupportActionBar(settingToolbar.apply { title = getString(R.string.settings) })
            fragmentManager.beginTransaction().add(R.id.settingFrameLayout, SettingFragment()).commit()
        } else {
            setSupportActionBar(settingToolbar.apply { title = getString(R.string.edit_site) })
            mFragment = SitePreferenceFragment()
            supportFragmentManager.beginTransaction().add(R.id.settingFrameLayout, mFragment, SP).commit()
        }
    }

    fun initLiveData() {
        settingViewModel.exitCommand.observe(this, Observer {
            if (mFragment?.tag == SE) {
                supportFragmentManager.beginTransaction().remove(mFragment).commit()
            } else {
                finish()
            }
        })
        settingViewModel.editSiteCommand.observe(this, Observer {
            mFragment = SiteEditorFragment()
            supportFragmentManager.beginTransaction().add(R.id.settingFrameLayout, mFragment, SE).commit()
        })
    }

    override fun onBackPressed() {
        settingViewModel.exitCommand.call()
    }
}
