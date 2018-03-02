package com.netnovelreader.ui.activity

import android.app.AlertDialog
import android.arch.lifecycle.Observer
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.netnovelreader.R
import com.netnovelreader.common.obtainViewModel
import com.netnovelreader.databinding.ActivitySettingBinding
import com.netnovelreader.ui.fragment.SettingFragment
import com.netnovelreader.ui.fragment.SiteEditorFragment
import com.netnovelreader.ui.fragment.SitePreferenceFragment
import com.netnovelreader.viewmodel.SettingViewModel
import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.coroutines.experimental.launch

class SettingActivity : AppCompatActivity() {
    val settingViewModel by lazy { obtainViewModel(SettingViewModel::class.java) }
    val SP = "SitePreferenceFragment"
    val SE = "SiteEditorFragment"
    var mTemp: Fragment? = null
    var siteListFg: Fragment? = null
    var updateMenu: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(intent.getIntExtra("themeid", R.style.AppThemeBlack))
        initView()
        initLiveData()
    }

    fun initView() {
        DataBindingUtil.setContentView<ActivitySettingBinding>(this, R.layout.activity_setting)
            .apply { viewModel = settingViewModel }
        if (intent.getIntExtra("type", 0) == 0) {
            setSupportActionBar(settingToolbar.apply { title = getString(R.string.settings) })
            fragmentManager.beginTransaction().add(R.id.settingFrameLayout, SettingFragment())
                .commit()
        } else {
            setSupportActionBar(settingToolbar.apply { title = getString(R.string.edit_site) })
            siteListFg = SitePreferenceFragment().also { mTemp = it }
            supportFragmentManager.beginTransaction().add(R.id.settingFrameLayout, siteListFg, SP)
                .commit()
        }
    }

    fun initLiveData() {
        settingViewModel.exitCommand.observe(this, Observer {
            if (siteListFg?.isHidden == true) {
                supportFragmentManager.beginTransaction().hide(mTemp).show(siteListFg).commit()
                updateMenu?.isVisible = true
            } else {
                finish()
            }
        })
        settingViewModel.editSiteCommand.observe(this, Observer {
            mTemp = SiteEditorFragment.instance
            updateMenu?.isVisible = false
            supportFragmentManager.beginTransaction().apply {
                if (supportFragmentManager.fragments.size > 1) {
                    show(mTemp)
                } else {
                    add(R.id.settingFrameLayout, mTemp, SE)
                }
            }.hide(siteListFg).commit()
        })
    }

    override fun onBackPressed() {
        settingViewModel.exitCommand.call()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_setting, menu)
        updateMenu = menu.findItem(R.id.updateSitePreference)
        return intent.getIntExtra("type", 0) != 0
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.updateSitePreference) {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.whenconflict))
                .setPositiveButton(R.string.no, { _, _ ->
                    launch { settingViewModel.updatePreference(false) }
                })
                .setNegativeButton(R.string.yes_local, { _, _ ->
                    launch { settingViewModel.updatePreference(true) }
                })
                .setNeutralButton(R.string.cancel, null)
                .create()
                .show()
        }
        return true
    }
}
