package com.netnovelreader.bean

import android.databinding.BaseObservable
import android.databinding.ObservableField
import android.graphics.Bitmap

/**
 * Created by yangbo on 18-1-12.
 */
data class BookBean(
    var bookname: ObservableField<String>,
    var latestChapter: ObservableField<String>,
    var downloadURL: ObservableField<String>,
    var bitmap: ObservableField<Bitmap>,
    var isUpdate: ObservableField<String>
) : BaseObservable()