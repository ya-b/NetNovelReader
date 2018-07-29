package com.netnovelreader.vm

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.databinding.ObservableArrayList
import android.databinding.ObservableBoolean
import android.text.TextUtils
import com.netnovelreader.R
import com.netnovelreader.repo.SearchRepo
import com.netnovelreader.repo.db.BookInfoEntity
import com.netnovelreader.repo.http.resp.SearchBookResp
import com.netnovelreader.utils.COVER_NAME
import com.netnovelreader.utils.IO_EXECUTOR
import com.netnovelreader.utils.bookDir
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import java.io.File

class SearchViewModel(var repo: SearchRepo, app: Application) : AndroidViewModel(app) {
    val isLoading = ObservableBoolean(false)
    val searchResultList = ObservableArrayList<SearchBookResp>()
    val exitCommand = MutableLiveData<Void>()
    val downloadCommand = MutableLiveData<SearchBookResp>()
    val toaskCommand = MutableLiveData<String>()
    val confirmCommand = MutableLiveData<SearchBookResp>()
    private var searchObserver: DisposableObserver<SearchBookResp>? = null

    @Synchronized
    fun searchBook(bookname: String) {
        if (bookname.isEmpty()) return
        searchResultList.clear()
        searchObserver?.dispose()
        if (searchObserver == null) {
            searchObserver = object : DisposableObserver<SearchBookResp>() {
                override fun onNext(t: SearchBookResp) {
                    isLoading.set(true)
                    if (!TextUtils.isEmpty(t.bookname) && !TextUtils.isEmpty(t.url)) {
                        searchResultList.add(t)
                    }
                    //下载封面图片
                    if (!File(bookDir(bookname), COVER_NAME).exists()) {
                        repo.downloadImage(bookname, t.imageUrl)
                    }
                }

                override fun onComplete() {
                    isLoading.set(false)
                    //搜索完成后，再获取最新章节
                    getLatestChapter(searchResultList)
                }

                override fun onError(e: Throwable) {
                    isLoading.set(false)
                    getLatestChapter(searchResultList)
                }
            }
        }
        repo.search(bookname).subscribeOn(Schedulers.from(IO_EXECUTOR)).subscribe(searchObserver!!)
    }

    fun changeSourceSearch(bookname: String, chapterName: String) {
        if (bookname.isEmpty()) return
        if (chapterName.isEmpty()) return
        repo.search(bookname)
            .subscribeOn(Schedulers.from(IO_EXECUTOR))
            .subscribe(
                {
                    isLoading.set(true)
                    repo.getBookInShelf(bookname).subscribe (
                        { entity ->
                            if(it.url == entity.downloadUrl) return@subscribe
                            repo.getCatalogFromNet(it) { key, list ->
                                list?.firstOrNull { it.chapterName == chapterName }?.let {
                                    searchResultList.add(key)
                                }
                            }
                        },
                        {
                            LoggerFactory.getLogger(this.javaClass).warn("error on changeSourceSearch")
                        })
                },
                { isLoading.set(false) },
                { isLoading.set(false) }
            )
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
                {
                    if (!isOnlyAdd) {
                        downloadCommand.postValue(book)
                    }
                    repo.addBookToShelf(
                        BookInfoEntity(
                            null, book.bookname, book.url, "1#1",
                            true, book.latestChapter, 0, book.imageUrl
                        )
                    )
                    repo.getCatalog(book) { _, chapters ->
                        repo.setCatalog(book.bookname, chapters ?: emptyList())
                    }
                })
    }

    fun changeSourceDownload(book: SearchBookResp, chapterName: String) {
        repo.getCatalog(book) { _, chapters ->
            chapters ?: return@getCatalog
            repo.getBookInShelf(book.bookname).subscribeOn(Schedulers.from(IO_EXECUTOR)).subscribe(
                {
                    it.readRecord = "${chapters.first { it.chapterName.equals(chapterName) }.id}#1"
                    it.latestChapter = chapters.last().chapterName
                    it.downloadUrl = book.url
                    repo.addBookToShelf(it)
                    repo.setCatalog(book.bookname, chapters)
                    bookDir(book.bookname).listFiles { _, name -> !name.equals(COVER_NAME) }
                        .forEach { it.delete() }
                    exit()
                }, {
                    LoggerFactory.getLogger(this.javaClass).warn("error on changeSourceDownload")
                })
        }
    }

    fun exit() {
        exitCommand.postValue(null)
    }

    //todo 上一个搜索没完成，又开始一个搜索，这里估计会出问题
    private fun getLatestChapter(respList: ObservableArrayList<SearchBookResp>) {
        for (i in 0 until respList.size) {
            repo.getCatalog(respList[i]) { resp, _ ->
                resp?.let { respList.set(i, resp) }
            }
        }
    }
}