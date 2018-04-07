package com.netnovelreader.ui.activity

import android.arch.lifecycle.Observer
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.animation.DecelerateInterpolator
import com.netnovelreader.R
import com.netnovelreader.common.PagerAdapter
import com.netnovelreader.common.checkPermission
import com.netnovelreader.common.obtainViewModel
import com.netnovelreader.common.toast
import com.netnovelreader.data.local.PreferenceManager
import com.netnovelreader.databinding.ActivityShelfBinding
import com.netnovelreader.ui.fragment.NovelClassfyFragment
import com.netnovelreader.ui.fragment.ShelfFragment
import com.netnovelreader.viewmodel.ShelfViewModel
import kotlinx.android.synthetic.main.activity_shelf.*
import kotlinx.coroutines.experimental.launch

class ShelfActivity : AppCompatActivity() {

    private lateinit var viewModel: ShelfViewModel
    private var hasPermission = false
    private var themeId = 0
    private var translationTemp = 0F

    override fun onCreate(savedInstanceState: Bundle?) {
        themeId = PreferenceManager.getThemeId(this).also { setTheme(it) }
        super.onCreate(savedInstanceState)
        viewModel =  obtainViewModel(ShelfViewModel::class.java)
        hasPermission = checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (!hasPermission) {
            requirePermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, 1)
        }
        initView()
        initLiveData()
        shelfTab.post { viewModel.tabHeight = shelfTab.height }
    }

    fun initView() {
        DataBindingUtil.setContentView<ActivityShelfBinding>(this, R.layout.activity_shelf)
                .apply { viewModel = this@ShelfActivity.viewModel }
        setSupportActionBar(shelfToolbar)
        shelfViewPager.offscreenPageLimit = 2
        shelfViewPager.adapter = PagerAdapter(
                supportFragmentManager,
                arrayOf(getString(R.string.shelf), getString(R.string.classification)),
                arrayOf(ShelfFragment::class.java, NovelClassfyFragment::class.java)
        )
        shelfTab.setupWithViewPager(shelfViewPager)
    }

    fun initLiveData() {
        viewModel.translateCommand.observe(this, Observer {
            if (it == null || shelfTab.height == 0) return@Observer
            if (it[1] == RecyclerView.SCROLL_STATE_IDLE) {
                mtoolbar.animate().translationY(-it[0].toFloat()).setDuration(300L)
                        .setInterpolator(DecelerateInterpolator(2F)).start()
            } else {
                mtoolbar.translationY = -it[0].toFloat()
            }
            translationTemp = -it[0].toFloat()
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_shelf, menu)
        viewModel.isLoginItemShow().also {
            menu.findItem(R.id.login).setVisible(it)
            menu.findItem(R.id.syncRecord).setVisible(!it)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.search_button -> {
                val intent = Intent(this, SearchActivity::class.java)
                        .apply { putExtra("themeid", themeId) }
                startActivity(intent)
                viewModel.refreshType = 2
                true
            }
            R.id.action_settings -> {
                startSettingActivity(0)
                true
            }
            R.id.edit_site_preference -> {
                startSettingActivity(1)
                true
            }
            R.id.login -> {
                startSettingActivity(2)
                true
            }
            R.id.syncRecord -> {
                startSettingActivity(3)
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
                launch { viewModel.refreshBookList() }
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

    private fun startSettingActivity(type: Int){
        val intent = Intent(this, SettingActivity::class.java).apply {
            putExtra("type", type)
            putExtra("themeid", themeId)
        }
        startActivity(intent)
    }
}
