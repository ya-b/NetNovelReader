package com.netnovelreader.repo

import android.app.Application
import com.netnovelreader.R
import com.netnovelreader.repo.db.BookInfoEntity
import com.netnovelreader.repo.db.ChapterInfoEntity
import com.netnovelreader.repo.db.ReaderDatabase
import com.netnovelreader.repo.http.WebService
import com.netnovelreader.repo.http.resp.ChapterInfoResp
import com.netnovelreader.repo.http.resp.SearchBookResp
import com.netnovelreader.utils.IO_EXECUTOR
import com.netnovelreader.utils.toMD5
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.SingleSource
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import org.slf4j.LoggerFactory
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class SearchRepo(app: Application) : Repo(app) {

    /**
     * 搜索书，如果已在搜索，则取消当前搜索，重新搜
     */
    fun search(bookname: String) =
        db.siteSelectorDao().getAll().subscribeOn(Schedulers.from(IO_EXECUTOR))
            .flatMapObservable { Observable.fromIterable(it) }
            .observeOn(Schedulers.from(IO_EXECUTOR))
            .flatMap { item ->
                WebService.searchBook.search(bookname, item)
                    .toObservable()
                    .subscribeOn(Schedulers.from(IO_EXECUTOR))
                    .onErrorReturn{ SearchBookResp("", "", "", "") }
            }

    fun getCatalogs(item: SearchBookResp) =
        if (app.cacheDir.list().contains(getFileName(item))) {
            getCatalogFromCache(item)
        } else {
            getCatalogFromNet(item)
        }

    /**
     * 保存到数据库
     */
    fun setCatalog(bookname: String, chapters: List<ChapterInfoResp>) {
        chapters.map {
            ChapterInfoEntity(
                null, it.id, bookname, it.chapterName,
                it.chapterUrl, ReaderDatabase.NOT_DOWN
            )
        }.also {
            db.runInTransaction {
                db.chapterInfoDao().deleteBook(bookname)
                db.chapterInfoDao().insert(*it.toTypedArray())
            }
        }
    }

    fun downloadChapter(bookname: String, info: ChapterInfoResp) =
        Single.zip(
            getChapter(info).retry(1).onErrorReturn { "" },
            db.chapterInfoDao().getChapterInfo(bookname, info.chapterName),
            BiFunction<String, ChapterInfoEntity,
                    Triple<String, ChapterInfoEntity, ChapterInfoResp>> { t1, t2 ->
                Triple(t1, t2, info)
            }
        ).subscribeOn(Schedulers.from(IO_EXECUTOR))

    fun updateChapter(chapterInfoEntitys: List<ChapterInfoEntity>) {
        db.runInTransaction {
            chapterInfoEntitys.forEach {
                db.chapterInfoDao().update(it)
            }
        }
    }

    //下载书籍图片(搜索时顺便获取)
    fun downloadImage(imageUrl: String): Single<Response<ResponseBody>> =
        WebService.readerAPI
            .request(imageUrl)
            .subscribeOn(Schedulers.from(IO_EXECUTOR))

    fun getCatalogFromNet(item: SearchBookResp) =
        getCatalog(item)
            .subscribeOn(Schedulers.from(IO_EXECUTOR))
            .flatMap { chapters ->
                SingleSource<Pair<SearchBookResp, List<ChapterInfoResp>>> {
                    item.latestChapter = chapters.lastOrNull()?.chapterName
                            ?: app.getString(R.string.latest_chapter_get_failed)
                    try {
                        //url的md5值作为文件名， 写入磁盘缓存
                        ObjectOutputStream(
                            File(app.cacheDir, getFileName(item)).outputStream()
                        ).use { stream -> stream.writeObject(chapters) }
                    } catch (e: IOException) {
                        LoggerFactory.getLogger(this.javaClass).warn("getCatalogFromNet$e")
                    }
                    it.onSuccess(Pair(item, chapters))
                }
            }

    @Suppress("UNCHECKED_CAST")
    fun getCatalogFromCache(item: SearchBookResp) =
        Single.create<Pair<SearchBookResp, List<ChapterInfoResp>>> { emitter ->
            var result: List<ChapterInfoResp>? = null
            ObjectInputStream(File(app.cacheDir, getFileName(item)).inputStream())
                .use { result = it.readObject() as List<ChapterInfoResp> }
            result?.last()?.chapterName?.let { item.latestChapter = it }
            emitter.onSuccess(Pair(item, result.orEmpty()))
        }

    //url的md5值作为文件名
    private fun getFileName(item: SearchBookResp) = item.url.toMD5()

    fun addBookToShelf(book: BookInfoEntity) = db.bookInfoDao().insert(book)

    fun getBookInShelf(bookname: String) = db.bookInfoDao().getBookInfo(bookname)

    fun isBookDownloaded(bookname: String) = db.bookInfoDao().getBookInfo(bookname)
}