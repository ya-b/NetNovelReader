package com.netnovelreader.repo

import android.app.Application
import com.netnovelreader.repo.db.BookInfoEntity
import com.netnovelreader.repo.db.ChapterInfoEntity
import com.netnovelreader.repo.db.ReaderDatabase
import com.netnovelreader.repo.http.resp.ChapterInfoResp
import com.netnovelreader.repo.http.resp.SearchBookResp
import com.netnovelreader.utils.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File

class ChapterInfoRepo(app: Application) : Repo(app) {
    private var chapterDao = db.chapterInfoDao()

    fun getAllChapters(bookname: String) = ioThreadFuture { db.chapterInfoDao().getAll(bookname) }

    fun getChapterInfo(bookname: String, chapterNum: Int) =
        ioThreadFuture { db.chapterInfoDao().getChapterInfo(bookname, chapterNum) }

    fun getChapterInfo(bookname: String, chapterName: String, block: (Int) -> Unit) {
        ioThread {
            val entity = db.chapterInfoDao().getChapterInfo(bookname, chapterName)
            block.invoke(entity?.chapterNum ?: 0)
        }
    }

    fun getRecord(bookname: String) =
        ioThreadFuture {
            db.bookInfoDao().getBookInfo(bookname)?.readRecord
                ?.split("#")?.map { it.toInt() } ?: listOf(1, 1)
        }!!

    fun setRecord(bookname: String, chapterNum: Int, pageNum: Int) {
        ioThread {
            db.bookInfoDao().run {
                getBookInfo(bookname)
                    ?.apply { readRecord = "$chapterNum#${if (pageNum < 1) 1 else pageNum}" }
                    ?.let { update(it) }
            }
        }
    }

    fun getChapter(
        bookname: String,
        chapterNum: Int,
        block: ((String, Boolean) -> Unit)? = null
    ) {
        ioThread {
            chapterDao.getChapterInfo(bookname, chapterNum).also {
                if (it == null) {
                    block?.invoke("", false)
                } else if (it.isDownloaded == ReaderDatabase.ALREADY_DOWN
                    && File(bookDir(bookname), chapterNum.toString()).exists()) {
                    getChapterFromDisk(it, block)
                } else {
                    getChapterFromNet(it, block)
                }
            }
        }
    }

    fun downloadCatalog(bookname: String, block: (List<ChapterInfoEntity>) -> Unit) {
        Observable.create<BookInfoEntity?> {
            val info = db.bookInfoDao().getBookInfo(bookname)
            if (info == null) {
                it.onError(Throwable("error on get book info"))
            } else {
                it.onNext(info)
                it.onComplete()
            }
        }
            .subscribeOn(Schedulers.from(IO_EXECUTOR))
            .observeOn(Schedulers.from(IO_EXECUTOR))
            .flatMap { info ->
                Observable.create<List<ChapterInfoResp>> {
                    getCatalog(
                        SearchBookResp(
                            bookname, info.downloadUrl, info.coverPath, info.latestChapter
                        )
                    ).run {
                        it.onNext(this)
                        it.onComplete()
                    }
                }
            }.observeOn(Schedulers.from(IO_EXECUTOR))
            .subscribe (
                {
                    it.map {
                        ChapterInfoEntity( null, it.id, bookname, it.chapterName, it.chapterUrl,
                            ReaderDatabase.NOT_DOWN )
                    }.also { db.chapterInfoDao().insert(*it.toTypedArray()) }
                        .also { block.invoke(it) }
                },
                {
                    block.invoke(emptyList())
                }
            )

    }

    private fun getChapterFromNet(
        entity: ChapterInfoEntity,
        block: ((String, Boolean) -> Unit)? = null
    ) {
        Observable.create<String> {
            val str = getChapter(
                ChapterInfoResp(
                    entity.chapterNum,
                    entity.chapterName,
                    entity.chapterUrl
                )
            )
            if (mkBookDir(entity.bookname)) {
                File(bookDir(entity.bookname), entity.chapterNum.toString()).writeText(str)
                db.chapterInfoDao()
                    .getChapterInfo(entity.bookname, entity.chapterNum)
                    ?.also { it.isDownloaded = ReaderDatabase.ALREADY_DOWN }
                    ?.also { db.chapterInfoDao().update(it) }
            }
            it.onNext(str)
            it.onComplete()
        }.subscribeOn(Schedulers.from(IO_EXECUTOR))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { block?.invoke(it, false) },
                { block?.invoke("", true) }
            )
    }

    private fun getChapterFromDisk(
        entity: ChapterInfoEntity,
        block: ((String, Boolean) -> Unit)? = null
    ) {
        val file = File(bookDir(entity.bookname), entity.chapterNum.toString())  //章节文件地址
        Observable.create<String> {
            it.onNext(file.readText())
            it.onComplete()
        }.subscribeOn(Schedulers.from(IO_EXECUTOR))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { block?.invoke(it, false) },
                { block?.invoke("", true) }
            )
    }

    fun downCacheChapter(bookname: String, chapterNum: Int, cacheSize: Int) {
        if(cacheSize == 0) return
        ioThread {
            chapterDao.getRangeChapter(bookname, chapterNum + 1, chapterNum + cacheSize)
                .forEach {
                    if (it.isDownloaded != ReaderDatabase.ALREADY_DOWN
                        && File(bookDir(bookname), chapterNum.toString()).exists()) {
                        getChapterFromNet(it)
                    }
                }
        }
    }

    fun delCacheChapter(bookname: String, chapterNum: Int, preserveSize: Int) {
        if(preserveSize == 0) return
        ioThread {
            chapterDao.getRangeChapter(bookname, 1, chapterNum - preserveSize)
                .map { File(bookDir(bookname), it.chapterNum.toString()) }
                .forEach { it.deleteRecursively() }
        }
    }
}