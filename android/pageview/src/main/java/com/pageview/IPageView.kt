package com.pageview

import android.graphics.Typeface

interface IPageView {
    var pageNum: Int                    //页数
    var backgroundcolor: Int            //背景色
    var textColor : Int                 //字体颜色
    var txtFontType: Typeface           //正文字体类型//背景颜色
    var rowSpace: Float                 //行距
    var textSize: Float                 //正文部分默认画笔的大小
    var bottomTextSize: Float           //底部部分默认画笔的大小
    var text: String                    //一个未分割章节,格式：章节名|正文
    var isDrawTime: Boolean             //左下角是否显示时间

    var onNextChapter: IPageView.OnNextChapter?          // 下一章
    var onPreviousChapter: IPageView.OnPreviousChapter?  //上一章
    var onCenterClick: IPageView.OnCenterClick?          //点击view中间部分
    var onPageChange: IPageView.OnPageChange?            //当翻页时调用，向前向后翻页，同一章内翻页，翻至其他章节都会调用

    //页码初始化
    fun prepare(pageNum: Int)

    @FunctionalInterface
    interface OnCenterClick {
        fun onCenterClick()
    }

    @FunctionalInterface
    interface OnNextChapter {
        fun onNextChapter()
    }

    @FunctionalInterface
    interface OnPreviousChapter {
        fun onPreviousChapter()
    }

    @FunctionalInterface
    interface OnPageChange {
        fun onPageChange(index: Int)
    }
}