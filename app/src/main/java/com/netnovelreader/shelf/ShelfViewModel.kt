package com.netnovelreader.shelf

import android.databinding.ObservableArrayList
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import com.netnovelreader.common.IMAGENAME
import com.netnovelreader.common.getSavePath
import com.netnovelreader.common.id2TableName
import com.netnovelreader.data.SQLHelper
import com.netnovelreader.download.DownloadCatalog
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

    override fun updateBooks(): Boolean {
        val threadPoolExecutor = Executors.newFixedThreadPool(5)
        bookList.forEach { bean ->
            threadPoolExecutor.execute {
                DownloadCatalog(id2TableName(bean.bookid.get()), bean.downloadURL.get()).download()
                refreshBookList()
            }
        }
        return true
    }

    override fun cancelUpdateFlag(bookname: String) {
        SQLHelper.getDB().execSQL(
            "update ${SQLHelper.TABLE_SHELF} set ${SQLHelper.ISUPDATE}='' " +
                    "where ${SQLHelper.BOOKNAME}='$bookname';"
        )
    }

    /**
     * 刷新书架
     */
    override fun refreshBookList() {
        Thread {
            bookList.clear()
            val listInDir = dirBookList()
            val cursor = SQLHelper.queryShelfBookList()
            while (cursor.moveToNext()) {
                val bookId = cursor.getInt(cursor.getColumnIndex(SQLHelper.ID))
                val latestChapter = cursor.getString(cursor.getColumnIndex(SQLHelper.LATESTCHAPTER))
                val bookBean = ShelfBean(
                    ObservableInt(bookId),
                    ObservableField(cursor.getString(cursor.getColumnIndex(SQLHelper.BOOKNAME))),
                    ObservableField(latestChapter),
                    ObservableField(cursor.getString(cursor.getColumnIndex(SQLHelper.DOWNLOADURL))),
                    ObservableField(getBitmap(bookId)),
                    ObservableField(cursor.getString(cursor.getColumnIndex(SQLHelper.ISUPDATE)))
                )
                if (listInDir.contains(id2TableName(bookBean.bookid.get()))) {
                    bookList.add(bookBean)
                    Thread { checkCatalog(bookBean) }.start()
                } else {
                    Thread { deleteBook(bookBean.bookname.get()) }.start()
                }
            }
            cursor.close()
        }.start()
    }

    override fun deleteBook(bookname: String) {
        val id = SQLHelper.removeBookFromShelf(bookname)
        if (id == -1) return
        Thread {
            SQLHelper.dropTable(id2TableName(id))
            File(getSavePath(), id2TableName(id)).deleteRecursively()
        }.start()
    }

    private fun dirBookList(): ArrayList<String> {
        val list = ArrayList<String>()
        val file = File(getSavePath())
        if (file.exists()) {
            file.list().forEach {
                list.add(it)
            }
        }
        return list
    }

    private fun checkCatalog(bookBean: ShelfBean) {
        val tableName = id2TableName(bookBean.bookid.get())
        if (SQLHelper.getChapterCount(tableName) == 0) {
            try {
                DownloadCatalog(tableName, bookBean.downloadURL.get()).download()
            } catch (e: IOException) {
                Log.d("Reader:ShelfViewModel", e.printStackTrace().toString())
            }
        }
    }

    private fun getBitmap(bookId: Int): Bitmap {
        val file = File(getSavePath() + "/${id2TableName(bookId)}", IMAGENAME)
        var bitmap: Bitmap? = null
        if (file.exists()) {
            bitmap = BitmapFactory.decodeFile(file.path)
        }
        return bitmap ?: Bitmap.createBitmap(
            IntArray(45 * 60) { _ -> Color.parseColor("#7092bf") },
            45, 60, Bitmap.Config.RGB_565
        )
    }
}