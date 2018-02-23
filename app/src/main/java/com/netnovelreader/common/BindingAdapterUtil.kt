package com.netnovelreader.common

import android.databinding.BindingAdapter
import android.graphics.Bitmap
import android.support.v4.content.ContextCompat
import android.widget.ImageView
import com.netnovelreader.GlideApp
import com.netnovelreader.R

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