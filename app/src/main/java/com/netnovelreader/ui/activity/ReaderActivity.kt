package com.netnovelreader.ui.activity

import android.app.AlertDialog
import android.arch.lifecycle.Observer
import android.content.Intent
import android.content.IntentFilter
import android.databinding.DataBindingUtil
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.WindowManager
import com.netnovelreader.R
import com.netnovelreader.common.RecyclerAdapter
import com.netnovelreader.common.init
import com.netnovelreader.common.obtainViewModel
import com.netnovelreader.data.local.PreferenceManager
import com.netnovelreader.databinding.ActivityReaderBinding
import com.netnovelreader.receiver.NetStateChangedReceiver
import com.netnovelreader.viewmodel.ReaderViewModel
import kotlinx.android.synthetic.main.activity_reader.*
import kotlinx.coroutines.experimental.launch


class ReaderActivity : AppCompatActivity() {
    private val viewModel by lazy { obtainViewModel(ReaderViewModel::class.java) }
    private var dialog: AlertDialog? = null
    private var netStateReceiver: NetStateChangedReceiver? = null
    private var catalogView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        if (PreferenceManager.isFullScreen(this)) {
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        setTheme(intent.getIntExtra("themeid", R.style.AppThemeBlack))
        super.onCreate(savedInstanceState)
        initView()
        launch { initData() }
        initLiveData()
    }

    fun initView() {
        DataBindingUtil.setContentView<ActivityReaderBinding>(this, R.layout.activity_reader)
                .apply { viewModel = this@ReaderActivity.viewModel }
    }

    fun initData() {
        viewModel.bookName = intent.getStringExtra("bookname")
        viewModel.CACHE_NUM = PreferenceManager.getAutoDownNum(this@ReaderActivity)
        viewModel.start()
        netStateReceiver = NetStateChangedReceiver {
            if (viewModel.isLoading.get()) {
                viewModel.downloadJob?.cancel()
                viewModel.downloadJob = launch { viewModel.downloadAndShow() }
            }
        }
        val filter = IntentFilter().apply { addAction(ConnectivityManager.CONNECTIVITY_ACTION) }
        registerReceiver(netStateReceiver, filter)  //网络变化广播接收器
    }

    fun initLiveData() {
        viewModel.brightnessCommand.observe(this, Observer {
            it ?: return@Observer
            window.attributes = window.attributes.apply { screenBrightness = it }
        })
        viewModel.showDialogCommand.observe(this, Observer {
            if (it == true) showDialog() else dialog?.dismiss()
        })
        viewModel.changeSourceCommand.observe(this, Observer {
            val intent = Intent(this@ReaderActivity, SearchActivity::class.java).apply {
                putExtra("bookname", intent.getStringExtra("bookname"))
                putExtra("chapterName", it)
                putExtra("themeid", intent.getIntExtra("themeid", R.style.AppThemeBlack))
            }
            startActivityForResult(intent, 1)
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == 100) {
            launch { viewModel.reloadCurrentChapter() }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(netStateReceiver)
        catalogView?.adapter = null
        launch { viewModel.autoRemove() }
    }

    override fun onBackPressed() {
        viewModel.apply {
            if (isViewShow[ReaderViewModel.FOOT_VIEW]?.get() == true) {
                isViewShow.forEach { it.value.set(false) }
            } else {
                super.onBackPressed()
            }
        }
    }

    //显示目录
    fun showDialog() {
        if (dialog == null) {
            val builder = AlertDialog.Builder(this@ReaderActivity)
            catalogView = RecyclerView(this@ReaderActivity)
            catalogView?.init(
                    RecyclerAdapter(
                            viewModel.catalog, R.layout.item_catalog, viewModel, true
                    )
            )
            dialog = builder.setView(catalogView).create()
        }
        catalogView?.scrollToPosition(viewModel.chapterNum.get())
        dialog?.show()
        dialog?.window?.setLayout(readerView.width * 5 / 6, readerView.height * 9 / 10)
    }
}
