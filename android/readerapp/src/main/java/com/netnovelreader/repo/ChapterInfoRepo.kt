package com.netnovelreader.repo

import android.app.Application
import com.netnovelreader.repo.db.BookInfoEntity
import com.netnovelreader.repo.db.ChapterInfoEntity
import com.netnovelreader.repo.db.ReaderDatabase
import com.netnovelreader.repo.http.resp.ChapterInfoResp
import com.netnovelreader.repo.http.resp.SearchBookResp
import com.netnovelreader.utils.IO_EXECUTOR
import com.netnovelreader.utils.bookDir
import com.netnovelreader.utils.ioThread
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.SingleSource
import io.reactivex.schedulers.Schedulers
import java.io.File

class ChapterInfoRepo(app: Application) : Repo(app) {
    private var chapterDao = db.chapterInfoDao()

    fun getAllChapters(bookname: String) = db.chapterInfoDao().getAll(bookname)

    fun getChapterInfo(bookname: String, chapterNum: Int) =
        db.chapterInfoDao().getChapterInfo(bookname, chapterNum)

    fun getChapterInfo(bookname: String, chapterName: String) =
        db.chapterInfoDao().getChapterInfo(bookname, chapterName)

    fun getChapterCount(bookname: String) = db.chapterInfoDao().getChapterNum(bookname)

    fun getBookInfo(bookname: String) = db.bookInfoDao().getBookInfo(bookname)

    fun updateBookInfo(bookInfoEntity: BookInfoEntity) =
        ioThread { db.bookInfoDao().update(bookInfoEntity) }

    fun getRecord(bookname: String) =
        getBookInfo(bookname)
            .subscribeOn(Schedulers.from(IO_EXECUTOR))
            .flatMap { info ->
                SingleSource<List<Int>> { so ->
                    val arr = info.readRecord.split("#").map { it.toInt() }
                    so.onSuccess(arr)
                }
            }

    fun getChapter(bookname: String, chapterNum: Int) =
        chapterDao.getChapterInfo(bookname, chapterNum)
            .flatMap {
                if (it.isDownloaded == ReaderDatabase.ALREADY_DOWN
                    && File(bookDir(bookname), chapterNum.toString()).exists()) {
                    getChapterFromDisk(it)
                } else {
                    getChapterFromNet(it)
                }
            }.flatMap {  str ->
                Single.create<String> {
                    if(str.isEmpty()) {
                        it.onError(Throwable("error"))
                    } else {
                        it.onSuccess(str)
                    }
                }
            }

    fun downloadCatalog(bookname: String) =
        getBookInfo(bookname)
            .subscribeOn(Schedulers.from(IO_EXECUTOR))
            .flatMap { info ->
                getCatalog(
                    SearchBookResp(
                        bookname, info.downloadUrl, info.coverPath, info.latestChapter
                    )
                )
            }.map { list ->
                list.map {
                    ChapterInfoEntity(
                        null, it.id, bookname, it.chapterName, it.chapterUrl,
                        ReaderDatabase.NOT_DOWN
                    )
                }
            }

    private fun getChapterFromNet(entity: ChapterInfoEntity) =
        getChapter(ChapterInfoResp(entity.chapterNum, entity.chapterName, entity.chapterUrl))
            .map {
                if(it.trim().isNotEmpty()) {
                    File(bookDir(entity.bookname), entity.chapterNum.toString()).writeText(it)
                    entity.isDownloaded = ReaderDatabase.ALREADY_DOWN
                    db.chapterInfoDao().update(entity)
                }
                it
            }

    private fun getChapterFromDisk(entity: ChapterInfoEntity) =
        SingleSource<String> {
            val file = File(bookDir(entity.bookname), entity.chapterNum.toString())  //章节文件地址
            it.onSuccess(file.readText())
        }

    fun downCacheChapter(bookname: String, chapterNum: Int, cacheSize: Int): Observable<String> =
        chapterDao.getRangeChapter(bookname, chapterNum + 1, chapterNum + cacheSize)
            .subscribeOn(Schedulers.from(IO_EXECUTOR))
            .flatMapObservable { chapters ->
                chapters.filter {
                    it.isDownloaded != ReaderDatabase.ALREADY_DOWN
                            || !File(bookDir(bookname), chapterNum.toString()).exists()
                }
                    .let { Observable.fromIterable(it) }
            }.flatMapSingle { getChapterFromNet(it) }

    fun delCacheChapter(bookname: String, chapterNum: Int, preserveSize: Int) =
        chapterDao.getRangeChapter(bookname, 1, chapterNum - preserveSize)
            .subscribeOn(Schedulers.from(IO_EXECUTOR))

    fun saveCatalog(list: List<ChapterInfoEntity>) {
        db.chapterInfoDao().insert(*list.toTypedArray())
    }
}