package com.netnovelreader.shelf

import android.databinding.BaseObservable
import android.databinding.ObservableArrayList
import android.databinding.ObservableField
import android.databinding.ObservableInt
import com.netnovelreader.base.IModel
import java.util.Vector

/**
 * Created by yangbo on 18-1-12.
 */
data class ShelfBean(var bookid: ObservableInt,
                     var bookname: ObservableField<String>,
                     var readRecord: ObservableField<String>,
                     var downloadURL: ObservableField<String>
) : BaseObservable()