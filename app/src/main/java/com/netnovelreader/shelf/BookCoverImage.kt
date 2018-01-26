package com.netnovelreader.shelf

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.widget.ImageView

/**
 * Created by yangbo on 2018/1/26.
 */
class BookCoverImage : ImageView {
    var text: Bitmap? = null
        set(value) {
            field = value
            super.setImageBitmap(value)
        }
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)
}