package com.netnovelreader.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.databinding.ObservableArrayList
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.support.v4.content.ContextCompat
import com.netnovelreader.R
import com.netnovelreader.ReaderApplication.Companion.threadPool
import com.netnovelreader.bean.BookBean
import com.netnovelreader.common.IMAGENAME
import com.netnovelreader.common.getSavePath
import com.netnovelreader.common.id2TableName
import com.netnovelreader.data.db.ReaderDbManager
import com.netnovelreader.data.network.DownloadCatalog
import com.netnovelreader.interfaces.IShelfContract
import kotlinx.coroutines.experimental.launch
import java.io.File
import java.io.IOException
import java.util.*

/**
 * Created by yangbo on 2018/1/12.
 */
class ShelfViewModel(val context: Application) : AndroidViewModel(context),
    IShelfContract.IShelfViewModel {

    val bookList by lazy {
        MutableLiveData<ObservableArrayList<BookBean>>().run {
            value = ObservableArrayList(); value!!
        }
    }

    //检查书籍是否有更新
    @Synchronized
    override suspend fun updateBooks() {
        bookList.forEach {
            updateCatalog(it).invokeOnCompletion {
                val bookMap = ReaderDbManager.queryShelfBookList()
                bookList.forEach { bean ->
                    val value = bookMap[bean.bookid.get()]
                    if (value != null) {       //如果该书在数据库里面，则更新该书状态，比如最新章节的变化
                        value[0].takeIf { it != bean.bookname.get() }
                            ?.apply { bean.bookname.set(this) }
                        value[1].takeIf { it != bean.latestChapter.get() }
                            ?.apply { bean.latestChapter.set(this) }
                        value[2].takeIf { it != bean.downloadURL.get() }
                            ?.apply { bean.downloadURL.set(this) }
                        value[3].takeIf { it != bean.isUpdate.get() }
                            ?.apply { bean.isUpdate.set(this) }
                    }
                }
            }
        }
    }

    //取消书籍更新标志"●",设为最近阅读
    override suspend fun cancelUpdateFlag(bookname: String) {
        ReaderDbManager.cancelUpdateFlag(bookname)
        ReaderDbManager.setLatestRead(bookname)
    }

    /**
     * 刷新书架，重新读数据库（数据库有没有更新）
     */
    override suspend fun refreshBookList() {
        val bookDirList = dirBookList()
        val bookMap = ReaderDbManager.queryShelfBookList()   //数据库里面的所有书
        val temp = ArrayList<BookBean>()
        bookMap.forEach {
            val bookBean = BookBean(
                ObservableInt(it.key),
                ObservableField(it.value[0]),
                ObservableField(it.value[1]),
                ObservableField(it.value[2]),
                ObservableField(getBitmap(it.key)),
                ObservableField(it.value[3])
            )
            if (bookDirList?.contains(id2TableName(bookBean.bookid.get())) == true) {
                temp.add(bookBean)
                if (ReaderDbManager.getChapterCount(id2TableName(bookBean.bookid.get())) == 0) {
                    updateCatalog(bookBean)
                }
            } else {
                bookBean.bookname.get()?.run { deleteBook(this) }
            }
        }
        bookList.clear()
        bookList.addAll(temp)
    }

    //删除书籍
    override suspend fun deleteBook(bookname: String) {
        ReaderDbManager.removeBookFromShelf(bookname).takeIf { it > -1 }?.apply {
            ReaderDbManager.dropTable(id2TableName(this))
            File(getSavePath(), id2TableName(this)).deleteRecursively()
            for (i in 0 until bookList.size) {
                if (bookList[i].bookname.get() == bookname) {
                    bookList.removeAt(i)
                    break
                }
            }
        }
    }

    //获取文件夹里面的书列表
    private fun dirBookList(): Array<String>? = File(getSavePath()).takeIf { it.exists() }?.list()

    /**
     * @must  false时：判断[ReaderDbManager.getChapterCount]目录为空，则更新
     * 更新目录
     */
    private fun updateCatalog(bookBean: BookBean) = launch(threadPool) {
        val tableName = id2TableName(bookBean.bookid.get())
        try {
            DownloadCatalog(tableName, bookBean.downloadURL.get() ?: "").download()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    //书架将要显示的书籍封面图片
    private fun getBitmap(bookId: Int): Bitmap {
        val file = File("${getSavePath()}/${id2TableName(bookId)}", IMAGENAME)
        return if (file.exists()) {
            BitmapFactory.decodeFile(file.path)
        } else {
            ((ContextCompat.getDrawable(
                context,
                R.drawable.cover_default
            ) as BitmapDrawable)).bitmap
        }
    }
}