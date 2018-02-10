package com.netnovelreader.shelf

import android.app.AlertDialog
import android.app.Dialog
import android.app.Fragment
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
import com.netnovelreader.editor.SiteEditorActivity
import com.netnovelreader.reader.ReaderActivity
import com.netnovelreader.search.SearchActivity
import kotlinx.android.synthetic.main.activity_shelf.*
import kotlinx.android.synthetic.main.item_shelf.view.*
import kotlinx.coroutines.experimental.launch

class ShelfActivity : AppCompatActivity(), IShelfContract.IShelfView {

    var shelfViewModel: ShelfViewModel? = null
    private var arrayListChangeListener: ArrayListChangeListener<BookBean>? = null
    private var hasPermission = false
    private var isFragmentShow = false
    private var settingFragment: SettingFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        PreferenceManager.setTheme(this)
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
        setSupportActionBar({ shelfToolbar.setTitle(R.string.shelf_activity_title); shelfToolbar }())
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
                shelfViewModel!!.updateBooks()
            }
            time = System.currentTimeMillis()
            shelf_layout.isRefreshing = false
        }
    }

    override fun onResume() {
        super.onResume()
        shelfViewModel?.bookList?.clear()
        updateShelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        shelfViewModel?.bookList?.removeOnListChangedCallback(arrayListChangeListener)
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
                isFragmentShow = true
                settingFragment = SettingFragment()
                fragmentManager.beginTransaction().replace(R.id.shelfFrameLayout, settingFragment)
                        .commit()
                shelfToolbar.setTitle(R.string.settings)
                shelfToolbar.setNavigationIcon(R.drawable.icon_back)
                shelfToolbar.setNavigationOnClickListener {
                    removeFragment(settingFragment)
                }
                findViewById<View>(R.id.search_button).visibility = View.INVISIBLE
                true
            }
            R.id.edit_site_preference -> {
                startActivity(Intent(this, SiteEditorActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (isFragmentShow) {
            removeFragment(settingFragment)  //退出设置页面
        } else {
            super.onBackPressed()
        }
    }

    fun removeFragment(fragment: Fragment?) {
        fragment ?: return
        fragmentManager.beginTransaction().remove(fragment).commit()
        shelfToolbar.setTitle(R.string.shelf_activity_title)
        shelfToolbar.navigationIcon = null
        findViewById<View>(R.id.search_button).visibility = View.VISIBLE
        isFragmentShow = false
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
            val bookname = v.nameView.text.toString()
            launch { shelfViewModel?.cancelUpdateFlag(bookname) }
            v.context.startActivity(Intent(v.context, ReaderActivity::class.java)
                    .apply { this.putExtra("bookname", bookname) })
        }

        fun itemOnLongClick(view: View): Boolean {
            val listener = DialogInterface.OnClickListener { dialog, which ->
                if (which == Dialog.BUTTON_POSITIVE) {
                    launch {
                        shelfViewModel?.deleteBook(view.nameView.text.toString())
                        shelfViewModel?.refreshBookList()
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
