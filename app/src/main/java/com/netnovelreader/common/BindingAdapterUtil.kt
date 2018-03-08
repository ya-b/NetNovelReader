package com.netnovelreader.common

import android.databinding.BindingAdapter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import com.netnovelreader.R
import com.netnovelreader.data.network.ApiManager
import com.netnovelreader.data.network.BookCoverCache
import com.netnovelreader.interfaces.OnProgressChangedListener
import com.netnovelreader.interfaces.OnScrolledListener
import com.netnovelreader.interfaces.OnTabUnselectedListener
import com.netnovelreader.ui.customview.ReaderView

@BindingAdapter("android:src")
fun loadUrl(imageView: ImageView, url: String?) {
    url ?: return
    val realUrl =
        if (!url.contains("http://")) "http://statics.zhuishushenqi.com$url-covers" else url
    var bitmap = BookCoverCache.get(url)
    if (bitmap == null) {
        ApiManager.novelReader.getPicture(realUrl).enqueueCall {
            if (it == null) return@enqueueCall
            bitmap = BitmapFactory.decodeStream(it.byteStream())
            BookCoverCache.add(url, bitmap)
            imageView.setImageBitmap(bitmap)
        }
    } else {
        imageView.setImageBitmap(bitmap)
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
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val i =
                (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            listener.onScrolled(dy, RecyclerView.SCROLL_STATE_DRAGGING, i == 1)
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            val i =
                (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            listener.onScrolled(0, newState, i == 1)
        }
    })
}

@BindingAdapter("android:onTabUnselectedListener")
fun setTabUnselectedListener(tabLayout: TabLayout, listener: OnTabUnselectedListener) {
    tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {

        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
            listener.unselectedListener(tab?.text.toString())
        }

        override fun onTabReselected(tab: TabLayout.Tab?) {

        }
    })
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