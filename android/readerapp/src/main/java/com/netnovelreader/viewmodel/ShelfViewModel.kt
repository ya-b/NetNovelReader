package com.netnovelreader.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import android.databinding.ObservableArrayList
import android.databinding.ObservableBoolean
import android.support.v7.widget.RecyclerView
import com.netnovelreader.R
import com.netnovelreader.ReaderApplication
import com.netnovelreader.ReaderApplication.Companion.threadPool
import com.netnovelreader.bean.NovelCatalog
import com.netnovelreader.common.*
import com.netnovelreader.data.CatalogManager
import com.netnovelreader.data.local.ReaderDbManager
import com.netnovelreader.data.local.db.ShelfBean
import com.netnovelreader.data.network.WebService
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import java.io.File

class ShelfViewModel(val context: Application) : AndroidViewModel(context) {
    val allBookList = LivePagedListBuilder(
        ReaderDbManager.shelfDao().allBooks(),
        PagedList.Config.Builder()
            .setPageSize(10)
            .setEnablePlaceholders(false)
            .build()
    ).build()                                                  //书架fragment小说列表
    var resultList = ObservableArrayList<NovelCatalog.Bean>()  //分类fragment列表
    var isLoading = ObservableBoolean(true)
    val readBookCommand = MutableLiveData<String>()             //阅读小说，打开readerActivity
    val showDialogCommand = MutableLiveData<String>()           //长按删除，询问对话框
    val stopRefershCommand = MutableLiveData<Void>()            //下拉刷新后，取消刷新进度条
    val openCatalogDetailCommand = MutableLiveData<String>()    //点击分类item
    val translateCommand = MutableLiveData<Array<Int>>()        //移动（显示||隐藏）tablayout
    var tabHeight = 0         //activity ,tab的高度
    var job: Job? = null
    @Volatile
    var updateTime = 0L
    @Volatile
    var refreshType = 0
    var toolbarOffset = 0

    //隐藏显示tab
    fun onShelfScroll(dy: Int, state: Int, isFirstVisible: Boolean) {
        if (tabHeight == 0) return
        if (state == RecyclerView.SCROLL_STATE_IDLE && !isFirstVisible) {
            toolbarOffset = when {
                Math.abs(toolbarOffset) < tabHeight / 2 -> 0
                else -> tabHeight
            }
            translateCommand.value = arrayOf(toolbarOffset, RecyclerView.SCROLL_STATE_IDLE)
        } else {
            toolbarOffset = when {
                toolbarOffset > tabHeight -> tabHeight
                toolbarOffset < 0 -> 0
                else -> toolbarOffset
            }
            translateCommand.value = arrayOf(toolbarOffset, RecyclerView.SCROLL_STATE_DRAGGING)
            if ((toolbarOffset < tabHeight && dy > 0) || (toolbarOffset > 0 && dy < 0)) {
                toolbarOffset += dy
            }
        }
    }

    fun tabChanged(text: String) {
        if (text == context.getString(R.string.shelf)) {
            translateCommand.value = arrayOf(0, RecyclerView.SCROLL_STATE_DRAGGING)
        } else {
            translateCommand.value = arrayOf(toolbarOffset, RecyclerView.SCROLL_STATE_DRAGGING)
        }
    }

    //打开阅读页面
    fun readBook(bookname: String) {
        if (bookname.isEmpty()) return
        readBookCommand.value = bookname
        launch(threadPool) {
            //取消书籍更新标志,设为最近阅读
            val latestRead = ReaderDbManager.shelfDao().getLatestReaded() ?: 0
            ReaderDbManager.shelfDao().replace(
                bookName = bookname, isUpdate = "", latestRead = latestRead + 1
            )
        }
    }

    //询问是否删除小说，[onLongClick]调用
    fun deleteDialog(bookname: String): Boolean {
        showDialogCommand.value = bookname
        return true
    }

    //检查书籍是否有更新
    fun updateBooks(isFromNet: Boolean) {
        stopRefershCommand.value = null
        System.currentTimeMillis().takeIf { it - updateTime > 2000 }?.also { updateTime = it } ?: return
        job?.cancel()
        job = launch {
            ReaderDbManager.shelfDao().getAll()?.forEach {
                launch(threadPool) { updateItem(it, isFromNet) }
            }
        }
    }

    //删除书籍
    fun deleteBook(bookname: String) {
        ReaderDbManager.apply {
            val deleteBean = shelfDao().getBookInfo(bookname) ?: return
            shelfDao().delete(deleteBean)
            dropTable(deleteBean.bookName)
            File(ReaderApplication.dirPath, deleteBean.bookName)
                .deleteRecursively()
        }
    }

    //打开分类item
    fun openCatalogDetail(name: String?) {
        name ?: return
        openCatalogDetailCommand.value = name
        refreshType = 2
    }

    //分类数据加载
    fun getNovelCatalogData() {
        isLoading.set(true)
        WebService.zhuiShuShenQi.getNovelCatalogData().enqueueCall {
            it?.male?.let { resultList.addAll(it) }
            if (resultList.isNotEmpty()) isLoading.set(false)
        }
    }

    fun isLoginItemShow(): Boolean = context.sharedPreferences().get(context.getString(R.string.tokenKey), "").isEmpty()

    //更新小说目录
    private fun updateItem(bookInfo: ShelfBean, isFromNet: Boolean) {
        if (isFromNet) {
            tryIgnoreCatch {
                CatalogManager.download(bookInfo.bookName!!, bookInfo.downloadUrl ?: "")
            }
        }
    }
}