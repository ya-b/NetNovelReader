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
        if(bookname.isNotEmpty()) {
            repo.deleteBook(bookname)
        }
    }

    fun updateBooks() {
        repo.updateCatalog()
            .subscribeOn(Schedulers.from(IO_EXECUTOR))
            .flatMap {
                Observable.zip(Observable.just(it),
                    repo.getAllChapter(it.first.bookname).toObservable(),
                    BiFunction<Pair<BookInfoEntity, List<ChapterInfoResp>>, List<ChapterInfoEntity>,
                            Triple<BookInfoEntity, List<ChapterInfoResp>, List<ChapterInfoEntity>>>
                    { t1, t2 -> Triple(t1.first, t1.second, t2) })
            }
            .subscribe(
                { triple ->
                    val bookInfoEntity = triple.first
                    val newChapters = triple.second
                    val existsChapters = triple.third
                    //大于int的表示更新章节， ChapterInfoEntity表示数据库里的最后一章
                    var index = Pair(0, ChapterInfoEntity(null, 0, "", "", "", 0))
                    for (i in newChapters.size - 1 downTo 0) {
                        val entity = existsChapters.filter { it.chapterName == newChapters[i].chapterName }
                        if (entity.isNotEmpty()) {
                            index = Pair(i, entity.last())
                            break
                        }
                    }
                    if (index.first < newChapters.size - 1) {
                        newChapters.subList(index.first + 1, newChapters.size)
                            .apply {
                                forEachIndexed { i, chapterInfoResp ->
                                    chapterInfoResp.id = index.first + i + 1
                                }
                            }.map {
                                ChapterInfoEntity(
                                    null, it.id, bookInfoEntity.bookname, it.chapterName,
                                    it.chapterUrl, ReaderDatabase.NOT_DOWN
                                )
                            }.let { repo.insertChapters(*it.toTypedArray()) }
                        bookInfoEntity.apply {
                            latestChapter = newChapters.lastOrNull()?.chapterName ?: ""
                            hasUpdate = true
                        }.let { repo.updateBookInfo(it) }
                    }
                }, {
                    stopRefershCommand.postValue(null)
                }, {
                    stopRefershCommand.postValue(null)
                }
            ).let { compositeDisposable.add(it) }
    }
}