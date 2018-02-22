package com.netnovelreader.shelf

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
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
import com.netnovelreader.R
import com.netnovelreader.common.ArrayListChangeListener
import com.netnovelreader.common.BindingAdapter
import com.netnovelreader.common.PreferenceManager
import com.netnovelreader.common.base.IClickEvent
import com.netnovelreader.common.data.SQLHelper
import com.netnovelreader.common.toast
import com.netnovelreader.databinding.ActivityShelfBinding
import com.netnovelreader.novelCatagory.CatalogGridActivity
import com.netnovelreader.reader.ReaderActivity
import com.netnovelreader.search.SearchActivity
import com.netnovelreader.setting.SettingActivity
import kotlinx.android.synthetic.main.activity_shelf.*
import kotlinx.android.synthetic.main.item_shelf.view.*
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch

class ShelfActivity : AppCompatActivity(), IShelfContract.IShelfView {

    var shelfViewModel: ShelfViewModel? = null
    private var arrayListChangeListener: ArrayListChangeListener<BookBean>? = null
    private var hasPermission = false
    private var themeId = 0
    var job: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        themeId = PreferenceManager.getThemeId(this).also { setTheme(it) }
        super.onCreate(savedInstanceState)
        setViewModel(ShelfViewModel())
        hasPermission = checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (!hasPermission) {
            requirePermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, 1)
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
        setSupportActionBar(shelfToolbar.apply { setTitle(R.string.shelf_activity_title) })
        shelfRecycler.layoutManager = LinearLayoutManager(this)
        shelfRecycler.itemAnimator = DefaultItemAnimator()
        val mAdapter =
                BindingAdapter(shelfViewModel?.bookList, R.layout.item_shelf, ShelfClickEvent())
        shelfRecycler.adapter = mAdapter
        arrayListChangeListener = ArrayListChangeListener(mAdapter)
        shelfViewModel?.bookList?.addOnListChangedCallback(arrayListChangeListener)
        shelf_layout.setColorSchemeResources(R.color.gray)
        var time = System.currentTimeMillis()
        shelf_layout.setOnRefreshListener {
            if (System.currentTimeMillis() - time > 2000) {
                job = launch { shelfViewModel!!.updateBooks() }
            }
            time = System.currentTimeMillis()
            shelf_layout.isRefreshing = false
        }
    }

    override fun onStart() {
        super.onStart()
        if (themeId != PreferenceManager.getThemeId(this)) {
            val intent = intent
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            finish()
            overridePendingTransition(0, 0)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        shelfViewModel?.bookList?.clear()
        updateShelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
        shelfViewModel?.bookList?.removeOnListChangedCallback(arrayListChangeListener)
        shelfViewModel?.bookList?.forEach { it.bitmap.get()?.recycle() }
        shelfViewModel = null
        SQLHelper.closeDB()
    }

    //刷新书架数据
    override fun updateShelf() {
        launch {
            if (hasPermission) {
                shelfViewModel?.refreshBookList()
            }
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
                startActivity(Intent(this, SettingActivity::class.java))
                true
            }
            R.id.edit_site_preference -> {
                startActivity(Intent(this, SettingActivity::class.java))
                true
            }
            R.id.classfied->{
                startActivity(Intent(this, CatalogGridActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
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
                updateShelf()
            } else {
                toast(getString(R.string.permission_warnning))
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun checkPermission(permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(
                this,
                permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    //请求权限
    override fun requirePermission(permission: String, reqCode: Int) {
        ActivityCompat.requestPermissions(this, Array(1) { permission }, reqCode)
    }

    /**
     * recyclerView item点击事件
     */
    inner class ShelfClickEvent : IClickEvent {
        fun itemOnClick(v: View) {
            job?.cancel()
            val bookname = v.nameView.text.toString()
            launch { shelfViewModel?.cancelUpdateFlag(bookname) }
            v.context.startActivity(Intent(v.context, ReaderActivity::class.java)
                    .apply { this.putExtra("bookname", bookname) })
        }

        fun itemOnLongClick(view: View): Boolean {
            val listener = DialogInterface.OnClickListener { _, which ->
                if (which == Dialog.BUTTON_POSITIVE) {
                    launch {
                        shelfViewModel?.deleteBook(view.nameView.text.toString())
                    }
                }
            }
            AlertDialog.Builder(this@ShelfActivity)
                    .setTitle(getString(R.string.deleteBook).replace("book", view.nameView.text.toString()))
                    .setPositiveButton(R.string.yes, listener)
                    .setNegativeButton(R.string.no, listener)
                    .create()
                    .show()
            return true
        }
    }
}
