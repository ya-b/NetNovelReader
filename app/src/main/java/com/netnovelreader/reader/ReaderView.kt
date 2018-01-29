package com.netnovelreader.reader

import android.content.Context
import android.databinding.ObservableArrayList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/**
 * Created by yangbo on 2018/1/19.
 */

class ReaderView : View {
    /**
     * View显示的内容
     */
    var text: ObservableArrayList<String>? = null
        set(text) {
            field = text
            invalidate()
        }
    /**
     * 第一次onDraw()时执行
     */
    internal var firstDrawListener: FirstDrawListener? = null
    private val paint = Paint()
    var txtFontSize: Float = 50f
    var indacitorFontSize: Float = 30f
    private var isFirstDraw = true

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int)
            : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        paint.color = Color.BLACK
        paint.textSize = 40f
        paint.isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        if (isFirstDraw) {
            isFirstDraw = false
            if (firstDrawListener != null) {
                firstDrawListener!!.doDrawPrepare()
            }
            firstDrawListener = null
        }
        if (this.text!!.size < 1) return
        super.onDraw(canvas)
        paint.textSize = indacitorFontSize
        //右下角绘制：   第n章：n/n
        canvas.drawText(
            this.text!![0], width - indacitorFontSize * text!![0].toCharArray().size,
            height - indacitorFontSize, paint
        )
        paint.textSize = txtFontSize
        for (i in 1 until text!!.size) {
            //绘制正文
            canvas.drawText(
                this.text!![i].replace(" ", "    "), getMarginLeft(),
                getMarginTop() + (i - 1) * txtFontSize, paint
            )
        }
    }

    fun getTextWidth(): Int {
        return (width * 0.96f).toInt()
    }

    fun getTextHeight(): Int {
        return((height - indacitorFontSize) * 0.96f).toInt()
    }

    private fun getMarginLeft(): Float {
        val count = (width * 0.96f).toInt() / txtFontSize.toInt()
        return (width - count * txtFontSize) / 2
    }

    private fun getMarginTop(): Float {
        val count = ((height - indacitorFontSize) * 0.96f).toInt() / txtFontSize.toInt()
        return ((height - indacitorFontSize) - count * txtFontSize) / 3 + txtFontSize
    }

    internal interface FirstDrawListener {
        fun doDrawPrepare()
    }

}
