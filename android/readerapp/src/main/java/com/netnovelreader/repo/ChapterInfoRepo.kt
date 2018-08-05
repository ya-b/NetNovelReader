package com.netnovelreader.repo

import android.app.Application
import com.netnovelreader.repo.db.ChapterInfoEntity
import com.netnovelreader.repo.db.ReaderDatabase
import com.netnovelreader.repo.http.resp.ChapterInfoResp
import com.netnovelreader.repo.http.resp.SearchBookResp
import com.netnovelreader.utils.IO_EXECUTOR
import com.netnovelreader.utils.bookDir
import com.netnovelreader.utils.ioThread
import io.reactivex.Observable
import io.reactivex.SingleSource
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import java.io.File

class ChapterInfoRepo(app: Application) : Repo(app) {
    private var chapterDao = db.chapterInfoDao()

    fun getAllChapters(bookname: String) = db.chapterInfoDao().getAll(bookname)

    fun getChapterInfo(bookname: String, chapterNum: Int) =
        db.chapterInfoDao().getChapterInfo(bookname, chapterNum)

    fun getChapterInfo(bookname: String, chapterName: String) =
        db.chapterInfoDao().getChapterInfo(bookname, chapterName)

    fun getRecord(bookname: String) =
        db.bookInfoDao()
            .getBookInfo(bookname)
            .subscribeOn(Schedulers.from(IO_EXECUTOR))
            .flatMap {  info ->
                SingleSource<List<Int>> {
                    val arr = info.readRecord.split("#").map { it.toInt() }
                    it.onSuccess(arr)
                }
            }

    /**
     * 保存阅读记录
     */
    fun setRecord(bookname: String, chapterNum: Int, pageNum: Int) {
        db.bookInfoDao()
            .getBookInfo(bookname)
            .subscribeOn(Schedulers.from(IO_EXECUTOR))
            .subscribe(
                {
                    it.readRecord = "$chapterNum#${if (pageNum < 1) 1 else pageNum}"
                    db.bookInfoDao().update(it)
                },
                {
                    LoggerFactory.getLogger(this.javaClass).warn("setRecord$it")
                })
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
            }

    fun downloadCatalog(bookname: String) =
        db.bookInfoDao()
            .getBookInfo(bookname)
            .subscribeOn(Schedulers.from(IO_EXECUTOR))
            .flatMap { info ->
                getCatalog(
                    SearchBookResp(
                        bookname, info.downloadUrl, info.coverPath, info.latestChapter
                    )
                )
            }.map {
                it.map {
                    ChapterInfoEntity(
                        null, it.id, bookname, it.chapterName, it.chapterUrl,
                        ReaderDatabase.NOT_DOWN
                    )
                }
            }

    private fun getChapterFromNet(entity: ChapterInfoEntity) =
        getChapter(
            ChapterInfoResp(
                entity.chapterNum,
                entity.chapterName,
                entity.chapterUrl
            )
        ).subscribeOn(Schedulers.from(IO_EXECUTOR))
            .flatMap { str ->
                SingleSource<String> { singleObserver ->
                    File(bookDir(entity.bookname), entity.chapterNum.toString()).writeText(str)
                    db.chapterInfoDao()
                        .getChapterInfo(entity.bookname, entity.chapterNum).subscribe({
                            it.isDownloaded = ReaderDatabase.ALREADY_DOWN
                            db.chapterInfoDao().update(it)
                        }, {
                            singleObserver.onError(it)
                        })
                    singleObserver.onSuccess(str)
                }
            }

    private fun getChapterFromDisk(entity: ChapterInfoEntity) =
        SingleSource<String> {
            val file = File(bookDir(entity.bookname), entity.chapterNum.toString())  //章节文件地址
            it.onSuccess(file.readText())
        }

    fun downCacheChapter(bookname: String, chapterNum: Int, cacheSize: Int) {
        if (cacheSize == 0) return
        chapterDao.getRangeChapter(bookname, chapterNum + 1, chapterNum + cacheSize)
            .subscribeOn(Schedulers.from(IO_EXECUTOR))
            .flatMapObservable {
                it.filter { it.isDownloaded != ReaderDatabase.ALREADY_DOWN
                        || !File(bookDir(bookname), chapterNum.toString()).exists() }
                    .let { Observable.fromIterable(it) }
            }.flatMapSingle { getChapterFromNet(it) }
            .subscribe()
    }

    fun delCacheChapter(bookname: String, chapterNum: Int, preserveSize: Int) {
        if (preserveSize == 0) return
        chapterDao.getRangeChapter(bookname, 1, chapterNum - preserveSize)
            .subscribeOn(Schedulers.from(IO_EXECUTOR))
            .subscribe {
                it.map { File(bookDir(bookname), it.chapterNum.toString()) }
                    .forEach { it.deleteRecursively() }
            }
    }

    fun saveCatalog(list: List<ChapterInfoEntity>) {
        ioThread {
            db.chapterInfoDao().insert(*list.toTypedArray())
        }
    }
}