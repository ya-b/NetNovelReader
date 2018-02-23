package com.netnovelreader.ui

import android.app.AlertDialog
import android.databinding.DataBindingUtil
import android.databinding.ObservableArrayList
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import com.netnovelreader.R
import com.netnovelreader.bean.FilterBean
import com.netnovelreader.common.CatalogPagerAdapter
import com.netnovelreader.common.PreferenceManager
import com.netnovelreader.common.RecyclerAdapter
import com.netnovelreader.common.init
import com.netnovelreader.databinding.ActivityCatalogDetailBinding
import com.netnovelreader.interfaces.IClickEvent
import kotlinx.android.synthetic.main.activity_catalog_detail.*


/**
 * 文件： NovelCatalogDetailActivity
 * 描述：
 * 作者： YangJunQuan   2018-2-11.
 */
class NovelCatalogDetailActivity : AppCompatActivity() {
    private var dialog: AlertDialog? = null               //筛选小说用的Dialog
    private var filterList = ObservableArrayList<FilterBean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        PreferenceManager.getThemeId(this).also { setTheme(it) }
        super.onCreate(savedInstanceState)

        DataBindingUtil.setContentView<ActivityCatalogDetailBinding>(
                this,
                R.layout.activity_catalog_detail
        )
        setSupportActionBar({ toolbar.title = intent.getStringExtra("major");toolbar }())
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        initView()
    }

    private fun initView() {
        viewPager.offscreenPageLimit =
                4                  //一次性初始化typeList-1+1页，所以初始化时间比较久，但是随后的切换不会卡顿因为都已经初始化完毕了
        viewPager.adapter = CatalogPagerAdapter(
                supportFragmentManager,
                intent.getStringExtra("major")
        )
        tabLayout.setupWithViewPager(viewPager)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            android.R.id.home -> finish()     //点击回退按钮结束当前Activity
            R.id.filterNovel -> showDialog()  //进一步筛选小说
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_catalog, menu)
        return true
    }


    private fun showDialog() {
        if (dialog == null) {
            filterList.add(FilterBean(ObservableField("同人小说"), ObservableBoolean(true)))
            filterList.add(FilterBean(ObservableField("同人影视"), ObservableBoolean(false)))
            filterList.add(FilterBean(ObservableField("同人电视剧"), ObservableBoolean(false)))
            filterList.add(FilterBean(ObservableField("同人？？？"), ObservableBoolean(false)))
            val builder = AlertDialog.Builder(this)
            val filterView = RecyclerView(this)
            filterView.init(
                    RecyclerAdapter(filterList, R.layout.item_filter, FilterNovelItemClickListener())
            )
            dialog = builder.setView(filterView).create()
            val dialogWindow = dialog!!.window
            dialogWindow.setGravity(Gravity.CENTER)
        }
        dialog?.show()
    }

    inner class FilterNovelItemClickListener : IClickEvent {
        fun onItemClick(bean: FilterBean) {
            filterList.firstOrNull { it.selected.get() == true }?.selected?.set(false)
            bean.selected.set(true)
            dialog?.dismiss()
        }
    }
}