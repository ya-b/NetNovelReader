package com.netnovelreader.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.databinding.ObservableArrayList
import com.netnovelreader.ReaderApplication
import com.netnovelreader.ReaderApplication.Companion.threadPool
import com.netnovelreader.bean.BookBean
import com.netnovelreader.bean.NovelCatalog
import com.netnovelreader.common.ReaderLiveData
import com.netnovelreader.common.enqueueCall
import com.netnovelreader.common.replace
import com.netnovelreader.data.db.ReaderDbManager
import com.netnovelreader.data.db.ShelfBean
import com.netnovelreader.data.network.ApiManager
import com.netnovelreader.data.network.DownloadCatalog
import kotlinx.coroutines.experimental.launch
import java.io.File
import java.io.IOException

/**
 * Created by yangbo on 2018/1/12.
 */
class ShelfViewModel(context: Application) : AndroidViewModel(context) {

    val bookList = ObservableArrayList<BookBean>()             //书架fragment小说列表
    var resultList = ObservableArrayList<NovelCatalog.Bean>()  //分类fragment列表
    val readBookCommand = ReaderLiveData<String>()             //阅读小说，打开readerActivity
    val showDialogCommand = ReaderLiveData<String>()           //长按删除，询问对话框
    val notRefershCommand = ReaderLiveData<Void>()             //下拉刷新后，取消刷新进度条
    val openCatalogDetailCommand = ReaderLiveData<String>()    //点击分类item
    val translateCommand = ReaderLiveData<Int>()               //移动（显示||隐藏）tablayout
    val paddingCommand = ReaderLiveData<Int>()                 //tablayout的高度
    var dbBookList: List<ShelfBean>? = null
    @Volatile
    var timeTemp = 0L
    @Volatile
    var refreshType = 0
    var toolbarOffset = 0

    //隐藏显示tab
    fun onShelfScroll(dy: Int) {
        toolbarOffset =
                if (toolbarOffset > paddingCommand.value ?: 10000) {
                    paddingCommand.value!!
                } else if (toolbarOffset < 0) {
                    0
                } else {
                    toolbarOffset
                }
        translateCommand.value = toolbarOffset
        if ((toolbarOffset < paddingCommand.value!! && dy > 0) || (toolbarOffset > 0 && dy < 0)) {
            toolbarOffset += dy
        }
    }

    //打开阅读页面
    fun readBookTask(bookname: String) {
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
    fun askDeleteTask(bookname: String): Boolean {
        showDialogCommand.value = bookname
        return true
    }

    //检查书籍是否有更新
    @Synchronized
    fun updateBooks(isFromNet: Boolean) = launch {
        notRefershCommand.call()
        System.currentTimeMillis().takeIf { it - timeTemp > 2000 }?.also { timeTemp = it }
                ?: return@launch
        bookList.forEach {
            if (isFromNet) {
                updateCatalog(it)
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
            bookList.filter { it.bookname.get() == bookname }
                    .let { bookList.removeAll(it) }
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
        ApiManager.zhuiShuShenQi.getNovelCatalogData()
                .enqueueCall { it?.male?.let { resultList.addAll(it) } }
    }

    //更新小说目录
    private fun updateCatalog(bookBean: BookBean) {
        try {
            DownloadCatalog(bookBean.bookname.get()!!, bookBean.downloadURL.get() ?: "").download()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}