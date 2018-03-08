package com.netnovelreader.common

import android.databinding.BindingAdapter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.ImageView
import com.netnovelreader.R
import com.netnovelreader.customview.ReaderView
import com.netnovelreader.data.network.ApiManager
import com.netnovelreader.interfaces.OnScrolledListener

@BindingAdapter("android:src")
fun loadUrl(imageView: ImageView, url: String?) {
    url ?: return
    val realUrl = if (!url.contains("http://")) "http://statics.zhuishushenqi.com$url-covers" else url
    ApiManager.novelReader.getPicture(realUrl).enqueueCall {
        it?.let { BitmapFactory.decodeStream(it.byteStream()) }?.let { imageView.setImageBitmap(it) }
    }
}

@BindingAdapter("android:src")
fun setSrc(imageView: ImageView, bitmap: Bitmap?) {
    if (bitmap == null) {
        imageView.setImageDrawable(
                ContextCompat.getDrawable(imageView.context, R.drawable.cover_default)
        )
    } else {
        imageView.setImageBitmap(bitmap)
    }
}

@BindingAdapter("android:textSize")
fun setTextSize(readerView: ReaderView, float: Float?) {
    readerView.txtFontSize = float
}

@BindingAdapter("android:textFontType")
fun setFontType(readerView: ReaderView, typeface: Typeface?) {
    readerView.txtFontType = typeface
}

@BindingAdapter("android:textColor")
fun setTextColor(readerView: ReaderView, color: Int) {
    readerView.txtFontColor = color
}

@BindingAdapter("android:background")
fun setBgColog(readerView: ReaderView, color: Int?) {
    readerView.bgColor = color
}

@BindingAdapter("android:doDrawPrepare")
fun setDoDrawPrepare(readerView: ReaderView, doDrawPrepare: ReaderView.DoDrawPrepare?) {
    readerView.doDrawPrepare = doDrawPrepare
}

@BindingAdapter("android:onCenterClick")
fun setOnCenterClick(readerView: ReaderView, onCenterClick: ReaderView.OnCenterClick?) {
    readerView.onCenterClick = onCenterClick
}

@BindingAdapter("android:nextChapter")
fun setNextChapter(readerView: ReaderView, nextChapter: ReaderView.NextChapter?) {
    readerView.nextChapter = nextChapter
}

@BindingAdapter("android:previousChapter")
fun setPreviousChapter(readerView: ReaderView, previousChapter: ReaderView.PreviousChapter?) {
    readerView.previousChapter = previousChapter
}

@BindingAdapter("android:onPageChange")
fun setOnPageChange(readerView: ReaderView, onPageChange: ReaderView.OnPageChange?) {
    readerView.onPageChange = onPageChange
}

@BindingAdapter("android:onRefresh")
fun setRefershListener(
        refreshLayout: SwipeRefreshLayout,
        listener: SwipeRefreshLayout.OnRefreshListener?
) {
    refreshLayout.setOnRefreshListener(listener)
}

@BindingAdapter("android:navigationOnClick")
fun setNavigationOnClickListener(toobar: Toolbar, listener: View.OnClickListener?) {
    toobar.setNavigationOnClickListener(listener)
}

@BindingAdapter("android:onScroll")
fun setOnScrolledListener(recyclerView: RecyclerView, listener: OnScrolledListener) {
    recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            listener.onScrolled(dy)
        }
    })
}