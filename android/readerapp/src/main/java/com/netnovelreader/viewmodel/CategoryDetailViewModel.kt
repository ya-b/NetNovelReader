package com.netnovelreader.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.databinding.ObservableArrayList
import android.databinding.ObservableBoolean
import android.util.LruCache
import com.netnovelreader.bean.NovelIntroduce
import com.netnovelreader.bean.NovelList
import com.netnovelreader.common.enqueueCall
import com.netnovelreader.common.tryIgnoreCatch
import com.netnovelreader.data.network.WebService
import kotlinx.coroutines.experimental.launch

class CategoryDetailViewModel(context: Application) : AndroidViewModel(context) {
    var isLoading = ObservableBoolean(true)
    val bookMap = HashMap<String, LruCache<String, ObservableArrayList<NovelList.BooksBean>>>()
    val toastMessage by lazy { MutableLiveData<String>() }
    val showBookDetailCommand by lazy { MutableLiveData<NovelIntroduce>() }
    val exitCommand = MutableLiveData<Void>()      //点击返回图标

    @Synchronized
    fun getNovelIntroduce(id: String) = launch {
        val novelIntroduce = tryIgnoreCatch {
            WebService.zhuiShuShenQi.getNovelIntroduce(id).execute().body()
        }
        if (novelIntroduce == null) {
            toastMessage.postValue("没有搜索到相关小说的介绍")
        } else {
            showBookDetailCommand.postValue(novelIntroduce)
        }
    }

    fun initBooklist(type: String, major: String) {
        val booklist = getBookList(type, major)
        if (booklist.isNotEmpty()) return
        isLoading.set(true)
        WebService.zhuiShuShenQi.seachBookListByTypeAndMajor(type = type, major = major)
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
        } else if (bookMap[type]!![major] == null) {
            bookMap[type]!!.put(major, ObservableArrayList())
        }
        return bookMap[type]!![major]
    }

    fun exit() {
        exitCommand.value = null
    }
}