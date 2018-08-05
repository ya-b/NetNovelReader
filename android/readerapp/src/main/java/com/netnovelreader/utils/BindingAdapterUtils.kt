package com.netnovelreader.utils

import android.databinding.BindingAdapter
import android.text.TextUtils
import android.widget.ImageView
import android.widget.SeekBar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.netnovelreader.R
import com.netnovelreader.interfaces.OnProgressChangedListener
import com.pageview.IPageView
import com.pageview.PageView
import java.io.File

const val COVER_NAME = "cover.png"

@BindingAdapter("android:path")
fun loadPath(imageView: ImageView, path: String?) {
    path?.trim()?.takeIf { it.isNotEmpty() } ?: return
    val file = path.substring(0, path.indexOf("@")).let{ File(bookDir(it),COVER_NAME) }
    val p = if(file.exists()) {
        file.path
    } else {
        path.substring(path.indexOf("@") + 1)
    }
    val options = RequestOptions()
        .placeholder(R.drawable.cover_default)
        .error(R.drawable.cover_default)
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
    if(TextUtils.isEmpty(p)) {
        Glide.with(imageView).load(R.drawable.cover_default).into(imageView)
    } else {
        Glide.with(imageView).load(p).apply(options).into(imageView)
    }
}

@BindingAdapter("android:OnSeekBarChangeListener")
fun setOnSeekBarChangeListener(seekBar: SeekBar, listener: OnProgressChangedListener) {
    seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            listener.onProgressChanged(progress)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
        }
    })
}

@BindingAdapter("android:rowSpace")
fun setRowSpace(pageView: PageView, rowSpace: Float) {
    pageView.rowSpace = rowSpace
}

@BindingAdapter("android:backgroundColor")
fun setBackground(pageView: PageView, background: Int) {
    pageView.backgroundcolor = background
}

@BindingAdapter("android:onCenterClick")
fun setOnCenterClick(readerView: PageView, onCenterClick: IPageView.OnCenterClick?) {
    readerView.onCenterClick = onCenterClick
}

@BindingAdapter("android:onNextChapter")
fun setNextChapter(readerView: PageView, onNextChapter: IPageView.OnNextChapter?) {
    readerView.onNextChapter = onNextChapter
}

@BindingAdapter("android:onPreviousChapter")
fun setPreviousChapter(readerView: PageView, onPreviousChapter: IPageView.OnPreviousChapter?) {
    readerView.onPreviousChapter = onPreviousChapter
}

@BindingAdapter("android:onPageChange")
fun setOnPageChange(readerView: PageView, onPageChange: IPageView.OnPageChange?) {
    readerView.onPageChange = onPageChange
}