package com.netnovelreader.shelf

import android.databinding.ObservableArrayList
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.netnovelreader.ReaderApplication.Companion.threadPool
import com.netnovelreader.common.IMAGENAME
import com.netnovelreader.common.data.SQLHelper
import com.netnovelreader.common.download.DownloadCatalog
import com.netnovelreader.common.getDefaultCover
import com.netnovelreader.common.getSavePath
import com.netnovelreader.common.id2TableName
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import java.io.File
import java.io.IOException
import java.util.*

/**
 * Created by yangbo on 2018/1/12.
 */
class ShelfViewModel : IShelfContract.IShelfViewModel {

    var bookList = ObservableArrayList<BookBean>()

    //检查书籍是否有更新
    override fun updateBooks() {
        var i = 0
        bookList.forEach {
            updateCatalog(it, true).invokeOnCompletion {
                synchronized(bookList) {
                    (++i).takeIf { it > bookList.size - 1 || it % 3 == 0 }
                        ?.run { launch { refreshBookList() } }
                }
            }
        }
    }

    //取消书籍更新标志"●",设为最近阅读
    override suspend fun cancelUpdateFlag(bookname: String) {
        SQLHelper.cancelUpdateFlag(bookname)
        SQLHelper.setLatestRead(bookname)
    }

    /**
     * 刷新书架，从数据库重新获取
     */
    override suspend fun refreshBookList() {
        val bookDirList = dirBookList()
        val bookMap = SQLHelper.queryShelfBookList()
        val willDel = ArrayList<BookBean>()
        bookList.forEach { bean ->
            val value = bookMap.get(bean.bookid.get())
            if (value != null) {
                value[0].takeIf { it != bean.bookname.get() }?.apply { bean.bookname.set(this) }
                value[1].takeIf { it != bean.latestChapter.get() }
                    ?.apply { bean.latestChapter.set(this) }
                value[2].takeIf { it != bean.downloadURL.get() }
                    ?.apply { bean.downloadURL.set(this) }
                value[3].takeIf { it != bean.isUpdate.get() }?.apply { bean.isUpdate.set(this) }
            } else {
                willDel.add(bean)
            }
            bookMap.remove(bean.bookid.get())
        }
        willDel.apply { forEach { bookList.remove(it) } }.clear()
        bookMap.forEach {
            val bookBean = BookBean(
                ObservableInt(it.key),
                ObservableField(it.value[0]),
                ObservableField(it.value[1]),
                ObservableField(it.value[2]),
                ObservableField(getBitmap(it.key).await()),
                ObservableField(it.value[3])
            )
            if (bookDirList?.contains(id2TableName(bookBean.bookid.get())) ?: false) {
                willDel.add(bookBean)
                updateCatalog(bookBean, false)
            } else {
                bookBean.bookname.get()?.run { deleteBook(this) }
            }
        }
        bookList.addAll(willDel)
    }

    //删除书籍
    override suspend fun deleteBook(bookname: String) {
        SQLHelper.removeBookFromShelf(bookname).takeIf { it > -1 }?.apply {
            SQLHelper.dropTable(id2TableName(this))
            File(getSavePath(), id2TableName(this)).deleteRecursively()
        }
    }

    //获取文件夹里面的书列表
    private fun dirBookList(): Array<String>? = File(getSavePath()).takeIf { it.exists() }?.list()

    //更新目录
    private fun updateCatalog(bookBean: BookBean, must: Boolean) =
        launch(threadPool) {
            val tableName = id2TableName(bookBean.bookid.get())
            if (must || SQLHelper.getChapterCount(tableName) == 0) {
                try {
                    DownloadCatalog(tableName, bookBean.downloadURL.get() ?: "").download()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

    //书架将要显示的书籍封面图片
    private fun getBitmap(bookId: Int) = async(threadPool) {
        File("${getSavePath()}/${id2TableName(bookId)}", IMAGENAME)
            .takeIf { it.exists() }
            ?.let { BitmapFactory.decodeFile(it.path) } ?: getDefaultCover()
    }
}