package com.netnovelreader.shelf

import android.content.Context
import android.graphics.Bitmap
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet

/**
 * Created by yangbo on 2018/1/26.
 */
class BookCoverImage : AppCompatImageView {
    var text: Bitmap? = null
        set(value) {
            field = value
            super.setImageBitmap(value)
        }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )
}