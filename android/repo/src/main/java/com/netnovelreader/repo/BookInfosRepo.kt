package com.netnovelreader.repo

import android.app.Application
import com.netnovelreader.repo.db.BookInfoEntity
import com.netnovelreader.repo.db.ChapterInfoEntity
import com.netnovelreader.repo.http.resp.SearchBookResp
import com.netnovelreader.utils.IO_EXECUTOR
import com.netnovelreader.utils.bookDir
import com.netnovelreader.utils.ioThread
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

class BookInfosRepo(app: Application) : Repo(app) {
    private var dao = db.bookInfoDao()

    fun getAllBookInfos() = dao.allBooks()

    fun updateBookInfo(bookInfoEntity: BookInfoEntity) =
        ioThread { dao.update(bookInfoEntity) }

    fun getChapterNum(bookname: String) = db.chapterInfoDao().getChapterNum(bookname)

    fun insertChapters(vararg chapterInfoEntity: ChapterInfoEntity) {
        ioThread { db.chapterInfoDao().insert(*chapterInfoEntity) }
    }

    fun deleteBook(bookname: String) {
        ioThread {
            dao.delete(bookname)
            bookDir(bookname).deleteRecursively()
        }
    }

    fun setMaxOrderToBook(bookname: String) {
        ioThread { dao.setMaxOrderToBook(bookname) }
    }

    fun updateCatalog() =
        dao.getAll()
            .subscribeOn(Schedulers.from(IO_EXECUTOR))
            .flatMapObservable { Observable.fromIterable(it) }
            .flatMapSingle { entity ->
                getCatalog(
                    SearchBookResp(
                        entity.bookname, entity.downloadUrl, entity.coverPath, entity.latestChapter
                    )
                ).map { Pair(entity, it) }
                    .subscribeOn(Schedulers.from(IO_EXECUTOR))
            }
}