package com.netnovelreader.repo

import android.app.Application
import com.netnovelreader.repo.db.BookInfoEntity
import com.netnovelreader.repo.db.ChapterInfoEntity
import com.netnovelreader.repo.db.ReaderDatabase
import com.netnovelreader.repo.http.resp.ChapterInfoResp
import com.netnovelreader.repo.http.resp.SearchBookResp
import com.netnovelreader.utils.IO_EXECUTOR
import com.netnovelreader.utils.bookDir
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory

class BookInfosRepo(app: Application) : Repo(app) {
    private var dao = db.bookInfoDao()

    fun getAllBookInfos() =
        dao.allBooks()

    fun deleteBook(bookname: String) {
        dao.getBookInfo(bookname)
            .subscribeOn(Schedulers.from(IO_EXECUTOR))
            .subscribe(
                {
                    dao.delete(it)
                    bookDir(bookname).deleteRecursively()
                },
                {
                    LoggerFactory.getLogger(this.javaClass).warn("deleteBook$it")
                })
    }

    fun setMaxOrderToBook(bookname: String) {
        Single.zip(
            dao.getBookInfo(bookname),
            dao.getMaxOrderNum().toSingle(),
            BiFunction<BookInfoEntity, Int, Pair<BookInfoEntity, Int>> { b, i -> Pair(b, i) }
        ).subscribeOn(Schedulers.from(IO_EXECUTOR))
            .subscribe(
                {
                    it.first.orderNumber = it.second + 1
                    it.first.hasUpdate = false
                    dao.update(it.first)
                },
                {
                    LoggerFactory.getLogger(this.javaClass).warn("setMaxOrderToBook$it")
                }
            )
    }

    fun updateCatalog() =
        dao.getAll()
            .subscribeOn(Schedulers.from(IO_EXECUTOR))
            .flatMapObservable { Observable.fromIterable(it) }
            .subscribeOn(Schedulers.from(IO_EXECUTOR))
            .flatMapSingle { entity ->
                getCatalog(
                    SearchBookResp(
                        entity.bookname, entity.downloadUrl, entity.coverPath, entity.latestChapter
                    )
                ).map { Pair(entity, it) }
                    .subscribeOn(Schedulers.from(IO_EXECUTOR))
            }

    fun writeToDB(bookInfoEntity: BookInfoEntity, list: List<ChapterInfoResp>) {
        db.chapterInfoDao()
            .getAll(bookInfoEntity.bookname)
            .subscribeOn(Schedulers.from(IO_EXECUTOR))
            .subscribe {
                //大于int的表示更新章节， ChapterInfoEntity表示数据库里的最后一章
                var index = Pair(0, ChapterInfoEntity(null, 0, "", "", "", 0))
                for (i in list.size - 1 downTo 0) {
                    val entity = it.filter { it.chapterName == list[i].chapterName }
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
}