package com.netnovelreader.reader

import android.content.Context
import android.databinding.ObservableField
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.netnovelreader.R
import com.netnovelreader.common.PreferenceManager
import com.netnovelreader.common.download.ChapterCache
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.KProperty

/**
 * Created by yangbo on 2018/1/19.
 */

class ReaderView : View, GestureDetector.OnGestureListener {
    var rowSpace = 2f                                                      //行距

    var txtFontColorId: Int = 0                                            //字体颜色
    var bgColorId: Int? by InvalidateAfterSet(R.color.read_font_default)   //背景颜色
    var txtFontType: Typeface? by InvalidateAfterSet(null)           //正文字体类型//背景颜色
    private val mBottomPaint = Paint()                                     //绘制底部文字部分所用的画笔
    private val mMainPaint = Paint()                                       //绘制正文部分所用的画笔
    var txtFontSize: Float? by InvalidateAfterSet(50f)               //正文部分默认画笔的大小
    var indicatorFontSize: Float = 35f                                     //底部部分默认画笔的大小

    var text: ObservableField<String>? by InvalidateAfterSet(null)    //一个未分割章节,格式：章节名|正文
    var textArray: ArrayList<ArrayList<String>>? = null                     //分割后的章节,view显示的内容，第i项是第i行文字内容
    var title: String? by InvalidateAfterSet("")                      //章节名称
    var pageNum: Int? by InvalidateAfterSet(1)                        //页数
    var maxPageNum = 0
    var pageFlag = 0                                        //0刚进入view，1表示目录跳转，2表示下一页，3表示上一页

    lateinit var timeFormatter: SimpleDateFormat
    private val MIN_MOVE = 80F                              //翻页最小滑动距离
    lateinit var detector: GestureDetector
    private var isFirstDraw = true

    var firstDrawListener: FirstDrawListener? = null
    var pageListener: ReaderPageListener? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
            context,
            attrs,
            defStyleAttr
    ) {
        init()
    }


    interface FirstDrawListener {
        /**
         * 第一次绘制时调用
         */
        fun doDrawPrepare()
    }


    interface ReaderPageListener {
        // 下一章
        fun nextChapter()

        //上一章
        fun previousChapter()

        //点击view中间部分
        fun onCenterClick()

        //当翻页时调用，向前向后翻页，同一章内翻页，翻至其他章节都会调用
        fun onPageChange()
    }


    fun init() {
        mBottomPaint.isAntiAlias = true   //抗锯齿开启
        mMainPaint.isAntiAlias = true
        rowSpace = PreferenceManager.getRowSpace(context)
        txtFontColorId = R.color.read_font_default
        bgColorId = R.color.read_bg_default
        timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        detector = GestureDetector(context, this)
        mBottomPaint.textSize = indicatorFontSize                                  //底部部分画笔大小
    }

    override fun onDraw(canvas: Canvas) {
        if (isFirstDraw) {
            isFirstDraw = false
            firstDrawListener?.doDrawPrepare() //绘制前一些操作
            firstDrawListener = null
            flushTextArray()
        }
        super.onDraw(canvas)
        canvas.drawColor(ContextCompat.getColor(context, bgColorId!!))             //背景颜色

        mBottomPaint.color = ContextCompat.getColor(context, txtFontColorId)       //底部部分字体颜色
        mMainPaint.color = ContextCompat.getColor(context, txtFontColorId)         //正文部分字体颜色
        mMainPaint.typeface = txtFontType
        mMainPaint.textSize = txtFontSize!!                                        //正文部分画笔大小


        if (PreferenceManager.isFullScreen(context)) {                             //全屏条件下绘制
            //底部左下角绘制：时间            格式如： 14:40
            val date = timeFormatter.format(System.currentTimeMillis())
            val dateX = getMarginLeft()
            val dateY = height - indicatorFontSize
            canvas.drawText(date, dateX, dateY, mBottomPaint)
        }


        //底部右下角绘制：章节相关信息    格式为:    第 XXX 章节 YYY章节名  ：  n / 该章节总共页数
        val bottomText = "${title ?: ""} $pageNum/$maxPageNum"
        canvas.drawText(
                bottomText,
                width - mBottomPaint.measureText(bottomText) - getMarginLeft(),
                height - indicatorFontSize,
                mBottomPaint
        )

        if (textArray == null || maxPageNum < 1) return              //正文内容缺乏，直接不绘制了
        //绘制正文
        for (i in 0 until textArray!![pageNum!! - 1].size) {
            canvas.drawText(
                    textArray!![pageNum!! - 1][i].replace(" ", "    "),
                    getMarginLeft(),
                    getMarginTop() + i * txtFontSize!! * rowSpace,
                    mMainPaint
            )
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            performClick()
        }
        return detector.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onDown(e: MotionEvent?): Boolean {
        return true
    }

    override fun onFling(e1: MotionEvent, e2: MotionEvent, vX: Float, vY: Float): Boolean {
        val beginX = e1.x
        val endX = e2.x
        if (Math.abs(beginX - endX) < MIN_MOVE) {
            return false
        }
        if (beginX > endX) {
            pageToNext()
        } else {
            pageToPrevious()
        }
        return false
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        val x = e.x
        val y = e.y
        if (x > width * 3 / 5) {
            pageToNext()
        } else if (x < width * 2 / 5) {
            pageToPrevious()
        } else if (y > height * 2 / 5 && y < height * 3 / 5) {
            pageListener?.onCenterClick()
            pageFlag = 1
        }
        return false
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

    //向前翻页
    private fun pageToPrevious() {
        pageFlag = 3
        if (pageNum!! < 2) {
            pageListener?.previousChapter()
        } else {
            pageNum = pageNum!! - 1
        }
        pageListener?.onPageChange()
    }

    //向后翻页
    private fun pageToNext() {
        pageFlag = 2
        if (pageNum!! < maxPageNum) {
            pageNum = pageNum!! + 1
        } else {
            pageListener?.nextChapter()
        }
        pageListener?.onPageChange()
    }

    private fun flushTextArray() {
        val str = text?.get()
        if (str.isNullOrEmpty()) return
        val indexOfDelimiter = str!!.indexOf("|")       //text格式 ： 章节名|正文
        title = str.substring(0, indexOfDelimiter)
        val tx = str.substring(indexOfDelimiter + 1)
        textArray = spliteText(tx)
        maxPageNum = if (tx == ChapterCache.FILENOTFOUND) 0 else textArray!!.size
    }

    private fun spliteText(text: String?): ArrayList<ArrayList<String>> {
        if (text.isNullOrEmpty()) return ArrayList()
        val tmpArray = text!!.split("\n")
        val tmplist = ArrayList<String>()
        tmpArray.forEach {
            val tmp = "  " + it.trim()
            val totalCount = getTextWidth() / txtFontSize!!.toInt() //一行容纳字数
            for (i in 0..tmp.length / totalCount) {
                tmplist.add(tmp.filterIndexed { index, _ -> index > i * totalCount - 1 && index < (i + 1) * totalCount })
            }
        }
        val arrayList = ArrayList<ArrayList<String>>()
        val totalCount = getTextHeight() / (txtFontSize!! * rowSpace).toInt()  //一页容纳行数
        for (i in 0..tmplist.size / totalCount) {
            arrayList.add(tmplist.filterIndexed { index, s -> index > i * totalCount - 1 && index < (i + 1) * totalCount } as ArrayList<String>)
        }
        return arrayList
    }

    //正文区域宽度
    private fun getTextWidth(): Int {
        return (width * 0.96f).toInt()
    }

    //正文区域高度
    private fun getTextHeight(): Int {
        return ((height - indicatorFontSize) * 0.96f).toInt()
    }

    private fun getMarginLeft(): Float {
        val count = getTextWidth() / txtFontSize!!.toInt()
        return (width - count * txtFontSize!!) / 2
    }

    private fun getMarginTop(): Float {
        val count = getTextHeight() / (txtFontSize!! * rowSpace).toInt()
        return ((height - indicatorFontSize) - count * txtFontSize!! * rowSpace) / 2 + txtFontSize!!
    }

    inner class InvalidateAfterSet<T>(var value: T? = null) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
            return value
        }

        @Synchronized
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
            @Volatile
            this.value = value
            when (property.name) {
                "txtFontSize" -> {
                    val scale = maxPageNum.toFloat() / pageNum!!
                    flushTextArray()
                    pageNum = (maxPageNum / scale).toInt().takeIf { it != 0 } ?: 1
                }
                "text" -> {
                    flushTextArray()
                    pageNum = when(pageFlag){
                        0 -> if(maxPageNum == 0) 0 else pageNum
                        1,2 -> if(maxPageNum == 0) 0 else 1
                        3 -> maxPageNum
                        else -> 1
                    }

                }
                else -> invalidate()
            }
        }
    }
}
