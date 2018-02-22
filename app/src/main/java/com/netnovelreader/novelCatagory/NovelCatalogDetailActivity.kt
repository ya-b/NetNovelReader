package com.netnovelreader.novelCatagory

import android.app.AlertDialog
import android.databinding.DataBindingUtil
import android.databinding.ObservableArrayList
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.TextView
import com.netnovelreader.R
import com.netnovelreader.common.BindingAdapter
import com.netnovelreader.common.PreferenceManager
import com.netnovelreader.common.base.IClickEvent
import com.netnovelreader.databinding.ActivityCatalogDetailBinding
import kotlinx.android.synthetic.main.activity_catalog_detail.*


/**
 * 文件： NovelCatalogDetailActivity
 * 描述：
 * 作者： YangJunQuan   2018-2-11.
 */
class NovelCatalogDetailActivity : AppCompatActivity() {
    private var dialog: AlertDialog? = null               //筛选小说用的Dialog
    private var filterList = ObservableArrayList<FilterBean>()
    private var filterAdapter: BindingAdapter<FilterBean>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        PreferenceManager.getThemeId(this).also { setTheme(it) }
        super.onCreate(savedInstanceState)


        DataBindingUtil.setContentView<ActivityCatalogDetailBinding>(this, R.layout.activity_catalog_detail)
        setSupportActionBar({ toolbar.title = intent.getStringExtra("major");toolbar }())
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        initView()
    }

    private fun initView() {
        viewPager.offscreenPageLimit = 4                  //一次性初始化typeList-1+1页，所以初始化时间比较久，但是随后的切换不会卡顿因为都已经初始化完毕了
        viewPager.adapter = CatalogPagerAdapter(supportFragmentManager, intent.getStringExtra("major"))
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
        var filterView: RecyclerView? = null
        if (dialog == null) {
            filterList.add(FilterBean("同人小说", true))
            filterList.add(FilterBean("同人影视", false))
            filterList.add(FilterBean("同人电视剧", false))
            filterList.add(FilterBean("同人？？？", false))
            val builder = AlertDialog.Builder(this)
            val view = LayoutInflater.from(this).inflate(R.layout.dialog_catalog, null)
            filterView = view.findViewById(R.id.catalogView)
            filterView.layoutManager = LinearLayoutManager(this)
            filterView.itemAnimator = DefaultItemAnimator()
            filterAdapter = BindingAdapter(filterList, R.layout.item_filter, FilterNovelItemClickListener())
            filterView.adapter = filterAdapter
            dialog = builder.setView(view).create()
            val dialogWindow = dialog!!.window
            dialogWindow.setGravity(Gravity.CENTER)
        }
        dialog?.show()
    }


    inner class FilterNovelItemClickListener : IClickEvent {
        fun onItemClick(v: View) {
            val filterName = v.findViewById<TextView>(R.id.filterName).text.toString()
            filterList.firstOrNull { it.selected == true }?.selected = false
            filterList.firstOrNull { it.minorType == filterName }?.selected = true
            filterAdapter?.notifyDataSetChanged()
            dialog?.dismiss()
        }
    }
}