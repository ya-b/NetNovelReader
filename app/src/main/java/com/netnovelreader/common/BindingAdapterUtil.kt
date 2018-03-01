package com.netnovelreader.common

import android.databinding.BindingAdapter
import android.graphics.Bitmap
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.ImageView
import com.netnovelreader.GlideApp
import com.netnovelreader.R
import com.netnovelreader.customview.ReaderView

@BindingAdapter("android:src")
fun loadUrl(imageView: ImageView, url: String?) {
    url ?: return
    GlideApp.with(imageView.context)
        .load(if (!url.contains("http://")) "http://statics.zhuishushenqi.com$url-covers" else url)//补全Url
        .error(ContextCompat.getDrawable(imageView.context, R.drawable.cover_default))
        .centerCrop()
        .into(imageView)
}

@BindingAdapter("android:src")
fun setSrc(imageView: ImageView, bitmap: Bitmap) {
    imageView.setImageBitmap(bitmap)
}

@BindingAdapter("android:textSize")
fun setTextSize(readerView: ReaderView, float: Float) {
    readerView.txtFontSize = float
}

@BindingAdapter("app:textFontType")
fun setFontType(readerView: ReaderView, typeface: Typeface) {
    readerView.txtFontType = typeface
}

@BindingAdapter("android:textColor")
fun setTextColor(readerView: ReaderView, color: Int) {
    readerView.txtFontColor = color
}

@BindingAdapter("android:background")
fun setBgColog(readerView: ReaderView, color: Int) {
    readerView.bgColor = color
}

@BindingAdapter("android:doDrawPrepare")
fun setDoDrawPrepare(readerView: ReaderView, doDrawPrepare: ReaderView.DoDrawPrepare){
    readerView.doDrawPrepare = doDrawPrepare
}

@BindingAdapter("android:onCenterClick")
fun setOnCenterClick(readerView: ReaderView, onCenterClick: ReaderView.OnCenterClick){
    readerView.onCenterClick = onCenterClick
}

@BindingAdapter("android:nextChapter")
fun setNextChapter(readerView: ReaderView, nextChapter: ReaderView.NextChapter){
    readerView.nextChapter = nextChapter
}

@BindingAdapter("android:previousChapter")
fun setPreviousChapter(readerView: ReaderView, previousChapter: ReaderView.PreviousChapter){
    readerView.previousChapter = previousChapter
}

@BindingAdapter("android:nextPage")
fun setOnPageChange(readerView: ReaderView, onPageChange: ReaderView.OnPageChange){
    readerView.onPageChange = onPageChange
}

@BindingAdapter("android:onRefresh")
fun setRefershListener(refreshLayout: SwipeRefreshLayout, listener: SwipeRefreshLayout.OnRefreshListener){
    refreshLayout.setOnRefreshListener(listener)
}

@BindingAdapter("app:navigationOnClick")
fun setNavigationOnClickListener(toobar: Toolbar, listener: View.OnClickListener){
    toobar.setNavigationOnClickListener(listener)
}