package com.netnovelreader.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.databinding.ObservableArrayList
import android.databinding.ObservableField
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.support.v4.content.ContextCompat
import com.netnovelreader.R
import com.netnovelreader.ReaderApplication.Companion.threadPool
import com.netnovelreader.bean.BookBean
import com.netnovelreader.common.IMAGENAME
import com.netnovelreader.common.getSavePath
import com.netnovelreader.data.db.ReaderDbManager
import com.netnovelreader.data.db.ShelfBean
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
                val list = ReaderDbManager.getRoomDB().shelfDao().getAll()
                bookList.forEach { bean ->
                    list?.forEach {
                        //如果该书在数据库里面，则更新该书状态，比如最新章节的变化
                        if(it.bookName == bean.bookname.get()){
                            bean.latestChapter.set(it.latestChapter)
                            bean.downloadURL.set(it.downloadUrl)
                            bean.isUpdate.set(it.isUpdate)
                        }
                    }
                }
            }
        }
    }

    //取消书籍更新标志"●",设为最近阅读
    override suspend fun cancelUpdateFlag(bookname: String) {
        val latestRead = ReaderDbManager.getRoomDB().shelfDao().getLatestReaded() ?: 0
        ReaderDbManager.getRoomDB().shelfDao().replace(ShelfBean(bookName = bookname,
                isUpdate = "",
                latestRead = latestRead + 1)
        )
    }

    /**
     * 刷新书架，重新读数据库（数据库有没有更新）
     */
    override suspend fun refreshBookList() {
        val bookDirList = dirBookList()
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
        ReaderDbManager.getRoomDB().shelfDao().apply {
            this.getBookInfo(bookname)?.also {
                ReaderDbManager.dropTable(it.bookName ?: "")
                File(getSavePath(), it.bookName).deleteRecursively()
                for (i in 0 until bookList.size) {
                    if (bookList[i].bookname.get() == bookname) {
                        bookList.removeAt(i)
                        break
                    }
                }
                this.delete(it)
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
        try {
            DownloadCatalog(bookBean.bookname.get()!!, bookBean.downloadURL.get() ?: "").download()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    //书架将要显示的书籍封面图片
    private fun getBitmap(bookname: String): Bitmap {
        val file = File("${getSavePath()}/$bookname", IMAGENAME)
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