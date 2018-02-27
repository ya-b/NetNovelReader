package com.netnovelreader.ui

import android.app.AlertDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.netnovelreader.R
import com.netnovelreader.common.PreferenceManager
import com.netnovelreader.common.RecyclerAdapter
import com.netnovelreader.common.init
import com.netnovelreader.common.toast
import com.netnovelreader.databinding.ActivityShelfBinding
import com.netnovelreader.viewmodel.ShelfViewModel
import kotlinx.android.synthetic.main.activity_shelf.*
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch

class ShelfActivity : AppCompatActivity() {

    var shelfViewModel: ShelfViewModel? = null
    private var hasPermission = false
    private var themeId = 0
    var job: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        themeId = PreferenceManager.getThemeId(this).also { setTheme(it) }
        super.onCreate(savedInstanceState)
        hasPermission = checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (!hasPermission) {
            requirePermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, 1)
        }
        initViewModel()
        initView()
        initLiveData()
    }

    /**
     * DataBinding绑定
     */
    fun initViewModel() {
        val factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        shelfViewModel = ViewModelProviders.of(this, factory).get(ShelfViewModel::class.java)
        DataBindingUtil.setContentView<ActivityShelfBinding>(this, R.layout.activity_shelf)
    }

    fun initView() {
        setSupportActionBar(shelfToolbar)
        shelfRecycler.init(
                RecyclerAdapter(shelfViewModel?.bookList, R.layout.item_shelf, shelfViewModel),
                null
        )
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

    fun initLiveData() {
        shelfViewModel?.readBookCommand?.observe(this, Observer {
            startActivity(Intent(this, ReaderActivity::class.java)
                    .apply { this.putExtra("bookname", it) })
        })
        shelfViewModel?.showDialogCommand?.observe(this, Observer {
            AlertDialog.Builder(this@ShelfActivity)
                    .setTitle(getString(R.string.deleteBook).replace("book", it!!))
                    .setPositiveButton(R.string.yes, { _, _ -> launch { shelfViewModel?.deleteBook(it) } })
                    .setNegativeButton(R.string.no, null)
                    .create()
                    .show()
        })
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
        launch { if (hasPermission) shelfViewModel?.refreshBookList() }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
        (shelfRecycler.adapter as RecyclerAdapter<Any, Any>).removeDataChangeListener()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_shelf, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.search_button -> {
                startActivity(Intent(this, SearchActivity::class.java).apply {
                    this.putExtra("type", 0)
                })
                true
            }
            R.id.action_settings -> {
                startActivity(Intent(this, SettingActivity::class.java))
                true
            }
            R.id.edit_site_preference -> {
                startActivity(Intent(this, SettingActivity::class.java).apply {
                    this.putExtra("type", 1)
                })
                true
            }
            R.id.classfied -> {
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
                launch { shelfViewModel?.refreshBookList() }
            } else {
                toast(getString(R.string.permission_warnning))
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun checkPermission(permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(
                this,
                permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    //请求权限
    fun requirePermission(permission: String, reqCode: Int) {
        ActivityCompat.requestPermissions(this, Array(1) { permission }, reqCode)
    }
}
