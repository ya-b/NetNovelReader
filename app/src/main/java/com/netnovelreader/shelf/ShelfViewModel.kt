package com.netnovelreader.shelf

import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.graphics.BitmapFactory
import com.netnovelreader.common.*
import com.netnovelreader.common.data.SQLHelper
import com.netnovelreader.common.download.DownloadCatalog
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newFixedThreadPoolContext
import java.io.File
import java.io.IOException
import java.util.*

/**
 * Created by yangbo on 2018/1/12.
 */
class ShelfViewModel : IShelfContract.IShelfViewModel {

    var bookList = ObservableSyncArrayList<BookBean>()
    var poolContext = newFixedThreadPoolContext(THREAD_NUM, "DownloadService")

    //检查书籍是否有更新
    override fun updateBooks() {
        var i = 0
        launch {
            bookList.forEach {
                updateCatalog(it, true).invokeOnCompletion {
                    synchronized(bookList) {
                        (++i).takeIf { it > bookList.size - 1 || it % 3 == 0 }
                            ?.run { refreshBookList() }
                    }
                }
            }
        }
    }

    //取消书籍更新标志"●",设为最近阅读
    override fun cancelUpdateFlag(bookname: String) {
        launch {
            SQLHelper.cancelUpdateFlag(bookname)
            SQLHelper.setLatestRead(bookname)
        }
    }

    /**
     * 刷新书架，从数据库重新获取
     */
    @Synchronized
    override fun refreshBookList() {
        launch {
            val arrayList = ArrayList<BookBean>()
            val bookDirList = dirBookList().await()
            SQLHelper.queryShelfBookList().forEach {
                val bookBean = BookBean(
                    ObservableInt(it.key),
                    ObservableField(it.value[0]),
                    ObservableField(it.value[1]),
                    ObservableField(it.value[2]),
                    ObservableField(getBitmap(it.key).await()),
                    ObservableField(it.value[3])
                )
                if (bookDirList.contains(id2TableName(bookBean.bookid.get()))) {
                    arrayList.add(bookBean)
                    updateCatalog(bookBean, false)
                } else {
                    bookBean.bookname.get()?.run { deleteBook(this) }
                }
            }
            bookList.clear()
            bookList.addAll(arrayList)
        }
    }

    //删除书籍
    override fun deleteBook(bookname: String) {
        launch(poolContext) {
            SQLHelper.removeBookFromShelf(bookname).takeIf { it > -1 }?.apply {
                SQLHelper.dropTable(id2TableName(this))
                File(getSavePath(), id2TableName(this)).deleteRecursively()
            }
        }
    }

    //获取文件夹里面的书列表
    private fun dirBookList() = async(poolContext) {
        val list = ArrayList<String>()
        File(getSavePath()).takeIf { it.exists() }?.list()?.forEach { list.add(it) }
        list
    }

    //更新目录
    private fun updateCatalog(bookBean: BookBean, must: Boolean) =
        async(poolContext) {
            val tableName = id2TableName(bookBean.bookid.get())
            if (must || SQLHelper.getChapterCount(tableName) == 0) {
                try {
                    DownloadCatalog(tableName, bookBean.downloadURL.get() ?: "").download()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            1
        }

    //书架将要显示的书籍封面图片
    private fun getBitmap(bookId: Int) = async(poolContext) {
        File("${getSavePath()}/${id2TableName(bookId)}", IMAGENAME)
            .takeIf { it.exists() }
            ?.let { BitmapFactory.decodeFile(it.path) }
                ?: getDefaultCover()
    }
}