package com.netnovelreader.shelf

import android.content.Context
import android.graphics.Bitmap
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import com.netnovelreader.GlideApp
import com.netnovelreader.R


/**
 * Created by yangbo on 2018/1/26.
 */
class BookCoverImage : AppCompatImageView {
    var text: Bitmap? = null
        set(value) {
            field = value
            super.setImageBitmap(value)
        }
    var url: String? = null
        set(value) {
            field = value
            field?.let {
                field = if (!field!!.contains("http://")) "http://statics.zhuishushenqi.com$field-covers" else field   //补全Url
                GlideApp.with(context)
                        .load(field)
                        .error(ContextCompat.getDrawable(context, R.drawable.cover_default))
                        .centerCrop()
                        .into(this)
            }
        }

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
}