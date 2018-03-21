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
    private lateinit var settingViewModel: SettingViewModel
    val SP = "SitePreferenceFragment"
    val SE = "SiteEditorFragment"
    var siteListFg: Fragment? = null
    var siteEditorFg: Fragment? = null
    var menuItemList = ArrayList<MenuItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingViewModel = obtainViewModel(SettingViewModel::class.java)
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
            siteListFg = SitePreferenceFragment()
            supportFragmentManager.beginTransaction().add(R.id.settingFrameLayout, siteListFg, SP)
                    .commit()
        }
    }

    fun initLiveData() {
        settingViewModel.exitCommand.observe(this, Observer {
            if (siteListFg?.isHidden == true) {
                supportFragmentManager.beginTransaction().hide(siteEditorFg).show(siteListFg)
                        .commit()
                menuItemList.forEach { it.isVisible = true }
            } else {
                finish()
            }
        })
        settingViewModel.editSiteCommand.observe(this, Observer {
            siteEditorFg ?: kotlin.run { siteEditorFg = SiteEditorFragment.instance }
            menuItemList.forEach { it.isVisible = false }
            supportFragmentManager.beginTransaction().apply {
                if (supportFragmentManager.fragments.size > 1) {
                    show(siteEditorFg)
                } else {
                    add(R.id.settingFrameLayout, siteEditorFg, SE)
                }
            }.hide(siteListFg).commit()
        })
    }

    override fun onBackPressed() {
        settingViewModel.exitCommand.call()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_setting, menu)
        menuItemList.add(menu.findItem(R.id.addSitePreference))
        menuItemList.add(menu.findItem(R.id.updateSitePreference))
        return intent.getIntExtra("type", 0) != 0   //false 不显示
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.updateSitePreference -> showDialog()
            R.id.addSitePreference -> {
                settingViewModel.edittingSite!!.add(null)
                settingViewModel.editSiteCommand.value = null
            }
        }
        return true
    }

    fun showDialog() {
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
}
