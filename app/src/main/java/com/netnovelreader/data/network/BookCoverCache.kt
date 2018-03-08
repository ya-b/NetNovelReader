package com.netnovelreader.data.network

import android.graphics.Bitmap
import android.util.LruCache

object BookCoverCache {
    private val mCacheSize = Runtime.getRuntime().maxMemory() / 8
    private val mImageCache = object : LruCache<String, Bitmap>(mCacheSize.toInt()) {
        override fun sizeOf(key: String?, value: Bitmap) = value.byteCount
    }

    fun add(url: String, bitmap: Bitmap) = mImageCache.put(url, bitmap)
    fun get(url: String) = mImageCache.get(url)
}