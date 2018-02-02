package com.netnovelreader.reader

import android.content.Context
import android.databinding.ObservableArrayList
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.netnovelreader.R
import com.netnovelreader.common.ApplyPreference
import java.text.SimpleDateFormat
import java.util.*
import kotlin.reflect.KProperty

/**
 * Created by yangbo on 2018/1/19.
 */

class ReaderView : View, GestureDetector.OnGestureListener {
    companion object {
        var rowSpace = 2f                                  //行距
    }

    var txtFontColorId: Int = 0                               //字体颜色
    var text: ObservableArrayList<String>? by InvalidateAfterSet(null)  //view显示的内容，第 0 item项是章节名字，第i项是第i行文字内容
    var bgColorId: Int? by InvalidateAfterSet(R.color.read_font_default)      //背景颜色
    var txtFontType: Typeface? by InvalidateAfterSet(null)              //正文字体类型//背景颜色
    internal var firstDrawListener: FirstDrawListener? = null
    var pageListener: ReaderPageListener? = null

    private val mBottomPaint = Paint()                //绘制底部文字部分所用的画笔
    private val mMainPaint = Paint()                  //绘制正文部分所用的画笔
    var txtFontSize: Float = 50f                      //正文部分默认画笔的大小,单位是像素px
    var indicatorFontSize: Float = 35f                //底部部分默认画笔的大小，单位是像素px


    private var isFirstDraw = true
    lateinit var timeFormatter: SimpleDateFormat
    private val MIN_MOVE = 80F //翻页最小滑动距离
    lateinit var detector: GestureDetector


    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }


    fun init() {
        mMainPaint.textSize = 40f
        mBottomPaint.isAntiAlias = true   //抗锯齿开启
        mMainPaint.isAntiAlias = true
        rowSpace = ApplyPreference.getRowSpace(context)
        txtFontColorId = R.color.read_font_default
        bgColorId = R.color.read_bg_default
        timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        detector = GestureDetector(context, this)
    }

    override fun onDraw(canvas: Canvas) {
        if (isFirstDraw) {
            isFirstDraw = false
            firstDrawListener?.doDrawPrepare() //绘制前一些操作
            firstDrawListener = null
        }

        if (this.text!!.size < 1) return      //正文内容缺乏，直接不绘制了
        super.onDraw(canvas)


        canvas.drawColor(ContextCompat.getColor(context, bgColorId!!))                      //背景颜色


        mBottomPaint.textSize = indicatorFontSize                                    //底部部分画笔大小
        mBottomPaint.color = ContextCompat.getColor(context, txtFontColorId)              //底部部分字体颜色
        mMainPaint.textSize = txtFontSize                                            //正文部分画笔大小
        mMainPaint.color = ContextCompat.getColor(context, txtFontColorId)                //正文部分字体颜色
        mMainPaint.typeface = txtFontType
        //绘制正文
        for (i in 1 until text!!.size) {
            canvas.drawText(
                this.text!![i].replace(" ", "    "),
                getMarginLeft(),
                getMarginTop() + (i - 1) * txtFontSize * rowSpace,
                mMainPaint
            )
        }

        if (ApplyPreference.isFullScreen(context)) {  //全屏下绘制
            //底部左下角绘制：时间            格式如： 14:40
            val date = timeFormatter.format(System.currentTimeMillis())
            val dateX = getMarginLeft()
            val dateY = height - indicatorFontSize
            canvas.drawText(
                date,
                dateX,
                dateY,
                mBottomPaint
            )//日期的文字 + 绘制日期的X坐标 + 绘制日期的Y坐标 + 绘制底部专用的画笔
        }


        //底部右下角绘制：章节相关信息    格式为:    第 XXX 章节 YYY章节名  ：  n / 该章节总共页数
        canvas.drawText(
            this.text!![0],
            width - mBottomPaint.measureText(text!![0]) - getMarginLeft(),
            height - indicatorFontSize,
            mBottomPaint
        )
    }

    //正文区域宽度
    fun getTextWidth(): Int {
        return (width * 0.96f).toInt()
    }

    //正文区域高度
    fun getTextHeight(): Int {
        return ((height - indicatorFontSize) * 0.96f).toInt()
    }

    private fun getMarginLeft(): Float {
        val count = (width * 0.96f).toInt() / txtFontSize.toInt()
        return (width - count * txtFontSize) / 2
    }

    private fun getMarginTop(): Float {
        val count =
            ((height - indicatorFontSize) * 0.96f).toInt() / (txtFontSize * rowSpace).toInt()
        return ((height - indicatorFontSize) - count * txtFontSize * rowSpace) / 2 + txtFontSize
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

    override fun onFling(
        e1: MotionEvent,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        val beginX = e1.x
        val endX = e2.x
        if (Math.abs(beginX - endX) < MIN_MOVE) {
            return false
        } else {
            if (beginX > endX) {
                pageListener?.pageToNext()
            } else {
                pageListener?.pageToPrevious()
            }
        }
        return false
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        val x = e.x
        val y = e.y
        if (x > width * 3 / 5) {
            pageListener?.pageToNext()
        } else if (x < width * 2 / 5) {
            pageListener?.pageToPrevious()
        } else if (y > height * 2 / 5 && y < height * 3 / 5) {
            pageListener?.onCenterClick()
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

    interface FirstDrawListener {
        fun doDrawPrepare()
    }

    interface ReaderPageListener {
        fun pageToNext()
        fun pageToPrevious()
        fun onCenterClick()
    }

    inner class InvalidateAfterSet<T>(var value: T? = null) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
            return value
        }

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
            this.value = value
            invalidate()
        }
    }

}
