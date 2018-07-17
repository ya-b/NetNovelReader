package com.netnovelreader.repo

import android.app.Application
import com.netnovelreader.repo.db.BookInfoEntity
import com.netnovelreader.repo.db.ChapterInfoEntity
import com.netnovelreader.repo.db.ReaderDatabase
import com.netnovelreader.repo.http.resp.ChapterInfoResp
import com.netnovelreader.repo.http.resp.SearchBookResp
import com.netnovelreader.utils.IO_EXECUTOR
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

class BookInfosRepo(app: Application) : Repo(app) {
    private var dao = db.bookInfoDao()

    fun getAllBookInfos() =
        dao.allBooks()

    fun deleteBook(bookname: String) {
        dao.getBookInfo(bookname)?.also { dao.delete(it) }
    }

    fun setMaxOrderToBook(bookname: String) {
        dao.getBookInfo(bookname)
            ?.also {
                it.orderNumber = dao.getMaxOrderNum() + 1
                it.hasUpdate = false
            }
            ?.also { dao.update(it) }

    }

    fun updateCatalog(block: ((Boolean) -> Unit)? = null) {
        Observable.create<List<BookInfoEntity>> {
            it.onNext(dao.getAll())
            it.onComplete()
        }.subscribeOn(Schedulers.from(IO_EXECUTOR))
            .flatMap { Observable.fromIterable(it) }
            .subscribeOn(Schedulers.from(IO_EXECUTOR))
            .flatMap {
                Observable.create<Pair<BookInfoEntity, List<ChapterInfoResp>>> { emitter ->
                    val list = getCatalog(
                        SearchBookResp(
                            it.bookname, it.downloadUrl,
                            it.coverPath, it.latestChapter
                        )
                    )
                    emitter.onNext(Pair(it, list))
                    emitter.onComplete()
                }.subscribeOn(Schedulers.from(IO_EXECUTOR))
            }
            .subscribe(
                {
                    writeToDB(it.first, it.second)
                },
                {
                    block?.invoke(false)
                },
                {
                    block?.invoke(true)
                }
            )
    }

    private fun writeToDB(bookInfoEntity: BookInfoEntity, list: List<ChapterInfoResp>) {
        val exists = db.chapterInfoDao().getAll(bookInfoEntity.bookname)
        //大于int的表示更新章节， ChapterInfoEntity表示数据库里的最后一章
        var index = Pair(0, ChapterInfoEntity(null, 0, "", "", "", 0))
        for (i in list.size - 1 downTo 0) {
            val entity = exists.filter { it.chapterName == list[i].chapterName }
            if (entity.isNotEmpty()) {
                index = Pair(i, entity.last())
                break
            }
        }
        if (index.first < list.size - 1) {
            list.subList(index.first + 1, list.size)
                .apply {
                    forEachIndexed { i, chapterInfoResp ->
                        chapterInfoResp.id = index.first + i + 1
                    }
                }.map {
                    ChapterInfoEntity(
                        null, it.id, bookInfoEntity.bookname, it.chapterName,
                        it.chapterUrl, ReaderDatabase.NOT_DOWN
                    )
                }.let { db.chapterInfoDao().insert(*it.toTypedArray()) }
            bookInfoEntity.apply {
                latestChapter = list.lastOrNull()?.chapterName ?: ""
                hasUpdate = true
            }.let { dao.update(it) }
        }
    }
}