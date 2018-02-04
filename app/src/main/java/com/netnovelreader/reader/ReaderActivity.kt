package com.netnovelreader.reader

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.TextView
import com.netnovelreader.R
import com.netnovelreader.base.IClickEvent
import com.netnovelreader.common.*
import com.netnovelreader.databinding.ActivityReaderBinding
import com.netnovelreader.download.ChapterCache
import com.netnovelreader.search.SearchActivity
import de.hdodenhof.circleimageview.CircleImageView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_reader.*
import kotlinx.android.synthetic.main.item_catalog.view.*
import java.util.*


class ReaderActivity : AppCompatActivity(), IReaderContract.IReaderView,
    ReaderView.ReaderPageListener, ReaderView.FirstDrawListener, IClickEvent {
    val FONTSIZE = "FontSize"
    var readerViewModel: ReaderViewModel? = null
    var dialog: AlertDialog? = null
    var netStateReceiver: NetChangeReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        if (ApplyPreference.isFullScreen(this)) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        ApplyPreference.setTheme(this)
        super.onCreate(savedInstanceState)
        setViewModel(
            ReaderViewModel(
                intent.getStringExtra("bookname"),
                ApplyPreference.getAutoDownNum(this)
            )
        )
        init()
    }

    override fun setViewModel(vm: ReaderViewModel) {
        readerViewModel = vm
        val binding =
            DataBindingUtil.setContentView<ActivityReaderBinding>(this, R.layout.activity_reader)
        binding.clickEvent = ReaderClickEvent()
        binding.readerViewModel = readerViewModel
        binding.readerView.background = getDrawable(R.drawable.bg_readbook_yellow)
        binding.readerView.firstDrawListener = this
        binding.readerView.pageListener = this
        binding.readerView.txtFontSize = getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
            .getFloat(FONTSIZE, 50f)
    }

    override fun init() {
        loadingbar.hide()
        netStateReceiver = NetChangeReceiver()
        val filter = IntentFilter()
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(netStateReceiver, filter)

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
    }

    override fun onBackPressed() {
        if (footView.visibility == View.VISIBLE) {
            headerView.visibility = View.INVISIBLE
            footView.visibility = View.INVISIBLE
            fontSetting.visibility = View.INVISIBLE
        } else {
            super.onBackPressed()
        }
    }

    /**
     * readerview第一次绘制时调用
     */
    override fun doDrawPrepare() {
        readerView.pageNum = readerViewModel?.initData()
        val boolean = readerViewModel?.pageByCatalog(null)
        if (boolean ?: true) downloadChapter()
    }

    override fun onCenterClick() {
        if (hideHeaderFoot()) return
        footView.visibility = View.VISIBLE
        headerView.visibility = View.VISIBLE
    }

    override fun nextChapter() {
        if (loadingbar.isShown) loadingbar.hide()
        hideHeaderFoot()
        val boolean = readerViewModel?.nextChapter()
        if (boolean ?: true) downloadChapter()
    }

    override fun previousChapter() {
        if (loadingbar.isShown) loadingbar.hide()
        hideHeaderFoot()
        val boolean = readerViewModel?.previousChapter()
        if (boolean ?: true) downloadChapter()
    }

    override fun onPageChange() {
        hideHeaderFoot()
        Thread {
            readerViewModel?.setRecord(readerViewModel?.chapterNum ?: 0, readerView.pageNum!!)
        }.start()
    }

    private fun downloadChapter() {
        loadingbar.show()
        Observable.create<Boolean> {
            var str = ChapterCache.FILENOTFOUND
            var times = 0
            while (str == ChapterCache.FILENOTFOUND && times++ < 10) {
                str = readerViewModel!!.chapterCache.getFromNet(
                    getSavePath() + "/" + readerViewModel?.dirName!!,
                    readerView.title ?: "HELLO"
                )
                Thread.sleep(500)
            }
            if (str == ChapterCache.FILENOTFOUND || str.isEmpty()) {
                it.onNext(false)
            } else {
                it.onNext(true)
            }
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it && !(readerViewModel?.pageByCatalog(null) ?: true)) loadingbar.hide()
            }
    }

    override fun showDialog() {
        var catalogView: RecyclerView? = null
        if (dialog == null) {
            val builder = AlertDialog.Builder(this)
            val view = LayoutInflater.from(this).inflate(R.layout.dialog_catalog, null)
            catalogView = view.findViewById(R.id.catalogView)
            catalogView.layoutManager = LinearLayoutManager(this)
            catalogView.addItemDecoration(NovelItemDecoration(this))
            catalogView.itemAnimator = DefaultItemAnimator()
            catalogView.adapter = BindingAdapter(
                readerViewModel?.catalog, R.layout.item_catalog,
                CatalogItemClickListener()
            )
            dialog = builder.setView(view).create()
        }
        readerViewModel?.updateCatalog()
        catalogView?.adapter?.notifyDataSetChanged()
        catalogView?.scrollToPosition(readerViewModel!!.chapterNum - 1)
        dialog?.show()
        dialog?.window?.setLayout(readerView.width * 5 / 6, readerView.height * 9 / 10)
    }

    private fun hideHeaderFoot(): Boolean {
        if (footView.visibility == View.VISIBLE) {
            headerView.visibility = View.INVISIBLE
            footView.visibility = View.INVISIBLE
            fontSetting.visibility = View.INVISIBLE
            return true
        }
        return false
    }

    inner class NetChangeReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netState = cm.activeNetworkInfo
            val isAvailable = netState?.isAvailable ?: false
            if (isAvailable && loadingbar.isShown) {
                Timer().schedule(object : TimerTask() {
                    override fun run() {
                        downloadChapter()
                    }
                }, 500)
            }
        }
    }

    inner class CatalogItemClickListener : IClickEvent {
        fun onChapterClick(v: View) {
            if (loadingbar.isShown) loadingbar.hide()
            val boolean = readerViewModel?.pageByCatalog(v.itemChapter.text.toString())
            if (boolean ?: true) {
                readerView.title = v.itemChapter.text.toString()
                downloadChapter()
            }
            dialog?.dismiss()
        }
    }

    inner class ReaderClickEvent : IClickEvent {

        private var selectedBgImageView: CircleImageView? = null      //用于记录用户当前选中的背景样式的UI控件
        private var selectedFontTextView: TextView? = null             //用于记录用户当前选中的字体样式的UI控件

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
                    showDialog()
                }
                fontSizeButton -> {
                    if (fontSetting.visibility == View.VISIBLE) {
                        fontSetting.visibility = View.INVISIBLE
                    } else {
                        fontSetting.visibility = View.VISIBLE
                    }
                }
            }
        }

        fun onFontSizeClick(v: View) {
            readerView.txtFontSize = (v as TextView).text.toString().toFloat()
            getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).edit()
                .putFloat(FONTSIZE, readerView.txtFontSize!!).apply()
        }

        //改变阅读界面字体类型
        fun onFontTypeClick(view: View) {
            view as TextView
            val context = view.context
            val typeFacePath =      //根据用户选择获取字体路径
                when (view.id) {
                    R.id.tv_beiweikai -> Config.FONTTYPE_BEIWEIKAISHU
                    R.id.tv_bysong -> Config.FONTTYPE_BYSONG
                    R.id.tv_default -> Config.FONTTYPE_DEFAULT
                    R.id.tv_zhulang -> Config.FONTTYPE_CHENGUANG
                    R.id.tv_fzkatong -> Config.FONTTYPE_FZKATONG
                    else -> Config.FONTTYPE_DEFAULT
                }

            readerView.txtFontType = Config.getTypeface(context, typeFacePath)        //根据新的字体重新绘制视图
            selectedFontTextView?.let {
                Config.setTextViewSelect(
                    it,
                    false
                )                            //将前次选中的字体TextView改变背景色
            }
            selectedFontTextView = view
            Config.setTextViewSelect(
                view,
                true
            )                               //为当前选中的字体TextView改变背景色

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
