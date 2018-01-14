package com.netnovelreader.shelf

import android.databinding.BaseObservable
import android.databinding.ObservableArrayList
import com.netnovelreader.base.IModel
import java.util.Vector

/**
 * Created by yangbo on 18-1-12.
 */
class ShelfModel : BaseObservable() {
    var bookList: ObservableArrayList<BookInfoBean>
    init {
        bookList = ObservableArrayList<BookInfoBean>()
    }
    class BookInfoBean(var bookid: Int,
                       var bookname: String,
                       var latestChapter: String,
                       var readRecord: String,
                       var downloadURL: String
    )
}