package com.netnovelreader.ui.activity

import android.app.AlertDialog
import android.arch.lifecycle.Observer
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.databinding.DataBindingUtil
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import com.netnovelreader.R
import com.netnovelreader.common.*
import com.netnovelreader.databinding.ActivityReaderBinding
import com.netnovelreader.viewmodel.ReaderViewModel
import kotlinx.android.synthetic.main.activity_reader.*
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking


class ReaderActivity : AppCompatActivity() {
    val readerViewModel by lazy { obtainViewModel(ReaderViewModel::class.java) }
    var dialog: AlertDialog? = null
    var netStateReceiver: NetChangeReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        if (PreferenceManager.isFullScreen(this)) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        PreferenceManager.getThemeId(this).also { setTheme(it) }
        super.onCreate(savedInstanceState)
        initView()
        initLiveData()
    }

    fun initView() {
        readerViewModel.bookName = intent.getStringExtra("bookname")
        readerViewModel.CACHE_NUM = PreferenceManager.getAutoDownNum(this@ReaderActivity)
        DataBindingUtil.setContentView<ActivityReaderBinding>(this, R.layout.activity_reader)
                .apply { viewModel = readerViewModel }
        netStateReceiver = NetChangeReceiver()
        val filter = IntentFilter().apply { addAction(ConnectivityManager.CONNECTIVITY_ACTION) }
        registerReceiver(netStateReceiver, filter)  //网络变化广播接收器

        getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).apply {
            readerViewModel.fontSizeChangeEvent(getFloat(getString(R.string.fontSizeKey), 50f))
            readerViewModel.fontTypeChangeEvent(
                getString(
                    getString(R.string.fontTypeKey),
                    "default"
                )
            )
            readerViewModel.backgroundChangeEvent(
                getInt(
                    getString(R.string.backgroundColorKey),
                    0
                )
            )
        }

        sb_brightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val attrs = window.attributes
                attrs.screenBrightness = (if (progress < 1) 1 else if (progress > 255) 255 else progress) /
                        255f
                window.attributes = attrs
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })
    }

    fun initLiveData() {
        readerViewModel.showDialogCommand.observe(this, Observer {
            if (it == true) showDialog() else dialog?.dismiss()
        })
        readerViewModel.changeSourceCommand.observe(this, Observer {
            val intent = Intent(this@ReaderActivity, SearchActivity::class.java).apply {
                putExtra("bookname", intent.getStringExtra("bookname"))
                putExtra("chapterName", it)
            }
            startActivityForResult(intent, 1)
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1 && requestCode == 100){
            launch { readerViewModel.reloadCurrentChapter() }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(netStateReceiver)
        launch { readerViewModel.autoRemove() }
    }

    override fun onBackPressed() {
        if (footView.visibility == View.VISIBLE) {
            headerView.visibility = View.INVISIBLE
            footView.visibility = View.INVISIBLE
            fontSetting.visibility = View.INVISIBLE
            backgroundSetting.visibility = View.INVISIBLE
        } else {
            super.onBackPressed()
        }
    }

    //显示目录
    fun showDialog() = runBlocking {
        var catalogView: RecyclerView? = null
        if (dialog == null) {
            val builder = AlertDialog.Builder(this@ReaderActivity)
            catalogView = RecyclerView(this@ReaderActivity)
            catalogView.init(
                RecyclerAdapter(readerViewModel.catalog, R.layout.item_catalog, readerViewModel)
            )
            dialog = builder.setView(catalogView).create()
        }
        catalogView?.scrollToPosition(readerViewModel.chapterNum.get() - 1)
        dialog?.show()
        dialog?.window?.setLayout(readerView.width * 5 / 6, readerView.height * 9 / 10)
        Unit
    }

    //网络变化广播接收器
    inner class NetChangeReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val isAvailable = cm.activeNetworkInfo?.isAvailable ?: false
            if (isAvailable && readerViewModel.isLoading.get() != false) {   //当网络变为连接状态，并且加载条显示时，下载章节内容
                launch { readerViewModel.downloadAndShow() }
            }
        }
    }
}
