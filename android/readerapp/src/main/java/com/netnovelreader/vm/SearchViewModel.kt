package com.netnovelreader.vm

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.databinding.ObservableArrayList
import android.text.TextUtils
import com.netnovelreader.R
import com.netnovelreader.repo.SearchRepo
import com.netnovelreader.repo.db.BookInfoEntity
import com.netnovelreader.repo.http.resp.ChapterInfoResp
import com.netnovelreader.repo.http.resp.SearchBookResp
import com.netnovelreader.utils.COVER_NAME
import com.netnovelreader.utils.IO_EXECUTOR
import com.netnovelreader.utils.bookDir
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream

class SearchViewModel(var repo: SearchRepo, app: Application) : AndroidViewModel(app) {
    val isLoading = MutableLiveData<Boolean>()
    val searchResultList = ObservableArrayList<SearchBookResp>()
    val exitCommand = MutableLiveData<Void>()
    val downloadCommand = MutableLiveData<SearchBookResp>()
    val toaskCommand = MutableLiveData<String>()
    val confirmCommand = MutableLiveData<SearchBookResp>()
    var compositeDisposable: CompositeDisposable = CompositeDisposable()
    private var searchObserver: DisposableObserver<SearchBookResp>? = null
    private var latestChapterObserver
            : DisposableObserver<Pair<SearchBookResp, List<ChapterInfoResp>>>? = null

    fun destroy() {
        compositeDisposable.clear()
    }

    @Synchronized
    fun searchBook(bookname: String) {
        if (bookname.isEmpty()) return
        isLoading.postValue(true)
        searchResultList.clear()
        searchObserver?.dispose()
        searchObserver = object : DisposableObserver<SearchBookResp>() {
            override fun onNext(t: SearchBookResp) {
                if (!TextUtils.isEmpty(t.bookname) && !TextUtils.isEmpty(t.url)) {
                    searchResultList.add(t)
                }
                //下载封面图片
                if (!File(bookDir(bookname), COVER_NAME).exists()) {
                    downloadImg(bookname, t.imageUrl)
                }
            }

            override fun onComplete() {
                isLoading.postValue(false)
                //搜索完成后，再获取最新章节  p2jPxLYERl8Z 1993
                getLatestChapter(searchResultList)
            }

            override fun onError(e: Throwable) {
                isLoading.postValue(false)
                getLatestChapter(searchResultList)
            }
        }
        repo.search(bookname).subscribeOn(Schedulers.from(IO_EXECUTOR)).subscribe(searchObserver!!)
    }

    fun changeSourceSearch(bookname: String, chapterName: String) {
        if (bookname.isEmpty()) return
        if (chapterName.isEmpty()) return
        isLoading.postValue(true)
        repo.search(bookname)
            .flatMap { result ->
                repo.getCatalogFromNet(result).toObservable()
                    .onErrorReturn { Pair(result, emptyList()) }
            }
            .subscribe(
                { pair ->
                    pair.second.firstOrNull { it.chapterName == chapterName }?.let {
                        searchResultList.add(pair.first)
                    }
                },
                { isLoading.postValue(false) },
                { isLoading.postValue(false) }
            ).let { compositeDisposable.add(it) }
    }

    fun confirmDownload(book: SearchBookResp) {
        confirmCommand.value = book
    }

    fun download(book: SearchBookResp, isOnlyAdd: Boolean) {
        repo.isBookDownloaded(book.bookname)
            .subscribeOn(Schedulers.from(IO_EXECUTOR))
            .subscribe(
                {
                    toaskCommand.postValue(getApplication<Application>().getString(R.string.already_in_shelf))
                },
                { _ ->
                    if (!isOnlyAdd) {
                        downloadCommand.postValue(book)
                    }
                    repo.addBookToShelf(
                        BookInfoEntity(
                            null, book.bookname, book.url, "1#1",
                            true, book.latestChapter, 0, book.imageUrl
                        )
                    )
                    toaskCommand.postValue(getApplication<Application>().getString(R.string.add_to_shelf))
                    repo.getCatalogs(book)
                        .subscribe(
                            {
                                repo.setCatalog(book.bookname, it.second)
                            },
                            {
                                LoggerFactory.getLogger(SearchViewModel::class.java)
                                    .debug(it.toString())
                            })
                }).let { compositeDisposable.add(it) }
    }

    fun changeSourceDownload(book: SearchBookResp, chapterName: String) {
        Single.zip(repo.getCatalogs(book),
            repo.getBookInShelf(book.bookname),
            BiFunction<Pair<SearchBookResp, List<ChapterInfoResp>>, BookInfoEntity,
                    Triple<SearchBookResp, List<ChapterInfoResp>, BookInfoEntity>> { t1, t2 ->
                Triple(t1.first, t1.second, t2)
            }
        ).subscribeOn(Schedulers.from(IO_EXECUTOR))
            .subscribe(
                { pair ->
                    pair.third.readRecord =
                            "${pair.second.first { it.chapterName.equals(chapterName) }.id}#1"
                    pair.third.latestChapter = pair.second.last().chapterName
                    pair.third.downloadUrl = book.url
                    repo.addBookToShelf(pair.third)
                    repo.setCatalog(book.bookname, pair.second)
                    bookDir(book.bookname).listFiles { _, name -> !name.equals(COVER_NAME) }
                        .forEach { it.delete() }
                    exit()
                },
                {
                    toaskCommand.postValue(getApplication<Application>().getString(R.string.download_failed))
                    LoggerFactory.getLogger(SearchViewModel::class.java).debug(it.toString())
                }).let { compositeDisposable.add(it) }
    }

    fun exit() {
        exitCommand.postValue(null)
    }

    private fun getLatestChapter(respList: ObservableArrayList<SearchBookResp>) {
        latestChapterObserver?.dispose()
        latestChapterObserver =
                object : DisposableObserver<Pair<SearchBookResp, List<ChapterInfoResp>>>() {
                    override fun onComplete() {

                    }

                    override fun onNext(t: Pair<SearchBookResp, List<ChapterInfoResp>>) {
                        respList.set(respList.indexOf(t.first), t.first)
                    }

                    override fun onError(e: Throwable) {

                    }
                }
        Observable.fromIterable(respList)
            .flatMap {
                repo.getCatalogs(it).toObservable().subscribeOn(Schedulers.from(IO_EXECUTOR))
            }
            .subscribe(latestChapterObserver!!)
    }

    private fun downloadImg(bookname: String, imageUrl: String) {
        val path = "${bookDir(bookname)}".let { "$it${File.separator}$COVER_NAME" }
        if (imageUrl.isEmpty() || File(path).exists()) return
        repo.downloadImage(imageUrl)
            .subscribeOn(Schedulers.from(IO_EXECUTOR))
            .subscribe(
                {
                    it?.body()?.byteStream()?.use { ins ->
                        FileOutputStream(path).use { os -> ins.copyTo(os) }
                    }
                },
                {
                    LoggerFactory.getLogger(SearchRepo::class.java).warn("downloadImage:$it")
                }).let { compositeDisposable.add(it) }
    }
}