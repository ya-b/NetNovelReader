package com.netnovelreader.common

import android.databinding.BindingAdapter
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
import com.netnovelreader.ReaderApplication
import com.netnovelreader.data.BookCoverCache
import com.netnovelreader.data.network.WebService
import com.netnovelreader.interfaces.OnProgressChangedListener
import com.netnovelreader.interfaces.OnScrolledListener
import com.netnovelreader.interfaces.OnTabUnselectedListener
import com.pageview.PageListener
import com.pageview.PageView
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import java.io.File

@BindingAdapter("android:src")
fun loadUrl(imageView: ImageView, url: String?) = runBlocking {
    url ?: return@runBlocking
    val realUrl =
        if (!url.contains("http://")) "http://statics.zhuishushenqi.com$url-covers" else url
    val bitmap = BookCoverCache.get(url)
            ?: async(ReaderApplication.threadPool) {
                tryIgnoreCatch {
                    WebService.novelReader.getPicture(realUrl).execute().body()
                }.let {
                    if (it != null) BitmapFactory.decodeStream(it.byteStream()) else null
                }
            }.await()?.also { BookCoverCache.add(url, it) }
    if (bitmap != null) {
        imageView.setImageBitmap(bitmap)
    }
}

@BindingAdapter("android:src")
fun setSrc(imageView: ImageView, bookname: String?) {
    if (bookname.isNullOrEmpty()) return
    val bitmap = File("${ReaderApplication.dirPath}/$bookname", "$bookname.png")
        .takeIf { it.exists() }
        ?.let { BitmapFactory.decodeFile(it.path) }
    if (bitmap == null) {
        imageView.setImageDrawable(
            ContextCompat.getDrawable(imageView.context, R.drawable.cover_default)
        )
    } else {
        imageView.setImageBitmap(bitmap)
    }
}

@BindingAdapter("android:backgroundColor")
fun setBackground(pageView: PageView, background: Int) {
    pageView.backgroundcolor = background
}

@BindingAdapter("android:textFontType")
fun setFontType(readerView: PageView, typeface: Typeface?) {
    readerView.txtFontType = typeface
}

@BindingAdapter("android:doDrawPrepare")
fun setDoDrawPrepare(readerView: PageView, doDrawPrepare: PageListener.DoDrawPrepare?) {
    readerView.doDrawPrepare = doDrawPrepare
}

@BindingAdapter("android:onCenterClick")
fun setOnCenterClick(readerView: PageView, onCenterClick: PageListener.OnCenterClick?) {
    readerView.onCenterClick = onCenterClick
}

@BindingAdapter("android:onNextChapter")
fun setNextChapter(readerView: PageView, onNextChapter: PageListener.OnNextChapter?) {
    readerView.onNextChapter = onNextChapter
}

@BindingAdapter("android:onPreviousChapter")
fun setPreviousChapter(readerView: PageView, onPreviousChapter: PageListener.OnPreviousChapter?) {
    readerView.onPreviousChapter = onPreviousChapter
}

@BindingAdapter("android:onPageChange")
fun setOnPageChange(readerView: PageView, onPageChange: PageListener.OnPageChange?) {
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
            val i = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            listener.onScrolled(dy, RecyclerView.SCROLL_STATE_DRAGGING, i == 1)
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            val i = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
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