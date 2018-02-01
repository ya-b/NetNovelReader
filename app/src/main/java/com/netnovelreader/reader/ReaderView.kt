package com.netnovelreader.reader

import android.content.Context
import android.databinding.ObservableArrayList
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.netnovelreader.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by yangbo on 2018/1/19.
 */

class ReaderView : View {
    companion object {

        /**
         * 行距
         */
        val rowSpace = 1.3f
    }
    /**
     * View显示的内容
     */
    var text: ObservableArrayList<String>? = null
        set(text) {
            field = text
            invalidate()
        }
    var txtFontColorId: Int = R.color.read_font_default    //字体颜色
    var bgColorId: Int = R.color.read_bg_default           //背景颜色
        set(value) {
            field = value
            postInvalidate()
        }

    internal var firstDrawListener: FirstDrawListener? = null

    private val mBottomIndicatorPaint = Paint()     //绘制底部部分所用的画笔
    private val mTextPaint = Paint()                //绘制正文部分所用的画笔

    var txtFontSize: Float = 50f                     //正文部分默认画笔的大小
    private var indicatorFontSize: Float = 30f               //底部部分默认画笔的大小


    private var isFirstDraw = true


    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )


    init {
        mTextPaint.textSize = 40f
        mBottomIndicatorPaint.isAntiAlias = true   //抗锯齿开启
        mTextPaint.isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        if (isFirstDraw) {
            isFirstDraw = false
            firstDrawListener?.doDrawPrepare() //绘制前一些操作
            firstDrawListener = null           //该绘制前的操作只执行一次
        }

        if (this.text!!.size < 1) return      //正文内容缺乏，直接不绘制了
        super.onDraw(canvas)


        canvas.drawColor(context.resources.getColor(bgColorId))                      //背景颜色


        mBottomIndicatorPaint.textSize = indicatorFontSize                           //底部部分画笔大小
        mBottomIndicatorPaint.color = context.resources.getColor(txtFontColorId)     //底部部分字体颜色
        mTextPaint.textSize = txtFontSize                                            //正文部分画笔大小
        mTextPaint.color = context.resources.getColor(txtFontColorId)                //正文部分字体颜色

        //绘制正文
        for (i in 1 until text!!.size) {
            canvas.drawText(
                this.text!![i].replace(" ", "    "),
                getMarginLeft(),
                getMarginTop() + (i - 1) * txtFontSize * rowSpace,
                mTextPaint
            )
        }

        val date = SimpleDateFormat("HH:mm").format(Date())
        val dateWith = mTextPaint.measureText(date)           //时间宽度

        //底部绘制： 时间 、手机电池容量 +  第n章：n/n
        canvas.drawText(
            this.text!![0],
            width - indicatorFontSize * text!![0].toCharArray().size,
            height - indicatorFontSize,
            mBottomIndicatorPaint
        )
        canvas.drawText(date, indicatorFontSize, height - indicatorFontSize, mBottomIndicatorPaint)
        //val rect1Left = indicatorFontSize * 2 + dateWith      //电池外框left位置=靠左边距+日期宽度+一段距离


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

    internal interface FirstDrawListener {
        fun doDrawPrepare()
    }

}
