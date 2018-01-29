package com.netnovelreader.reader

import android.app.AlertDialog
import android.content.Context
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import com.netnovelreader.BR
import com.netnovelreader.R
import com.netnovelreader.base.IClickEvent
import com.netnovelreader.common.ApplyPreference
import com.netnovelreader.common.BindingAdapter
import com.netnovelreader.common.NovelItemDecoration
import com.netnovelreader.common.PREFERENCE_NAME
import com.netnovelreader.databinding.ActivityReaderBinding
import kotlinx.android.synthetic.main.activity_reader.*
import kotlinx.android.synthetic.main.item_catalog.view.*


class ReaderActivity : AppCompatActivity(), IReaderContract.IReaderView,
    GestureDetector.OnGestureListener,
    ReaderView.FirstDrawListener, IClickEvent {
    val FONTSIZE = "FontSize"
    var readerViewModel: ReaderViewModel? = null
    private var detector: GestureDetector? = null
    var dialog: AlertDialog? = null
    private val MIN_MOVE = 80F

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
        readerView.txtFontSize = getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
            .getFloat(FONTSIZE, 50f)
        detector = GestureDetector(this, this)
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

    override fun onFling(
        e1: MotionEvent,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        if (footView.visibility == View.VISIBLE) {
            headerView.visibility = View.INVISIBLE
            footView.visibility = View.INVISIBLE
            fontSetting.visibility = View.INVISIBLE
            return false
        }
        val beginX = e1.x
        val endX = e2.x
        if (Math.abs(beginX - endX) < MIN_MOVE) {
            return false
        } else {
            if (beginX > endX) {
                readerViewModel!!.pageToNext(
                    readerView.getTextWidth(), readerView.getTextHeight(), readerView.txtFontSize
                )
            } else {
                readerViewModel!!.pageToPrevious(
                    readerView.getTextWidth(), readerView.getTextHeight(), readerView.txtFontSize
                )
            }
        }
        return false
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        if (footView.visibility == View.VISIBLE) {
            headerView.visibility = View.INVISIBLE
            footView.visibility = View.INVISIBLE
            fontSetting.visibility = View.INVISIBLE
            return false
        }
        val x = e.x
        val y = e.y
        if (x > readerView.width * 3 / 5) {
            readerViewModel!!.pageToNext(
                readerView.getTextWidth(), readerView.getTextHeight(), readerView.txtFontSize
            )
        } else if (x < readerView.width * 2 / 5) {
            readerViewModel!!.pageToPrevious(
                readerView.getTextWidth(), readerView.getTextHeight(), readerView.txtFontSize
            )
        } else if (y > readerView.height * 2 / 5 && y < readerView.height * 3 / 5) {
            if (footView.visibility == View.INVISIBLE) {
                footView.visibility = View.VISIBLE
                headerView.visibility = View.VISIBLE
            }
        }
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return detector!!.onTouchEvent(event)
    }

    override fun onDown(e: MotionEvent?): Boolean {
        return true
    }

    override fun onLongPress(e: MotionEvent?) {
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        return false
    }

    override fun onShowPress(e: MotionEvent?) {
    }

    override fun showDialog() {
        var catalogView: RecyclerView? = null
        if (dialog == null) {
            val builder = AlertDialog.Builder(this)
            val view = LayoutInflater.from(this).inflate(R.layout.dialog_catalog, null)
            catalogView = view.findViewById(R.id.catalogView)
            catalogView.layoutManager = LinearLayoutManager(this)
            catalogView.addItemDecoration(NovelItemDecoration(this))
            catalogView.setItemAnimator(DefaultItemAnimator())
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
            when (v) {
                size30 -> readerView.txtFontSize = 30f
                size40 -> readerView.txtFontSize = 40f
                size60 -> readerView.txtFontSize = 60f
                size70 -> readerView.txtFontSize = 70f
                else -> readerView.txtFontSize = 50f
            }
            getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).edit()
                .putFloat(FONTSIZE, readerView.txtFontSize).apply()
            readerViewModel?.changeFontSize(
                readerView.getTextWidth(), readerView.getTextHeight(), readerView.txtFontSize
            )
        }
    }
}
