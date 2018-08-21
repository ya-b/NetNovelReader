package com.netnovelreader.vm

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import com.netnovelreader.repo.BookInfosRepo
import com.netnovelreader.repo.db.BookInfoEntity
import com.netnovelreader.repo.db.ChapterInfoEntity
import com.netnovelreader.repo.db.ReaderDatabase
import com.netnovelreader.repo.http.resp.ChapterInfoResp
import com.netnovelreader.utils.IO_EXECUTOR
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers

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
    var compositeDisposable: CompositeDisposable = CompositeDisposable()

    fun destroy() {
        compositeDisposable.clear()
    }

    fun readBook(bookname: String) {
        startReaderFrag.value = StringBuilder(bookname)
        repo.setMaxOrderToBook(bookname)
    }

    fun askForDelBook(bookname: String): Boolean {
        dialogCommand.value = StringBuilder(bookname)
        return true // onLongClick的返回值
    }

    fun deleteBook(bookname: String) {
        if (bookname.isNotEmpty()) {
            repo.deleteBook(bookname)
        }
    }

    fun updateBooks() {
        repo.updateCatalog()
            .subscribeOn(Schedulers.from(IO_EXECUTOR))
            .flatMap {
                Observable.zip(
                    Observable.just(it),
                    repo.getChapterNum(it.first.bookname).toObservable(),
                    BiFunction<Pair<BookInfoEntity, List<ChapterInfoResp>>, Int,
                            Triple<BookInfoEntity, List<ChapterInfoResp>, Int>>
                    { t1, t2 -> Triple(t1.first, t1.second, t2) }
                )
            }.map {
                if(it.third < it.second.size) {
                    Pair(it.first, it.second.subList(it.third, it.second.size))
                } else {
                    Pair(it.first, emptyList())
                }
            }
            .subscribe(
                {
                    if(it.second.isEmpty()) return@subscribe
                    it.second.map { c ->
                        ChapterInfoEntity(
                            null, c.id, it.first.bookname, c.chapterName,
                            c.chapterUrl, ReaderDatabase.NOT_DOWN
                        )
                    }.let { list -> repo.insertChapters(*list.toTypedArray()) }
                    it.first.latestChapter = it.second.last().chapterName
                    it.first.hasUpdate = true
                    repo.updateBookInfo(it.first)
                }, {
                    stopRefershCommand.postValue(null)
                }, {
                    stopRefershCommand.postValue(null)
                }
            ).let { compositeDisposable.add(it) }
    }
}