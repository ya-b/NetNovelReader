package com.netnovelreader.vm

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import com.netnovelreader.repo.BookInfosRepo
import com.netnovelreader.utils.ioThread

class ShelfViewModel(var repo: BookInfosRepo, app: Application) : AndroidViewModel(app) {
    var allBooks = LivePagedListBuilder(
        repo.getAllBookInfos(),
        PagedList.Config.Builder()
            .setPageSize(10)
            .setEnablePlaceholders(false)
            .build()
    ).build()
    var startReaderFrag: MutableLiveData<StringBuilder> = MutableLiveData()

    fun readBook(bookname: String) {
        startReaderFrag.value = StringBuilder(bookname)
        ioThread { repo.setMaxOrderToBook(bookname) }
    }

    fun delBook(bookname: String): Boolean {
        ioThread { repo.deleteBook(bookname) }
        return true // onLongClick的返回值
    }
}