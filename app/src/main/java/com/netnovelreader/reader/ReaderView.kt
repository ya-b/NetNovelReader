package com.netnovelreader.reader

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast

/**
 * Created by yangbo on 18-1-17.
 */
class ReaderView : View, GestureDetector.OnGestureListener {
    //最小滑动距离
    val MIN_MOVE = 80F
    //当前页码
    var pageNum = 1
    //最大页码，-1表示未知
    var maxNum = -1
    //显示的内容
    var text = ""
    var detector: GestureDetector
    var mViewModel: ReaderViewModel? = null
    var paint: Paint
    init {
        detector = GestureDetector(context, this)
        paint = Paint()
        paint.color = Color.BLACK
        paint.textSize = 50F
    }

    constructor(mContext: Context): super(mContext)
    constructor(mContext: Context, attrs: AttributeSet): super(mContext, attrs)
    constructor(mContext: Context, attrs: AttributeSet, defStyleAttr: Int): super(mContext, attrs, defStyleAttr)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.WHITE)
        canvas.drawText(text, width/2F, width/2F, paint)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return detector.onTouchEvent(event)
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if(detector.onTouchEvent(event)){
            return true
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onDown(e: MotionEvent): Boolean {
        Log.d("====================","ondown=============")
        return true
    }

    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        var beginX = e1.getX()
        var endX = e2.getX()
        if(Math.abs(beginX - endX) < MIN_MOVE) return false
        if(beginX > endX){
            if(pageNum != maxNum){
                pageNum++
                this.text = "" + pageNum
                invalidate()
                Toast.makeText(context, "next", Toast.LENGTH_SHORT).show()
            }
        }else {
            if(pageNum != 1){
                pageNum--
                Log.d("================ --","$pageNum============")
                this.text == "" + pageNum
                Log.d("================ --","$text============")
                invalidate()
                Toast.makeText(context, "previous", Toast.LENGTH_SHORT).show()
            }
        }
        Log.d("====================","onfling=============")
        return false
    }

    override fun onLongPress(e: MotionEvent) {
        Log.d("====================","onlongpress=============")
    }

    override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        Log.d("====================","onscroll=============")
        return false
    }

    override fun onShowPress(e: MotionEvent) {

        Log.d("====================","onshowpress=============")
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        Log.d("====================","onsingletapup=============")
        return false
    }

    //一页显示多少字
    fun getStrLength(): Int{
        return 100
    }
}