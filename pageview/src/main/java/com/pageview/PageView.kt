package com.pageview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.KProperty

/**
 * Created by yangbo on 2018/1/19.
 */

class PageView : View, GestureDetector.OnGestureListener {
    val FILENOTFOUND = "            "
    var isDrawTime = false
    var rowSpace = 2f                                                   //行距
    var textColor: Int = Color.BLACK                                    //字体颜色
    var txtFontType: Typeface? by InvalidateAfterSet(null)        //正文字体类型//背景颜色
    var textSize: Float? by InvalidateAfterSet(50f)               //正文部分默认画笔的大小
    var mBottomTextSize: Float = 35f                                    //底部部分默认画笔的大小
    var text: String? by InvalidateAfterSet(null)                 //一个未分割章节,格式：章节名|正文
    var textArray: ArrayList<ArrayList<String>>? = null                 //分割后的章节,第i项是第i行文字内容
    var title: String? by InvalidateAfterSet("")                  //章节名称
    var pageNum: Int? by InvalidateAfterSet(1)                    //页数
    var maxPageNum = 0
    var pageFlag = 0                                 //0刚进入view，1表示目录跳转，2表示下一页，3表示上一页
    private val mPaint by lazy {
        Paint().apply {
            isAntiAlias = true
            isDither = true
            isFilterBitmap = true
        }
    }
    private val timeFormatter: SimpleDateFormat by lazy { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    private val MIN_MOVE = 80F                       //翻页最小滑动距离

    private var mCurrentPage: Bitmap? = null
    private var mPreviousPage: Bitmap? = null

    var doDrawPrepare: PageListener.DoDrawPrepare? = null          //第一次绘制时调用
    var nextChapter: PageListener.NextChapter? = null              // 下一章
    var previousChapter: PageListener.PreviousChapter? = null      //上一章
    var onCenterClick: PageListener.OnCenterClick? = null          //点击view中间部分
    var onPageChange: PageListener.OnPageChange? = null            //当翻页时调用，向前向后翻页，同一章内翻页，翻至其他章节都会调用

    private val detector: GestureDetector

    constructor(context: Context) : super(context) {
        detector = GestureDetector(context, this)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        detector = GestureDetector(context, this)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
            context,
            attrs,
            defStyleAttr
    ) {
        detector = GestureDetector(context, this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        pageNum = doDrawPrepare?.doDrawPrepare()              //绘制前一些操作
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
//        drawContent(canvas)
        mPreviousPage?.recycle()
        mPreviousPage = mCurrentPage
        mCurrentPage = genBitmap()
        canvas.drawBitmap(mCurrentPage, 0f, 0f, null)
    }

    private fun genBitmap(): Bitmap =
            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { drawContent(Canvas(it)) }

    private fun drawContent(canvas: Canvas) {
        mPaint.color = textColor                              //字体颜色
        mPaint.typeface = txtFontType
        mPaint.textSize = mBottomTextSize

        if (isDrawTime) {  //全屏条件下绘制
            val date = timeFormatter.format(System.currentTimeMillis())
            //底部左下角绘制：时间。格式如： 14:40
            canvas.drawText(date, getMarginLeft(), height - mBottomTextSize, mPaint)
        }
        //底部右下角绘制：章节相关信息    格式为:   第 XXX 章节 YYY章节名  ：  n / 该章节总共页数
        val bottomText = "${title ?: ""} $pageNum/$maxPageNum"
        canvas.drawText(
                bottomText,
                width - mPaint.measureText(bottomText) - getMarginLeft(),
                height - mBottomTextSize,
                mPaint
        )
        if (textArray == null || maxPageNum < 1) return              //正文内容缺乏，直接不绘制了
        mPaint.textSize = textSize!!                                        //正文部分画笔大小
        //绘制正文
        for (i in 0 until textArray!![pageNum!! - 1].size) {
            canvas.drawText(
                    textArray!![pageNum!! - 1][i].replace(" ", "    "),
                    getMarginLeft(),
                    getMarginTop() + i * textSize!! * rowSpace,
                    mPaint
            )
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return detector.onTouchEvent(event)
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
            onCenterClick?.onCenterClick()
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
            previousChapter?.previousChapter()
        } else {
            pageNum = pageNum!! - 1
        }
    }

    //向后翻页
    private fun pageToNext() {
        pageFlag = 2
        if (pageNum!! < maxPageNum) {
            pageNum = pageNum!! + 1
        } else {
            nextChapter?.nextChapter()
        }
    }

    private fun spliteText(text: String?): ArrayList<ArrayList<String>> {
        if (text.isNullOrEmpty()) return ArrayList()
        val tmpArray = text!!.split("\n")
        val tmplist = ArrayList<String>()
        tmpArray.forEach {
            val tmp = "  " + it.trim()
            val totalCount = getTextWidth() / textSize!!.toInt() //一行容纳字数
            for (i in 0..tmp.length / totalCount) {
                tmplist.add(tmp.filterIndexed { index, _ -> index > i * totalCount - 1 && index < (i + 1) * totalCount })
            }
        }
        val arrayList = ArrayList<ArrayList<String>>()
        val totalCount = getTextHeight() / (textSize!! * rowSpace).toInt()  //一页容纳行数
        for (i in 0..tmplist.size / totalCount) {
            arrayList.add(tmplist.filterIndexed { index, _ -> index > i * totalCount - 1 && index < (i + 1) * totalCount } as ArrayList<String>)
        }
        return arrayList
    }

    //正文区域宽度
    private fun getTextWidth(): Int = (width * 0.96f).toInt()

    //正文区域高度
    private fun getTextHeight(): Int = ((height - mBottomTextSize) * 0.96f).toInt()

    //左边距
    private fun getMarginLeft(): Float {
        val count = getTextWidth() / textSize!!.toInt()
        return (width - count * textSize!!) / 2
    }

    //上边距
    private fun getMarginTop(): Float {
        val count = getTextHeight() / (textSize!! * rowSpace).toInt()
        return ((height - mBottomTextSize) - count * textSize!! * rowSpace) / 2 + textSize!!
    }

    inner class InvalidateAfterSet<T>(var value: T? = null) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T? = value

        @Synchronized
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
            @Volatile
            this.value = value
            when (property.name) {
                "textSize" -> {
                    if (width < 1) return
                    val scale = maxPageNum.toFloat() / pageNum!!
                    textArray = spliteText(text)
                    maxPageNum = if (text == FILENOTFOUND || text.isNullOrEmpty()) 0 else textArray!!.size
                    pageNum = (maxPageNum / scale).toInt().takeIf { it != 0 } ?: 1
                }
                "text" -> {
                    if (width < 1) return
                    textArray = spliteText(text)
                    maxPageNum = if (text == FILENOTFOUND || text.isNullOrEmpty()) 0 else textArray!!.size
                    pageNum = when (pageFlag) {
                        0 -> if (maxPageNum == 0) 0 else if (pageNum == 0) 1 else pageNum
                        1, 2 -> if (maxPageNum == 0) 0 else 1
                        3 -> maxPageNum
                        else -> 1
                    }
                }
                "pageNum" -> {
                    onPageChange?.onPageChange(pageNum!!)
                    postInvalidate()
                }
                else -> postInvalidate()  //刷新view
            }
        }
    }
}
