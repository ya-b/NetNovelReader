package com.netnovelreader.reader

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.netnovelreader.data.database.ShelfSQLManager
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by yangbo on 18-1-17.
 */
class ReaderView : View, GestureDetector.OnGestureListener {
    /**
     * 最小滑动距离
     */
    val MIN_MOVE = 80F
    /**
     * 当前页码
     * 章节数
     */
    @Volatile
    var pageNum = 1
    @Volatile
    var chapterNum = 1
    @Volatile
    var maxPageNum = 1
    @Volatile
    var maxChapterNum = 1
    var bookName: String? = null
    var dirName: String? = null
    var readRecord: String? = null
    /**
     * 显示当前的章节数及页码
     */
    var indicator = "第1章:1/5"
    /**
     * 拆分后的内容
     */
    var arrayList = ArrayList<ArrayList<String>>()
    var mViewModel: ReaderViewModel? = null
    var detector: GestureDetector
    var txtPaint: Paint
    //字体大小
    var txtFontSize = 50F
    var indacitorFontSize = 30F
    var isFirstDraw = true
    init {
        detector = GestureDetector(context, this)
        txtPaint = Paint()
        txtPaint.color = Color.BLACK
        txtPaint.setAntiAlias(true)
    }

    constructor(mContext: Context) : super(mContext)
    constructor(mContext: Context, attrs: AttributeSet) : super(mContext, attrs)
    constructor(mContext: Context, attrs: AttributeSet, defStyleAttr: Int) : super(mContext, attrs, defStyleAttr)

    override fun onDraw(canvas: Canvas) {
        if(isFirstDraw) doOnFistDraw()
        super.onDraw(canvas)
        indicator = getIndicator(chapterNum, pageNum, maxPageNum)
        txtPaint.textSize = indacitorFontSize
        canvas.drawText(indicator, width - indacitorFontSize * indicator.length, height - indacitorFontSize, txtPaint)
        txtPaint.textSize = txtFontSize
        if(arrayList.size > 0){
            for(i in 0..arrayList.get(pageNum - 1).size - 1){
                canvas.drawText(arrayList.get(pageNum - 1)[i].replace(" ", "    "), width * 0.04F, height * 0.04F + i * txtFontSize, txtPaint)
            }
        }
    }

    fun doOnFistDraw(){
        isFirstDraw = false
        maxChapterNum = mViewModel?.getChapterCount() ?: 1
        if(bookName != null) {
            val queryResult = ShelfSQLManager().getRecord(bookName!!) //阅读记录 3#2 表示第3章第2页
            dirName = "BOOK${queryResult[0]}"
            if(queryResult[1].length > 0){
                val array = queryResult[1].split("#")
                chapterNum = array[0].toInt()
                pageNum  = array[1].toInt()
            }
        }
        getChapter(chapterNum, dirName!!, true)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return detector.onTouchEvent(event)
    }

    override fun onDown(e: MotionEvent): Boolean {
        return true
    }

    @Synchronized
    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        val beginX = e1.getX()
        val endX = e2.getX()
        if (Math.abs(beginX - endX) < MIN_MOVE) return false
        if (beginX > endX) {
            if (pageNum == maxPageNum || maxPageNum < 2){
                if(chapterNum == maxChapterNum){
                    return false
                }else{
                    getChapter(++chapterNum, dirName!!, true)
                }
            }else{
                pageNum++
                invalidate()
            }
        } else {
            if (pageNum < 2){
                if(chapterNum == 1){
                    return false
                }else{
                    getChapter(--chapterNum, dirName!!, false)
                }
            }else{
                pageNum--
                invalidate()
            }
        }
        readRecord = "$chapterNum#$pageNum"
        mViewModel?.setRecord(bookName!!, readRecord!!)
        return false
    }

    override fun onLongPress(e: MotionEvent) {

    }

    override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {

        return false
    }

    override fun onShowPress(e: MotionEvent) {

    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {

        return false
    }

    fun getIndicator(chapterNum: Int, pageNum: Int, maxPageNum: Int): String{
        return "第${chapterNum}章:${pageNum}/$maxPageNum"
    }

    fun getChapter(chapterNum: Int, dirName: String, isNext: Boolean){
        Observable.create<String> { e ->
            e.onNext(mViewModel?.getChapterText(chapterNum, dirName) ?: "")
        }
                .subscribeOn(Schedulers.io())
                .map { chapter -> mViewModel!!.splitChapterTxt(chapter,width,height,txtFontSize) }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { list ->
                    arrayList = list
                    maxPageNum = arrayList.size
                    if(isNext && maxPageNum != 0){
                        pageNum = 1
                    }else{
                        pageNum = maxPageNum
                    }
                    invalidate()
                }
    }


}