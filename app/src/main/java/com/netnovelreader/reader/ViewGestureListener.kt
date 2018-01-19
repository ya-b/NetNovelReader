package com.netnovelreader.reader

import android.databinding.ObservableArrayList
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import com.netnovelreader.data.database.ShelfSQLManager
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by yangbo on 2018/1/19.
 */
class ViewGestureListener(val mViewModel: ReaderViewModel, val readerView: NewReaderView) :
        GestureDetector.OnGestureListener,NewReaderView.FirstDrawListener{
    /**
     * 最小滑动距离
     */
    val MIN_MOVE = 80F
    val MAX_MOVE = 5F
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
     * 拆分后的内容
     */
    var arrayList = ArrayList<ObservableArrayList<String>>()


    override fun doDrawPrepare() {
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

    override fun onDown(e: MotionEvent): Boolean {
        return true
    }

    @Synchronized
    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        val beginX = e1.getX()
        val endX = e2.getX()
        if(Math.abs(beginX - endX) < MIN_MOVE) {
            return false
        }else{
            if (beginX > endX) {
                if(!pageNext()) return false
            } else {
                if(!pagePrevious()) return false
            }
        }
        readRecord = "$chapterNum#$pageNum"
        mViewModel.setRecord(bookName!!, readRecord!!)
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

    fun pageNext(): Boolean{
        Log.d("listener==========","next page")
        Log.d("listener==========","$pageNum $maxChapterNum")
        if (pageNum == maxPageNum || maxPageNum < 2){
            if(chapterNum == maxChapterNum){
                return false
            }else{
                getChapter(++chapterNum, dirName!!, true)
            }
        }else{
            pageNum++
            updateText()
        }
        return true
    }

    fun pagePrevious(): Boolean{
        Log.d("listener==========","previous page")
        Log.d("listener==========","$pageNum $maxChapterNum")
        if (pageNum < 2){
            if(chapterNum == 1){
                return false
            }else{
                getChapter(--chapterNum, dirName!!, false)
            }
        }else{
            pageNum--
            updateText()
        }
        return true
    }

    fun getChapter(chapterNum: Int, dirName: String, isNext: Boolean){

        Observable.create<String> { e ->
            e.onNext(mViewModel.getChapterText(chapterNum, dirName))
        }
                .subscribeOn(Schedulers.io())
                .map { chapter -> mViewModel.splitChapterTxt(chapter,readerView.width,readerView.height,readerView.txtFontSize) }
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
                    updateText()
                }
    }

    fun updateText(){
        mViewModel.text.clear()
        mViewModel.text.add("第${chapterNum}章：$pageNum/$maxPageNum")
        if(arrayList.size > 0){
            arrayList.get(pageNum - 1).forEach {
                mViewModel.text.add(it)
            }
        }
    }
}