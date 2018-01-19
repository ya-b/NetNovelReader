package com.netnovelreader.shelf

import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.netnovelreader.R
import com.netnovelreader.base.IClickEvent
import com.netnovelreader.common.ArrayListChangeListener
import com.netnovelreader.common.BindingAdapter
import com.netnovelreader.common.NovelItemDecoration
import com.netnovelreader.common.id2TableName
import com.netnovelreader.databinding.ActivityShelfBinding
import com.netnovelreader.reader.ReaderActivity
import com.netnovelreader.search.SearchActivity
import com.netnovelreader.service.DownloadService
import kotlinx.android.synthetic.main.activity_shelf.*
import kotlinx.android.synthetic.main.item_shelf.view.*

class ShelfActivity : AppCompatActivity(), IShelfContract.IShelfView {

    var shelfViewModel: ShelfViewModel? = null
    var arrayListChangeListener: ArrayListChangeListener<ShelfBean>? = null
    var hasPermission = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setViewModel(ShelfViewModel())
        hasPermission = checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (!hasPermission) {
            requirePermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, 1)
        }
        if (!checkPermission(android.Manifest.permission.INTERNET)) {
            requirePermission(android.Manifest.permission.INTERNET, 2)
        }
        init()
    }

    /**
     * DataBinding绑定
     */
    override fun setViewModel(vm: ShelfViewModel) {
        shelfViewModel = vm
        DataBindingUtil.setContentView<ActivityShelfBinding>(this, R.layout.activity_shelf)
    }

    override fun init() {
        setSupportActionBar({ shelfToolbar.setTitle(R.string.shelf_activity_title); shelfToolbar }())
        shelfRecycler.layoutManager = LinearLayoutManager(this)
        shelfRecycler.addItemDecoration(NovelItemDecoration(this))
        shelfRecycler.setItemAnimator(DefaultItemAnimator())
        val mAdapter = BindingAdapter(shelfViewModel?.bookList, R.layout.item_shelf, ShelfClickEvent())
        shelfRecycler.adapter = mAdapter
        arrayListChangeListener = ArrayListChangeListener(mAdapter)
        shelfViewModel?.bookList?.addOnListChangedCallback(arrayListChangeListener)
        shelf_layout.setColorSchemeResources(R.color.colorPrimary)
        var time = System.currentTimeMillis()
        shelf_layout.setOnRefreshListener {
            if (System.currentTimeMillis() - time > 2000) {
//                shelfViewModel!!.updateBooks()
                shelfViewModel!!.bookList.forEach {
                    val intent = Intent(this, DownloadService::class.java)
                    intent.putExtra("tableName", id2TableName(it.bookid.get()))
                    intent.putExtra("catalogurl", it.downloadURL.get())
                    startService(intent)
                }
            }
            time = System.currentTimeMillis()
            shelf_layout.isRefreshing = false
        }
    }

    override fun onResume() {
        super.onResume()
        updateShelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        shelfViewModel?.bookList?.removeOnListChangedCallback(arrayListChangeListener)
        shelfViewModel = null
    }

    //刷新书架数据
    override fun updateShelf() {
        if (hasPermission) {
            shelfViewModel?.refreshBookList()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_shelf, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.search_button -> {
                startActivity(Intent(this, SearchActivity::class.java))
                true
            }
            R.id.action_settings -> {
//                val settingFragment = ShelfSettingFragment()
//                fragmentManager.beginTransaction().replace(R.id.settingLayout, settingFragment).commit()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * 请求权限
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hasPermission = true
                updateShelf()
            } else {
                Toast.makeText(this, R.string.permission_warnning, Toast.LENGTH_LONG).show()
            }
        } else if (requestCode == 2 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, R.string.permission_warnning, Toast.LENGTH_LONG).show()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun checkPermission(permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    override fun requirePermission(permission: String, reqCode: Int) {
        ActivityCompat.requestPermissions(this, Array(1) { permission }, reqCode)
    }

    /**
     * recyclerView item点击事件
     */
    inner class ShelfClickEvent : IClickEvent {
        fun itemOnClick(v: View) {
            val intent = Intent(v.context, ReaderActivity::class.java)
            intent.putExtra("bookname", v.nameView.text.toString())
            v.context.startActivity(intent)
        }

        fun itemOnLongClick(view: View): Boolean {
            Toast.makeText(view.context, "删除${view.nameView.text}", Toast.LENGTH_SHORT).show()
            shelfViewModel?.deleteBook(view.nameView.text.toString())
            shelfViewModel?.refreshBookList()
            return true
        }
    }
}
