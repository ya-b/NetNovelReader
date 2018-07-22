package com.netnovelreader.vm

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import com.netnovelreader.repo.BookInfosRepo

class ShelfViewModel(var repo: BookInfosRepo, app: Application) : AndroidViewModel(app) {
    var allBooks = LivePagedListBuilder(
        repo.getAllBookInfos(),
        PagedList.Config.Builder()
            .setPageSize(10)
            .setEnablePlaceholders(false)
            .build()
    ).build()
    var startReaderFrag = MutableLiveData<StringBuilder>()
    var dialogCommand = MutableLiveData<StringBuilder>()
    var stopRefershCommand = MutableLiveData<Void>()

    fun readBook(bookname: String) {
        startReaderFrag.value = StringBuilder(bookname)
        repo.setMaxOrderToBook(bookname)
    }

    fun askForDelBook(bookname: String): Boolean {
        dialogCommand.value = StringBuilder(bookname)
        return true // onLongClick的返回值
    }

    fun deleteBook(bookname: String) {
        if(bookname.isNotEmpty()) {
            repo.deleteBook(bookname)
        }
    }

    fun updateBooks() {
        repo.updateCatalog {
            stopRefershCommand.postValue(null)
        }
    }
}