package com.netnovelreader.ui

import android.app.AlertDialog
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.TextView
import com.netnovelreader.R
import com.netnovelreader.common.*
import com.netnovelreader.customview.ReaderView
import com.netnovelreader.databinding.ActivityReaderBinding
import com.netnovelreader.interfaces.IClickEvent
import com.netnovelreader.interfaces.IReaderContract
import com.netnovelreader.viewmodel.ReaderViewModel
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_reader.*
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking


class ReaderActivity : AppCompatActivity(), IReaderContract.IReaderView,
        ReaderView.ReaderPageListener, ReaderView.FirstDrawListener, IClickEvent {
    val FONTSIZE = "FontSize"
    var readerViewModel: ReaderViewModel? = null
    var dialog: AlertDialog? = null
    var netStateReceiver: NetChangeReceiver? = null
    var isInit = false

    override fun onCreate(savedInstanceState: Bundle?) {
        if (PreferenceManager.isFullScreen(this)) {
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        PreferenceManager.getThemeId(this).also { setTheme(it) }
        super.onCreate(savedInstanceState)
        initViewModel()
        initView()
    }

    override fun initViewModel() {
        val factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        readerViewModel = ViewModelProviders.of(this, factory).get(ReaderViewModel::class.java)
        val binding =
                DataBindingUtil.setContentView<ActivityReaderBinding>(this, R.layout.activity_reader)
        binding.clickEvent = ReaderClickEvent()
        binding.readerViewModel = readerViewModel
        binding.readerView.firstDrawListener = this
        binding.readerView.pageListener = this
        binding.readerView.txtFontSize = getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
                .getFloat(FONTSIZE, 50f)
    }

    override fun initView() {
        netStateReceiver = NetChangeReceiver()
        val filter = IntentFilter().apply { addAction(ConnectivityManager.CONNECTIVITY_ACTION) }
        registerReceiver(netStateReceiver, filter)  //网络变化广播接收器

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

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(netStateReceiver)
        if (PreferenceManager.isAutoRemove(this)) {
            launch { readerViewModel?.autoRemove() }
        }
    }

    override fun onBackPressed() {
        if (footView.visibility == View.VISIBLE) {
            hideHeadFoot()
        } else {
            super.onBackPressed()
        }
    }

    /**
     * readerview第一次绘制时调用
     */
    override fun doDrawPrepare() {
        launch {
            readerView.pageNum = async {
                readerViewModel?.initData(
                        intent.getStringExtra("bookname"),
                        PreferenceManager.getAutoDownNum(this@ReaderActivity)
                )
            }.await()
            readerViewModel?.getChapter(ReaderViewModel.CHAPTERCHANGE.BY_CATALOG, null)
            isInit = true
        }
    }

    //点击屏幕中间
    override fun onCenterClick() {
        if (hideHeadFoot()) return
        footView.visibility = View.VISIBLE
        headerView.visibility = View.VISIBLE
    }

    override fun nextChapter() {
        hideHeadFoot()
        launch {
            readerViewModel?.getChapter(ReaderViewModel.CHAPTERCHANGE.NEXT, null)
        }
    }

    override fun previousChapter() {
        hideHeadFoot()
        launch {
            readerViewModel?.getChapter(ReaderViewModel.CHAPTERCHANGE.PREVIOUS, null)
        }
    }

    override fun onPageChange() {
        hideHeadFoot()
        launch {
            readerViewModel?.setRecord(readerViewModel?.chapterNum ?: 1, readerView.pageNum ?: 1)
        }
    }

    //显示目录
    override fun showDialog() = runBlocking {
        var catalogView: RecyclerView? = null
        if (dialog == null) {
            val builder = AlertDialog.Builder(this@ReaderActivity)
            catalogView = RecyclerView(this@ReaderActivity)
            catalogView.init(
                    RecyclerAdapter(readerViewModel?.catalog, R.layout.item_catalog, CatalogItemClickListener())
            )
            dialog = builder.setView(catalogView).create()
        }
        launch { readerViewModel?.getCatalog() }.join()
        catalogView?.scrollToPosition(readerViewModel!!.chapterNum - 1)
        dialog?.show()
        dialog?.window?.setLayout(readerView.width * 5 / 6, readerView.height * 9 / 10)
        Unit
    }

    private fun hideHeadFoot(): Boolean {
        return (footView.visibility == View.VISIBLE).apply {
            headerView.visibility = View.INVISIBLE
            footView.visibility = View.INVISIBLE
            fontSetting.visibility = View.INVISIBLE
            backgroundSetting.visibility = View.INVISIBLE
        }
    }

    //网络变化广播接收器
    inner class NetChangeReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val isAvailable = cm.activeNetworkInfo?.isAvailable ?: false
            if (isAvailable && isInit && readerViewModel?.isLoading?.get() != false) {   //当网络变为连接状态，并且加载条显示时，下载章节内容
                launch {
                    readerViewModel!!.downloadAndShow()
                    readerViewModel?.setRecord(
                            readerViewModel?.chapterNum ?: 1,
                            readerView.pageNum ?: 1
                    )
                }
            }
        }
    }

    //点击目录，跳转章节
    inner class CatalogItemClickListener : IClickEvent {
        fun onChapterClick(chapterName: String?) {
            launch {
                readerViewModel?.getChapter(ReaderViewModel.CHAPTERCHANGE.BY_CATALOG, chapterName)
                readerViewModel?.setRecord(
                        readerViewModel?.chapterNum ?: 1,
                        readerView.pageNum ?: 1
                )
            }
            dialog?.dismiss()
        }
    }

    inner class ReaderClickEvent : IClickEvent {

        private var selectedBgImageView: CircleImageView? = null       //用于记录用户当前选中的背景样式的UI控件
        private var selectedFontTypeTextView: TextView? = null             //用于记录用户当前选中的字体样式的UI控件
        private var selectedFontSizeTextView: TextView? = null             //用于记录用户当前选中的字体样式的UI控件

        //换源下载
        fun onHeadViewClick(v: View) {
            when (v) {
                changeSouce -> {
                    headerView.visibility = View.INVISIBLE
                    footView.visibility = View.INVISIBLE
                    fontSetting.visibility = View.INVISIBLE
                    val mIntent = Intent(this@ReaderActivity, SearchActivity::class.java)
                    mIntent.putExtra("bookname", intent.getStringExtra("bookname"))
                    mIntent.putExtra("chapterName", readerViewModel?.chapterName)
                    startActivity(mIntent)
                }
            }
        }

        fun onFootViewClick(v: View) {
            when (v) {
                catalogButton -> {
                    headerView.visibility = View.INVISIBLE
                    footView.visibility = View.INVISIBLE
                    fontSetting.visibility = View.INVISIBLE
                    backgroundSetting.visibility = View.INVISIBLE
                    showDialog()
                }
                fontSizeButton -> {
                    backgroundSetting.visibility = View.INVISIBLE
                    if (fontSetting.visibility == View.VISIBLE) {
                        fontSetting.visibility = View.INVISIBLE
                    } else {
                        fontSetting.visibility = View.VISIBLE
                    }
                }
                backgroundButton -> {
                    fontSetting.visibility = View.INVISIBLE
                    if (backgroundSetting.visibility == View.VISIBLE) {
                        backgroundSetting.visibility = View.INVISIBLE
                    } else {
                        backgroundSetting.visibility = View.VISIBLE
                    }
                }
            }
        }

        fun onFontSizeClick(v: View) {
            readerView.txtFontSize = (v as TextView).text.toString().toFloat()
            getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).edit()
                    .putFloat(FONTSIZE, readerView.txtFontSize!!).apply()

            v.setTextColor(
                    ContextCompat.getColor(
                            this@ReaderActivity,
                            R.color.lightgray
                    )
            )//为当前选中的字体TextView改变背景色
            selectedFontSizeTextView?.setTextColor(Color.WHITE)                                  //将前次选中的字体大小TextView改变背景色
            selectedFontSizeTextView = v
        }

        //改变阅读界面字体类型
        fun onFontTypeClick(view: View) {
            view as TextView
            val context = view.context
            val typeFacePath =      //根据用户选择获取字体路径
                    when (view.id) {
                        R.id.tv_beiweikai -> FontConfig.FONTTYPE_BEIWEIKAISHU
                        R.id.tv_bysong -> FontConfig.FONTTYPE_BYSONG
                        R.id.tv_default -> FontConfig.FONTTYPE_DEFAULT
                        R.id.tv_zhulang -> FontConfig.FONTTYPE_CHENGUANG
                        R.id.tv_fzkatong -> FontConfig.FONTTYPE_FZKATONG
                        else -> FontConfig.FONTTYPE_DEFAULT
                    }

            readerView.txtFontType = FontConfig.getTypeface(
                    context,
                    typeFacePath
            )   //根据新的字体重新绘制视图
            selectedFontTypeTextView?.let {
                FontConfig.setTextViewSelect(
                        it,
                        false
                )                       //将前次选中的字体TextView改变背景色
            }
            selectedFontTypeTextView = view
            FontConfig.setTextViewSelect(
                    view,
                    true
            )                           //为当前选中的字体TextView改变背景色

        }

        //改变阅读界面背景色
        fun itemChangeBgClick(view: View) {

            selectedBgImageView?.borderWidth = 0   //首先将之前选中的CircleImageView边框去掉
            view as CircleImageView
            view.borderColor = Color.RED           //然后为当前选中的CircleImageView添加边框
            view.borderWidth = 3
            selectedBgImageView = view
            when (view.id) {
                R.id.read_bg_default -> {
                    readerView.txtFontColorId = R.color.read_font_default       //设置默认字体颜色
                    readerView.bgColorId = R.color.read_bg_default              //设置阅读界面默认背景颜色
                }
                R.id.read_bg_1 -> {
                    readerView.txtFontColorId = R.color.read_font_1
                    readerView.bgColorId = R.color.read_bg_1
                }
                R.id.read_bg_2 -> {
                    readerView.txtFontColorId = R.color.read_font_2
                    readerView.bgColorId = R.color.read_bg_2
                }
                R.id.read_bg_3 -> {
                    readerView.txtFontColorId = R.color.read_font_3
                    readerView.bgColorId = R.color.read_bg_3
                }
                R.id.read_bg_4 -> {
                    readerView.txtFontColorId = R.color.read_font_4
                    readerView.bgColorId = R.color.read_bg_4
                }
            }
        }
    }
}
