package com.netnovelreader.reader

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Color
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
import com.netnovelreader.BR
import com.netnovelreader.R
import com.netnovelreader.base.IClickEvent
import com.netnovelreader.common.ApplyPreference
import com.netnovelreader.common.BindingAdapter
import com.netnovelreader.common.NovelItemDecoration
import com.netnovelreader.common.PREFERENCE_NAME
import com.netnovelreader.databinding.ActivityReaderBinding
import com.netnovelreader.search.SearchActivity
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_reader.*
import kotlinx.android.synthetic.main.item_catalog.view.*


class ReaderActivity : AppCompatActivity(), IReaderContract.IReaderView,
    ReaderView.ReaderPageListener, ReaderView.FirstDrawListener, IClickEvent {
    val FONTSIZE = "FontSize"
    var readerViewModel: ReaderViewModel? = null
    var dialog: AlertDialog? = null
    private var selectedBgImageView: CircleImageView? = null      //用于记录用户当前选中的背景样式的UI控件
    private var selectedFontTextView: TextView? = null             //用于记录用户当前选中的字体样式的UI控件

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
        binding.setVariable(BR.clickEvent, ReaderClickEvent())
        binding.setVariable(BR.chapter, readerViewModel)
    }

    override fun init() {
        readerView.background = getDrawable(R.drawable.bg_readbook_yellow)
        readerView.firstDrawListener = this
        readerView.pageListener = this
        readerView.txtFontSize = getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
            .getFloat(FONTSIZE, 50f)


        sb_brightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                setScreenLight(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })
    }

    fun setScreenLight(progress: Int) {
        var progress = progress
        if (progress < 1) {
            progress = 1
        } else if (progress > 255) {
            progress = 255
        }
        val attrs = window.attributes
        attrs.screenBrightness = progress / 255f
        window.attributes = attrs
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
        readerViewModel?.initData(
            readerView.getTextWidth(), readerView.getTextHeight(), readerView.txtFontSize
        )
    }

    override fun onCenterClick() {
        if (hideHeaderFoot()) return
        footView.visibility = View.VISIBLE
        headerView.visibility = View.VISIBLE
    }

    override fun pageToNext() {
        if (hideHeaderFoot()) return
        readerViewModel!!.pageToNext(
            readerView.getTextWidth(), readerView.getTextHeight(), readerView.txtFontSize
        )
    }

    override fun pageToPrevious() {
        if (hideHeaderFoot()) return
        readerViewModel!!.pageToPrevious(
            readerView.getTextWidth(), readerView.getTextHeight(), readerView.txtFontSize
        )
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
        catalogView?.scrollToPosition(readerViewModel!!.pageIndicator[0] - 1)
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

    inner class CatalogItemClickListener : IClickEvent {
        fun onChapterClick(v: View) {
            readerViewModel?.pageByCatalog(
                v.itemChapter.text.toString(),
                readerView.getTextWidth(),
                readerView.getTextHeight(),
                readerView.txtFontSize
            )
            dialog?.dismiss()
        }
    }

    inner class ReaderClickEvent : IClickEvent {
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
                .putFloat(FONTSIZE, readerView.txtFontSize).apply()
            readerViewModel?.changeFontSize(
                readerView.getTextWidth(), readerView.getTextHeight(), readerView.txtFontSize
            )
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

            readerView.txtFontType =
                    Config.getTypeface(context, typeFacePath)          //根据新的字体重新绘制视图
            selectedFontTextView?.let {
                Config.setTextViewSelect(
                    it,
                    false
                )
            }  //将前次选中的字体TextView改变背景色
            selectedFontTextView = view
            Config.setTextViewSelect(
                view,
                true
            )                                 //为当前选中的字体TextView改变背景色

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
