package com.netnovelreader.ui.activity

import android.arch.lifecycle.Observer
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.netnovelreader.R
import com.netnovelreader.common.CatalogPagerAdapter
import com.netnovelreader.common.obtainViewModel
import com.netnovelreader.common.toast
import com.netnovelreader.databinding.ActivityCatalogDetailBinding
import com.netnovelreader.viewmodel.CategoryDetailViewModel
import kotlinx.android.synthetic.main.activity_catalog_detail.*


/**
 * 文件： CategoryDetailActivity
 * 描述：
 * 作者： YangJunQuan   2018-2-11.
 */
class CategoryDetailActivity : AppCompatActivity() {
    private val viewModel by lazy { obtainViewModel(CategoryDetailViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(intent.getIntExtra("themeid", R.style.AppThemeBlack))
        super.onCreate(savedInstanceState)
        initView()
        initLiveData()
    }

    private fun initView() {
        val major = intent.getStringExtra("major")
        DataBindingUtil.setContentView<ActivityCatalogDetailBinding>(
            this,
            R.layout.activity_catalog_detail
        )
        setSupportActionBar(toolbar.apply { title = major })
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        viewPager.offscreenPageLimit = 2
        viewPager.adapter = CatalogPagerAdapter(supportFragmentManager, major)
        tabLayout.setupWithViewPager(viewPager)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()     //点击回退按钮结束当前Activity
        }
        return true
    }

    private fun initLiveData() {
        viewModel.toastMessage.observe(this, Observer { it?.let { toast(it) } })
        viewModel.showBookDetailCommand.observe(this, Observer {
            val intent = Intent(this, NovelDetailActivity::class.java).apply {
                putExtra("data", it)
                putExtra("themeid", intent.getIntExtra("themeid", R.style.AppThemeBlack))
            }
            startActivityForResult(intent, 233)
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == 2333) {
            setResult(10000)
            this.finish()
        }
    }
}