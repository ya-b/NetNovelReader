package com.netnovelreader.shelf

import android.databinding.ObservableArrayList
import android.databinding.ObservableField
import android.databinding.ObservableInt
import com.netnovelreader.common.DownloadTask
import com.netnovelreader.common.getSavePath
import com.netnovelreader.common.id2TableName
import com.netnovelreader.data.SQLHelper
import java.io.File
import java.io.IOException
import java.util.concurrent.Executors

/**
 * Created by yangbo on 2018/1/12.
 */
class ShelfViewModel : IShelfContract.IShelfViewModel {

    var bookList: ObservableArrayList<ShelfBean>

    init {
        bookList = ObservableArrayList()
    }

    //TODO
    override fun updateBooks(): Boolean {
        val threadPoolExecutor = Executors.newFixedThreadPool(5)
        bookList.forEach {
            threadPoolExecutor.execute {
                try {
                    DownloadTask(id2TableName(it.bookid.get()), it.downloadURL.get()).updateSql()
                    refreshBookList()
                } catch (e: IOException) {
                }
            }
        }
        return true
    }

    /**
     * 刷新书架
     */
    override fun refreshBookList() {
        Thread{
            bookList.clear()
            val listInDir = dirBookList()
            val cursor = SQLHelper.queryShelfBookList()
            while (cursor != null && cursor.moveToNext()) {
                val bookBean = ShelfBean(ObservableInt(cursor.getInt(cursor.getColumnIndex(SQLHelper.ID))),
                        ObservableField(cursor.getString(cursor.getColumnIndex(SQLHelper.BOOKNAME))),
                        ObservableField(cursor.getString(cursor.getColumnIndex(SQLHelper.LATESTCHAPTER))
                                ?: ""),
                        ObservableField(cursor.getString(cursor.getColumnIndex(SQLHelper.DOWNLOADURL))))
                if (listInDir.contains(id2TableName(bookBean.bookid.get()))) {
                    bookList.add(bookBean)
                } else {
                    Thread{ deleteBook(bookBean.bookname.get()) }.start()
                }
            }
            cursor?.close()
        }.start()
    }

    override fun deleteBook(bookname: String) {
        val id = SQLHelper.removeBookFromShelf(bookname)
        if (id == -1) return
        Thread{
            SQLHelper.dropTable(id2TableName(id))
            deleteDirs(File(getSavePath(), id2TableName(id)))
        }.start()
    }

    fun deleteDirs(file: File) {
        if (!file.exists()) return
        if (file.isFile) {
            file.delete()
        } else {
            val fileArray = file.listFiles()
            if (fileArray.size > 0) {
                for (i in 0..fileArray.size - 1) {
                    fileArray[i].delete()
                }
                file.delete()
            } else {
                file.delete()
            }
        }
    }

    fun dirBookList(): ArrayList<String> {
        val list = ArrayList<String>()
        val file = File(getSavePath())
        if (file.exists()) {
            file.list().forEach {
                list.add(it)
            }
        }
        return list
    }
}