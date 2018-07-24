package com.pageview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ViewFlipper
import kotlin.reflect.KProperty

class PageView : ViewFlipper {
    var pageNum: Int? by InvalidateAfterSet(1)                    //页数
    var backgroundcolor by InvalidateAfterSet(Color.WHITE)
    var textColor by InvalidateAfterSet(Color.BLACK)                    //字体颜色
    var txtFontType: Typeface? by InvalidateAfterSet(Typeface.DEFAULT)  //正文字体类型//背景颜色
    var rowSpace: Float? by InvalidateAfterSet(0.5f)               //行距
    var textSize: Float? by InvalidateAfterSet(50f)               //正文部分默认画笔的大小
    var bottomTextSize: Float = 35f                                     //底部部分默认画笔的大小
    var text: String? by InvalidateAfterSet(null)                 //一个未分割章节,格式：章节名|正文
    var title: String = ""                                             //章节名称

    val FILENOTFOUND = "            "         //表示该章节为空
    var isDrawTime = false                    //左下角是否显示时间
    var maxPageNum = 0                        //最大页数
    var pageFlag = 0                          //0刚进入view，1表示目录跳转，2表示下一页，3表示上一页
    private val MIN_MOVE = 80F                //翻页最小滑动距离

    var textArray = ArrayList<ArrayList<String>>()

    var isMoved = false                       //手势判断
    var isTouching = false                    //手势判断
    var moveStart = FloatArray(2)        //手势判断
    var moveEnd = FloatArray(2)          //手势判断

    var doDrawPrepare: PageListener.DoDrawPrepare? = null          //第一次绘制时调用
    var onNextChapter: PageListener.OnNextChapter? = null          // 下一章
    var onPreviousChapter: PageListener.OnPreviousChapter? = null  //上一章
    var onCenterClick: PageListener.OnCenterClick? = null          //点击view中间部分
    var onPageChange: PageListener.OnPageChange? = null            //当翻页时调用，向前向后翻页，同一章内翻页，翻至其他章节都会调用

    constructor(context: Context) : super(context){
        init(context)
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){
        init(context)
    }

    fun init(context: Context){
        PageContent(context).apply {
            mBgColor = backgroundcolor!!
            mTextSize = textSize!!
            mBottomTextSize = bottomTextSize
            mIsDrawTime = isDrawTime
            mRowSpace = rowSpace!!
            mPageNum = 0
            mMaxPageNum = 0
            mTextColor = textColor!!
            mTxtFontType = txtFontType!!
            this@PageView.addView(this)
        }
        PageContent(context).apply {
            mBgColor = backgroundcolor!!
            mTextSize = textSize!!
            mBottomTextSize = bottomTextSize
            mIsDrawTime = isDrawTime
            mRowSpace = rowSpace!!
            mPageNum = 0
            mMaxPageNum = 0
            mTextColor = textColor!!
            mTxtFontType = txtFontType!!
            this@PageView.addView(this)
        }
        showNext()
    }

    fun prepare(pageNum: Int) {
        this.pageNum = pageNum
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when(event.action){
            MotionEvent.ACTION_DOWN -> {
                moveStart = floatArrayOf(event.x, event.y)
                isTouching = true
                isMoved = false
            }
            MotionEvent.ACTION_MOVE -> {
                moveEnd = floatArrayOf(event.x, event.y)
                isMoved = true
            }
            MotionEvent.ACTION_UP -> {
                moveEnd = floatArrayOf(event.x, event.y)
                isTouching = false
            }
        }
        if(!isTouching && (!isMoved || (isMoved && Math.abs(moveStart[0] - moveEnd[0]) < 5f  //点击事件
                        && Math.abs(moveStart[1] - moveEnd[1]) < 5f))){
            when {
                moveStart[0] > width * 3 / 5 -> pageToNext(Orientation.horizontal)
                moveStart[0] < width * 2 / 5 -> pageToPrevious(Orientation.horizontal)
                moveStart[1] > height * 2 / 5 && moveStart[1] < height * 3 / 5 -> {
                    setInAnimation(getContext(), R.anim.slide_in_right)
                    setOutAnimation(getContext(), R.anim.slide_out_left)
                    onCenterClick?.onCenterClick()
                    pageFlag = 1
                }
            }
        } else if(!isTouching && isMoved && Math.abs(moveStart[0] - moveEnd[0]) > MIN_MOVE &&  //滑动左右翻页
                Math.abs(moveStart[1] - moveEnd[1]) < MIN_MOVE){
            when {
                moveStart[0] > moveEnd[0] -> pageToNext(Orientation.horizontal)
                else -> pageToPrevious(Orientation.horizontal)
            }
        } else if(!isTouching && isMoved && Math.abs(moveStart[1] - moveEnd[1]) > MIN_MOVE * height / width &&  //滑动上下翻页
                Math.abs(moveStart[0] - moveEnd[0]) < MIN_MOVE){
            when {
                moveStart[1] > moveEnd[1] -> pageToNext(Orientation.vertical)
                else -> pageToPrevious(Orientation.vertical)
            }
        } else if(!isTouching && isMoved && Math.abs(moveStart[1] - moveEnd[1]) > MIN_MOVE * height / width &&
                Math.abs(moveStart[0] - moveEnd[0]) > MIN_MOVE){  //需要进一步判断翻页方向
            if(Math.abs(moveStart[1] - moveEnd[1]) / Math.abs(moveStart[0] - moveEnd[0]) > height / width){
                if (moveStart[1] > moveEnd[1]) {
                    pageToNext(Orientation.vertical)
                } else {
                    pageToPrevious(Orientation.vertical)
                }
            }else{
                if (moveStart[0] > moveEnd[0]) {
                    pageToNext(Orientation.horizontal)
                } else {
                    pageToPrevious(Orientation.horizontal)
                }
            }
        }
        return true
    }

    private fun pageToNext(orientation: Orientation) {
        if(orientation == Orientation.horizontal){
            setInAnimation(getContext(), R.anim.slide_in_right)
            setOutAnimation(getContext(), R.anim.slide_out_left)
        }else{
            setInAnimation(getContext(), R.anim.slide_in_bottom)
            setOutAnimation(getContext(), R.anim.slide_out_top)
        }
        pageFlag = 2
        if (pageNum!! < maxPageNum) {
            pageNum = pageNum!! + 1
        } else {
            onNextChapter?.onNextChapter()
        }
    }

    private fun pageToPrevious(orientation: Orientation) {
        if(orientation == Orientation.horizontal) {
            setInAnimation(getContext(), R.anim.slide_in_left)
            setOutAnimation(getContext(), R.anim.slide_out_right)
        }else{
            setInAnimation(getContext(), R.anim.slide_in_top)
            setOutAnimation(getContext(), R.anim.slide_out_bottom)
        }
        pageFlag = 3
        if (pageNum!! < 2) {
            onPreviousChapter?.onPreviousChapter()
        } else {
            pageNum = pageNum!! - 1
        }
    }

    private fun displayView(){
        val another = (displayedChild + 1) % 2
        (getChildAt(another) as PageContent).apply {
            mBgColor = backgroundcolor!!
            if(maxPageNum > 0){
                if(pageNum!! > textArray.size) pageNum = textArray.size
                if(pageNum == 0) pageNum = 1
                mTextArray = textArray[pageNum!! - 1]
            }
            mRowSpace = rowSpace!!
            mTextSize = textSize!!
            mPageNum = pageNum!!
            mMaxPageNum = maxPageNum
            mTextColor = textColor!!
            mTitle = title
            mTxtFontType = txtFontType!!
        }
        displayedChild = another
    }

    //正文区域宽度
    private fun getTextWidth(): Int = (width * 0.96f).toInt()

    //正文区域高度
    private fun getTextHeight(): Int = ((height - bottomTextSize) * 0.96f).toInt()

    private fun spliteText(text: String?): ArrayList<ArrayList<String>> {
        if (text.isNullOrEmpty() || getTextWidth() == 0) return ArrayList()
        title = text!!.substring(0, text.indexOf("|"))
        val tmpArray = text.substring(text.indexOf("|") + 1).split("\n")
        val tmplist = ArrayList<String>()
        tmpArray.forEach {
            val tmp = "  " + it.trim()
            val totalCount = getTextWidth() / textSize!!.toInt() //一行容纳字数
            for (i in 0..tmp.length / totalCount) {
                tmp.filterIndexed { index, _ -> index > i * totalCount - 1 && index < (i + 1) * totalCount }
                        .also { tmplist.add(it) }
            }
        }
        val arrayList = ArrayList<ArrayList<String>>()
        val totalCount = getTextHeight() / (textSize!! * rowSpace!!).toInt()  //一页容纳行数
        for (i in 0..tmplist.size / totalCount) {
            (tmplist.filterIndexed { index, _ -> index > i * totalCount - 1 && index < (i + 1) * totalCount } as ArrayList<String>)
                    .also { arrayList.add(it) }
        }
        return arrayList
    }

    inner class InvalidateAfterSet<T>(@Volatile var value: T? = null) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T? = value

        @Synchronized
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
            this.value = value
            when (property.name) {
                "rowSpace", "textSize" -> {
                    if (width < 1) return
                    val scale = maxPageNum.toFloat() / pageNum!!
                    textArray = spliteText(text!!)
                    maxPageNum = if (text!!.substring(title.length + 1).equals(FILENOTFOUND)
                        || text.isNullOrEmpty()) 0 else textArray.size
                    pageNum = (maxPageNum / scale).toInt().takeIf { it != 0 } ?: 1
                }
                "text" -> {
                    if (width < 1) return
                    textArray = spliteText(text!!)
                    maxPageNum = if (text!!.substring(title.length + 1).equals(FILENOTFOUND)
                        || text.isNullOrEmpty()) 0 else textArray.size
                    pageNum = when (pageFlag) {
                        0 -> if (maxPageNum == 0) 0 else if (pageNum == 0) 1 else pageNum
                        1, 2 -> if (maxPageNum == 0) 0 else 1
                        3 -> maxPageNum
                        else -> 1
                    }
                }
                "pageNum" -> {
                    onPageChange?.onPageChange(pageNum!!)
                    displayView()
                }
                else -> {
                    displayView()
                }  //刷新view
            }
        }
    }
}