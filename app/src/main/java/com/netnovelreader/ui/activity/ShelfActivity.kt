package com.netnovelreader.ui.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.netnovelreader.R
import com.netnovelreader.common.*
import com.netnovelreader.databinding.ActivityShelfBinding
import com.netnovelreader.ui.fragment.NovelClassfyFragment
import com.netnovelreader.ui.fragment.ShelfFragment
import com.netnovelreader.viewmodel.ShelfViewModel
import kotlinx.android.synthetic.main.activity_shelf.*
import kotlinx.coroutines.experimental.launch

class ShelfActivity : AppCompatActivity() {

    val shelfViewModel by lazy { obtainViewModel(ShelfViewModel::class.java) }
    private var hasPermission = false
    private var themeId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        themeId = PreferenceManager.getThemeId(this).also { setTheme(it) }
        super.onCreate(savedInstanceState)
        hasPermission = checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (!hasPermission) {
            requirePermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, 1)
        }
        initView()
    }

    fun initView() {
        DataBindingUtil.setContentView<ActivityShelfBinding>(this, R.layout.activity_shelf)
            .apply { viewModel = shelfViewModel }
        setSupportActionBar(shelfToolbar)
        shelfViewPager.apply {
            offscreenPageLimit = 4
            adapter = PagerAdapter(
                supportFragmentManager,
                arrayOf(getString(R.string.shelf), getString(R.string.classification)),
                arrayOf(ShelfFragment::class.java, NovelClassfyFragment::class.java)
            )
        }.let { shelfTab.setupWithViewPager(it) }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_shelf, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.search_button -> {
                startActivity(
                    Intent(this, SearchActivity::class.java)
                        .apply { putExtra("themeid", themeId) }
                )
                shelfViewModel.refreshType = 2
                true
            }
            R.id.action_settings -> {
                startActivityForResult(Intent(this, SettingActivity::class.java).apply {
                    putExtra("type", 0)
                    putExtra("themeid", themeId)
                }, 1)
                true
            }
            R.id.edit_site_preference -> {
                startActivity(Intent(this, SettingActivity::class.java).apply {
                    putExtra("type", 1)
                    putExtra("themeid", themeId)
                })
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == 10) {
            val intent = intent
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            finish()
            overridePendingTransition(0, 0)
            startActivity(intent)
        }
    }

    /**
     * 请求权限的结果
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hasPermission = true
                launch { shelfViewModel.refreshBookList() }
            } else {
                toast(getString(R.string.permission_warnning))
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    //请求权限
    fun requirePermission(permission: String, reqCode: Int) {
        ActivityCompat.requestPermissions(this, Array(1) { permission }, reqCode)
    }
}
