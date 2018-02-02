package com.netnovelreader.reader

import android.annotation.SuppressLint
import android.content.Context
import android.databinding.ObservableArrayList
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import com.netnovelreader.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by yangbo on 2018/1/19.
 */

class ReaderView : View {
    companion object {
        val rowSpace = 2f                                  //行距
    }

    var txtFontColorId: Int = R.color.read_font_default    //字体颜色
    var text: ObservableArrayList<String>? = null         //view显示的内容，第 0 item项是章节名字，第i项是第i行文字内容
        set(text) {
            field = text
            invalidate()
        }
    var bgColorId: Int = R.color.read_bg_default           //背景颜色
        set(value) {
            field = value
            postInvalidate()
        }
    var txtFontType: Typeface? = null                     //正文字体类型
        set(value) {
            field = value
            postInvalidate()
        }
    internal var firstDrawListener: FirstDrawListener? = null

    private val mBottomPaint = Paint()                //绘制底部文字部分所用的画笔
    private val mMainPaint = Paint()                  //绘制正文部分所用的画笔
    var txtFontSize: Float = 50f                      //正文部分默认画笔的大小,单位是像素px
    var indicatorFontSize: Float = 35f                //底部部分默认画笔的大小，单位是像素px


    private var isFirstDraw = true


    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    init {
        mMainPaint.textSize = 40f
        mBottomPaint.isAntiAlias = true   //抗锯齿开启
        mMainPaint.isAntiAlias = true
    }

    @SuppressLint("DrawAllocation", "SimpleDateFormat", "WrongConstant")
    override fun onDraw(canvas: Canvas) {
        if (isFirstDraw) {
            isFirstDraw = false
            firstDrawListener?.doDrawPrepare() //绘制前一些操作
            firstDrawListener = null           //该绘制前的操作只执行一次
        }

        if (this.text!!.size < 1) return      //正文内容缺乏，直接不绘制了
        super.onDraw(canvas)


        canvas.drawColor(context.resources.getColor(bgColorId))                      //背景颜色


        mBottomPaint.textSize = indicatorFontSize                                    //底部部分画笔大小
        mBottomPaint.color = context.resources.getColor(txtFontColorId)              //底部部分字体颜色
        mMainPaint.textSize = txtFontSize                                            //正文部分画笔大小
        mMainPaint.color = context.resources.getColor(txtFontColorId)                //正文部分字体颜色
        mMainPaint.typeface = txtFontType                                            //正文部分字体类型，我们准备了共5种字体

        //绘制正文
        for (i in 1 until text!!.size) {
            canvas.drawText(
                    this.text!![i].replace(" ", "    "),
                    getMarginLeft(),
                    getMarginTop() + (i - 1) * txtFontSize * rowSpace,
                    mMainPaint
            )
        }


        //底部左下角绘制：时间            格式如： 14:40
        val date = SimpleDateFormat("HH:mm").format(Date())
        val dateX = getMarginLeft()
        val dateY = height - indicatorFontSize
        canvas.drawText(date, dateX, dateY, mBottomPaint)//日期的文字 + 绘制日期的X坐标 + 绘制日期的Y坐标 + 绘制底部专用的画笔


        //底部右下角绘制：章节相关信息    格式为:    第 XXX 章节 YYY章节名  ：  n / 该章节总共页数
        canvas.drawText(
                this.text!![0],
                width - indicatorFontSize * text!![0].toCharArray().size,
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

    internal interface FirstDrawListener {
        fun doDrawPrepare()
    }

}
