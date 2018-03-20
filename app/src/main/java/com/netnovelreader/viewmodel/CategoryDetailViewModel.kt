package com.netnovelreader.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.databinding.ObservableArrayList
import android.databinding.ObservableBoolean
import android.util.LruCache
import com.netnovelreader.bean.NovelIntroduce
import com.netnovelreader.bean.NovelList
import com.netnovelreader.common.ReaderLiveData
import com.netnovelreader.common.enqueueCall
import com.netnovelreader.data.network.ApiManager
import kotlinx.coroutines.experimental.launch
import java.io.IOException

class CategoryDetailViewModel(context: Application) : AndroidViewModel(context) {
    var isLoading = ObservableBoolean(true)
    val bookMap = HashMap<String, LruCache<String, ObservableArrayList<NovelList.BooksBean>>>()
    val toastMessage by lazy { ReaderLiveData<String>() }
    val showBookDetailCommand by lazy { ReaderLiveData<NovelIntroduce>() }
    val exitCommand = ReaderLiveData<Void>()      //点击返回图标

    @Synchronized
    fun getNovelIntroduce(id: String) {
        launch {
            val novelIntroduce = try {
                ApiManager.zhuiShuShenQi.getNovelIntroduce(id).execute().body()
            } catch (e: IOException) {
                null
            }
            if (novelIntroduce == null) {
                toastMessage.value = "没有搜索到相关小说的介绍"
            } else {
                showBookDetailCommand.value = novelIntroduce
            }
        }
    }

    fun initBooklist(type: String, major: String) {
        val booklist = getBookList(type, major)
        if (booklist.isNotEmpty()) return
        isLoading.set(true)
        ApiManager.zhuiShuShenQi.seachBookListByTypeAndMajor(type = type, major = major)
                .enqueueCall {
                    it ?: return@enqueueCall
                    isLoading.set(false)
                    booklist.addAll(it.books!!)
                }
    }

    fun getBookList(type: String, major: String): ObservableArrayList<NovelList.BooksBean> {
        if (bookMap[type] == null) {
            bookMap[type] = LruCache<String, ObservableArrayList<NovelList.BooksBean>>(3)
                    .apply { put(major, ObservableArrayList()) }
        } else {
            if (bookMap[type]!![major] == null) {
                bookMap[type]!!.put(major, ObservableArrayList())
            }
        }
        return bookMap[type]!![major]
    }

    fun exit() {
        exitCommand.call()
    }
}