package com.netnovelreader.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.databinding.ObservableArrayList
import android.databinding.ObservableField
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.netnovelreader.ReaderApplication.Companion.threadPool
import com.netnovelreader.bean.BookBean
import com.netnovelreader.common.IMAGENAME
import com.netnovelreader.common.ReaderLiveData
import com.netnovelreader.common.getSavePath
import com.netnovelreader.common.replace
import com.netnovelreader.data.db.ReaderDbManager
import com.netnovelreader.data.network.DownloadCatalog
import kotlinx.coroutines.experimental.launch
import java.io.File
import java.io.IOException
import java.util.*

/**
 * Created by yangbo on 2018/1/12.
 */
class ShelfViewModel(val context: Application) : AndroidViewModel(context) {

    val bookList = ObservableArrayList<BookBean>()
    val readBookCommand = ReaderLiveData<String>()
    val showDialogCommand = ReaderLiveData<String>()
    val notRefershCommand = ReaderLiveData<Void>()
    @Volatile
    var timeTemp = 0L

    fun readBookTask(bookname: String) {
        launch(threadPool) {
            //取消书籍更新标志,设为最近阅读
            val latestRead = ReaderDbManager.getRoomDB().shelfDao().getLatestReaded() ?: 0
            ReaderDbManager.getRoomDB().shelfDao().replace(
                bookName = bookname, isUpdate = "", latestRead = latestRead + 1
            )

        }
        readBookCommand.value = bookname
    }

    fun askDeleteTask(bookname: String): Boolean {
        showDialogCommand.value = bookname
        return true
    }

    //检查书籍是否有更新
    fun updateBooks() = launch {
        notRefershCommand.call()
        System.currentTimeMillis().takeIf { it - timeTemp > 2000 }?.also { timeTemp = it }
                ?: return@launch
        bookList.forEach {
            updateCatalog(it)
            val list = ReaderDbManager.getRoomDB().shelfDao().getAll()
            bookList.forEach { bean ->
                list?.forEach {
                    //如果该书在数据库里面，则更新该书状态，比如最新章节的变化
                    if (it.bookName == bean.bookname.get()) {
                        bean.latestChapter.set(it.latestChapter)
                        bean.isUpdate.set(it.isUpdate)
                    }
                }
            }
        }
    }

    /**
     * 刷新书架，重新读数据库（数据库有没有更新）
     */
    fun refreshBookList() {
        val bookDirList = File(getSavePath()).takeIf { it.exists() }?.list()
        val list = ReaderDbManager.getRoomDB().shelfDao().getAll() ?: return
        val temp = ArrayList<BookBean>()
        list.forEach {
            val bookBean = BookBean(
                ObservableField(it.bookName ?: ""),
                ObservableField(it.latestChapter ?: ""),
                ObservableField(it.downloadUrl ?: ""),
                ObservableField(getBitmap(it.bookName ?: "")),
                ObservableField(it.isUpdate ?: "")
            )
            if (bookDirList?.contains(bookBean.bookname.get()) == true) {
                temp.add(bookBean)
                if (ReaderDbManager.getChapterCount(bookBean.bookname.get()!!) == 0) {
                    launch { updateCatalog(bookBean) }
                }
            } else {
                bookBean.bookname.get()?.run { deleteBook(this) }
            }
        }
        bookList.clear()
        bookList.addAll(temp)
    }

    //删除书籍
    fun deleteBook(bookname: String) {
        ReaderDbManager.getRoomDB().shelfDao().apply {
            getBookInfo(bookname)?.also {
                it.bookName?.let { ReaderDbManager.dropTable(it) }
                File(getSavePath(), it.bookName).deleteRecursively()
                bookList.filter { it.bookname.get() == bookname }.let { bookList.removeAll(it) }
                delete(it)
            }
        }
    }

    private fun updateCatalog(bookBean: BookBean) {
        try {
            DownloadCatalog(bookBean.bookname.get()!!, bookBean.downloadURL.get() ?: "").download()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    //书架将要显示的书籍封面图片
    private fun getBitmap(bookname: String): Bitmap? =
        File("${getSavePath()}/$bookname", IMAGENAME)
            .takeIf { it.exists() }
            ?.let { BitmapFactory.decodeFile(it.path) }
}