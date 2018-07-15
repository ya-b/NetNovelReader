package com.netnovelreader.repo

import android.app.Application
import android.util.Log
import com.netnovelreader.R
import com.netnovelreader.repo.db.BookInfoEntity
import com.netnovelreader.repo.db.ChapterInfoEntity
import com.netnovelreader.repo.db.ReaderDatabase
import com.netnovelreader.repo.db.SiteSelectorEntity
import com.netnovelreader.repo.http.WebService
import com.netnovelreader.repo.http.resp.ChapterInfoResp
import com.netnovelreader.repo.http.resp.SearchBookResp
import com.netnovelreader.utils.*
import io.reactivex.Observable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import java.io.*

class SearchRepo(app: Application) : Repo(app) {
    private var searchWorker: SearchRespObserver? = null

    /**
     * 搜索书，如果已在搜索，则取消当前搜索，重新搜
     */
    @Throws(IOException::class)
    fun search(bookname: String, call: (Boolean, SearchBookResp?) -> Unit) {
        searchWorker?.dispose()
        searchWorker = SearchRespObserver(call)
        Observable.create<List<SiteSelectorEntity>> {
            it.onNext(db.siteSelectorDao().getAll())
            it.onComplete()
        }.subscribeOn(Schedulers.from(IO_EXECUTOR))
            .flatMap { Observable.fromIterable(it) }
            .observeOn(Schedulers.from(IO_EXECUTOR))
            .flatMap { item ->
                Observable.create<SearchBookResp> {
                    it.onNext(WebService.searchBook.search(bookname, item))
                    it.onComplete()
                }.subscribeOn(Schedulers.from(IO_EXECUTOR))
            }.subscribe(searchWorker!!)
    }

    /**
     *
     * @call :    给@SearchBookResp设置最新章节，并回调
     */
    @Throws(IOException::class)
    fun getCatalog(
        item: SearchBookResp,
        call: ((SearchBookResp?, List<ChapterInfoResp>?) -> Unit)? = null
    ) {
        if (app.cacheDir.list().contains(getFileName(item))) {
            getCatalogFromCache(item, call)
        } else {
            getCatalogFromNet(item, call)
        }
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
        }.toTypedArray()
            .let { db.chapterInfoDao().insert(*it) }
    }

    @Throws(IOException::class)
    fun downloadChapter(bookname: String, info: ChapterInfoResp) {
        val str = getChapter(info)
        File(bookDir(bookname), info.id.toString()).writeText(str)
        db.chapterInfoDao()
            .getChapterInfo(bookname, info.chapterName)
            ?.also { it.isDownloaded = ReaderDatabase.ALLREADY_DOWN }
            ?.also { db.chapterInfoDao().update(it) }
    }

    //下载书籍图片(搜索时顺便获取)
    fun downloadImage(bookname: String, imageUrl: String) {
        val path = "${bookDir(bookname)}".let { "$it${File.separator}$COVER_NAME" }
        if(imageUrl.isEmpty() || File(path).exists()) return
        ioThread {
            try {
                WebService.readerAPI.request(imageUrl).execute().body()?.byteStream()?.use { ins ->
                    FileOutputStream(path).use { os -> ins.copyTo(os) }
                }
            }catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    @Throws(IOException::class)
    private fun getCatalogFromNet(
        item: SearchBookResp,
        call: ((SearchBookResp?, List<ChapterInfoResp>?) -> Unit)? = null
    ) {
        Observable
            .create<List<ChapterInfoResp>> {
                val chapters = getCatalog(item)
                item.latestChapter = chapters.last().chapterName
                it.onNext(chapters)
                it.onComplete()
            }.subscribeOn(Schedulers.from(IO_EXECUTOR))
            .subscribe(
                { list ->
                    call?.invoke(item, list)
                    try {
                        //url的md5值作为文件名， 写入磁盘缓存
                        ObjectOutputStream(
                            File(app.cacheDir, getFileName(item)).outputStream()
                        ).use { it.writeObject(list) }
                    } catch (e: IOException) {
                        Log.w("${this.javaClass.name}: write object error", e)
                    }
                },
                {
                    //下载目录失败
                    call?.invoke(
                        item.apply {
                            latestChapter = app.getString(R.string.latest_chapter_get_failed)
                        },
                        null
                    )
                    Log.w("${this.javaClass.name}: down catalog error", it)
                }
            )
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(IOException::class)
    private fun getCatalogFromCache(
        item: SearchBookResp,
        call: ((SearchBookResp?, List<ChapterInfoResp>?) -> Unit)? = null
    ) {
        var result: List<ChapterInfoResp>? = null
        ObjectInputStream(File(app.cacheDir, getFileName(item)).inputStream())
            .use { result = it.readObject() as List<ChapterInfoResp> }
        result?.last()?.chapterName?.let { item.latestChapter = it }
        call?.invoke(item, result)
    }

    //url的md5值作为文件名
    private fun getFileName(item: SearchBookResp) = item.url.toMD5()

    fun addBook(book: BookInfoEntity) = db.bookInfoDao().insert(book)

    fun isBookDownloaded(bookname: String) = db.bookInfoDao().getBookInfo(bookname) != null

    class SearchRespObserver(private val call: (Boolean, SearchBookResp?) -> Unit) :
        DisposableObserver<SearchBookResp>() {
        override fun onNext(t: SearchBookResp) {
            call.invoke(true, t)
        }

        override fun onComplete() {
            call.invoke(false, null)
        }

        override fun onError(e: Throwable) {
            call.invoke(false, null)
            Log.w(this.javaClass.name, e)
        }
    }
}