package com.netnovelreader.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.databinding.ObservableArrayList
import android.databinding.ObservableBoolean
import android.support.v7.widget.RecyclerView
import com.netnovelreader.R
import com.netnovelreader.ReaderApplication
import com.netnovelreader.ReaderApplication.Companion.threadPool
import com.netnovelreader.bean.BookBean
import com.netnovelreader.bean.NovelCatalog
import com.netnovelreader.common.ReaderLiveData
import com.netnovelreader.common.enqueueCall
import com.netnovelreader.common.replace
import com.netnovelreader.data.CatalogManager
import com.netnovelreader.data.local.ReaderDbManager
import com.netnovelreader.data.local.db.ShelfBean
import com.netnovelreader.data.network.ApiManager
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import java.io.File
import java.io.IOException

/**
 * Created by yangbo on 2018/1/12.
 */
class ShelfViewModel(val context: Application) : AndroidViewModel(context) {

    val bookList = ObservableArrayList<BookBean>()             //书架fragment小说列表
    var resultList = ObservableArrayList<NovelCatalog.Bean>()  //分类fragment列表
    var isLoading = ObservableBoolean(true)
    val readBookCommand = ReaderLiveData<String>()             //阅读小说，打开readerActivity
    val showDialogCommand = ReaderLiveData<String>()           //长按删除，询问对话框
    val stopRefershCommand = ReaderLiveData<Void>()            //下拉刷新后，取消刷新进度条
    val openCatalogDetailCommand = ReaderLiveData<String>()    //点击分类item
    val translateCommand = ReaderLiveData<Array<Int>>()        //移动（显示||隐藏）tablayout
    var tabHeight = 0         //activity ,tab的高度
    var job: Job? = null
    var dbBookList: List<ShelfBean>? = null
    @Volatile
    var updateTime = 0L
    @Volatile
    var refreshType = 0
    var toolbarOffset = 0

    //隐藏显示tab
    fun onShelfScroll(dy: Int, state: Int, isFirstVisible: Boolean) {
        if (tabHeight == 0) return
        if (state == RecyclerView.SCROLL_STATE_IDLE && !isFirstVisible) {
            toolbarOffset = if (Math.abs(toolbarOffset) < tabHeight / 2) {
                0
            } else {
                tabHeight
            }
            translateCommand.value = arrayOf(toolbarOffset, RecyclerView.SCROLL_STATE_IDLE)
        } else {
            toolbarOffset = if (toolbarOffset > tabHeight) {
                tabHeight
            } else if (toolbarOffset < 0) {
                0
            } else {
                toolbarOffset
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
        launch(threadPool) {
            //取消书籍更新标志,设为最近阅读
            val latestRead = ReaderDbManager.shelfDao().getLatestReaded() ?: 0
            ReaderDbManager.shelfDao().replace(
                    bookName = bookname, isUpdate = "", latestRead = latestRead + 1
            )
        }
        bookList.firstOrNull { it.bookname.get() == bookname }?.isUpdate?.set("")
        readBookCommand.value = bookname
    }

    //询问是否删除小说，[onLongClick]调用
    fun deleteDialog(bookname: String): Boolean {
        showDialogCommand.value = bookname
        return true
    }

    //检查书籍是否有更新
    fun updateBooks(isFromNet: Boolean) {
        stopRefershCommand.call()
        System.currentTimeMillis().takeIf { it - updateTime > 2000 }?.also { updateTime = it }
                ?: return
        job?.cancel()
        job = launch {
            bookList.forEach { launch(threadPool) { updateItem(it, isFromNet) } }
        }
    }

    /**
     * 刷新书架，重新读数据库（数据库有没有更新）
     */
    @Synchronized
    fun refreshBookList() {
        dbBookList = ReaderDbManager.shelfDao().getAll() ?: return
        when (refreshType) {
            0 -> {
                bookList.clear()
                dbBookList!!.map { BookBean.fromShelfBean(it) }
                        .let { bookList.addAll(it) }
            }
            1 -> {
                if (dbBookList!![0].bookName == bookList[0].bookname.get()) return
                bookList.firstOrNull { it.bookname.get() == dbBookList!![0].bookName }
                        ?.let { bookList.remove(it) }
                bookList.add(0, BookBean.fromShelfBean(dbBookList!![0]))
            }
            2 -> if (dbBookList!!.size > bookList.size) {
                BookBean.fromShelfBean(dbBookList!!.last())
                        .let { bookList.add(it) }
            }
        }
        refreshType = 0
    }

    //删除书籍
    fun deleteBook(bookname: String) {
        ReaderDbManager.apply {
            val deleteBean = shelfDao().getBookInfo(bookname) ?: return
            shelfDao().delete(deleteBean)
            dropTable(deleteBean.bookName)
            bookList.firstOrNull { it.bookname.get() == bookname }
                    ?.let { bookList.remove(it) }
            File(ReaderApplication.dirPath, deleteBean.bookName)
                    .deleteRecursively()
            dbBookList = dbBookList?.filter { it.bookName != bookname }
        }
    }

    //打开分类item
    fun openCatalogDetail(name: String?) {
        name ?: return
        openCatalogDetailCommand.value = name
    }

    //分类数据加载
    fun getNovelCatalogData() {
        isLoading.set(true)
        ApiManager.zhuiShuShenQi.getNovelCatalogData().enqueueCall {
            it?.male?.let { resultList.addAll(it) }
            if (resultList.isNotEmpty()) isLoading.set(false)
        }
    }

    //更新小说目录
    private fun updateItem(bookBean: BookBean, isFromNet: Boolean) {
        if (isFromNet) {
            try {
                CatalogManager.download(
                        bookBean.bookname.get()!!,
                        bookBean.downloadURL.get() ?: ""
                )
            } catch (e: IOException) {
                e.printStackTrace()
            }
            dbBookList = ReaderDbManager.shelfDao().getAll()
        }
        (0 until bookList.size).forEach { i ->
            if (bookList[i].bookname.get() == dbBookList!![i].bookName) {
                if (bookList[i].isUpdate.get() != dbBookList!![i].isUpdate) {
                    bookList[i].isUpdate.set(dbBookList!![i].isUpdate)
                }
                if (bookList[i].latestChapter.get() != dbBookList!![i].latestChapter) {
                    bookList[i].latestChapter.set(dbBookList!![i].latestChapter)
                }
            }
        }
    }
}