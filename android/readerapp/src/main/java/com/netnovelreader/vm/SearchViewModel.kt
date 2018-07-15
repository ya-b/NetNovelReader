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
import com.netnovelreader.utils.bookDir
import com.netnovelreader.utils.ioThread
import java.io.File

class SearchViewModel(var repo: SearchRepo, app: Application) : AndroidViewModel(app) {
    val isLoading = ObservableBoolean(false)
    val isChangeSource = ObservableBoolean(false)
    val searchResultList = ObservableArrayList<SearchBookResp>()
    val exitCommand = MutableLiveData<Void>()
    val downloadCommand = MutableLiveData<SearchBookResp>()
    val toaskCommand = MutableLiveData<String>()
    val confirmCommand = MutableLiveData<SearchBookResp>()

    fun searchBook(bookname: String) {
        if (bookname.isEmpty()) return
        searchResultList.clear()
        repo.search("极道天魔") { isSearching, resp ->
            isLoading.set(isSearching)
            if (!TextUtils.isEmpty(resp?.bookname) && !TextUtils.isEmpty(resp?.url)) {
                searchResultList.add(resp)
            }
            //下载封面图片
            if(resp?.imageUrl != null && !File(bookDir(bookname), COVER_NAME).exists()) {
                repo.downloadImage(bookname, resp.imageUrl)
            }
            //搜索完成后，再获取最新章节
            if (!isSearching) {
                getLatestChapter(searchResultList)
            }
        }
    }

    fun confirmDownload(book: SearchBookResp) {
        confirmCommand.value = book
    }

    fun download(book: SearchBookResp, isOnlyAdd: Boolean) {
        ioThread {
            if (repo.isBookDownloaded(book.bookname)) {
                toaskCommand.postValue(getApplication<Application>().getString(R.string.already_in_shelf))
            } else {
                if (!isOnlyAdd) {
                    downloadCommand.postValue(book)
                }
                repo.addBook(
                    BookInfoEntity(
                        null, book.bookname, book.url, "1#1",
                        true, book.latestChapter, 0, book.imageUrl
                    )
                )
                repo.getCatalog(book) { _, chapters ->
                    repo.setCatalog(book.bookname, chapters ?: emptyList())
                }
            }
        }
    }

    fun exit() {
        exitCommand.value = null
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