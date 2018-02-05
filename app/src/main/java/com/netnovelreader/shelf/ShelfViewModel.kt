package com.netnovelreader.shelf

import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import com.netnovelreader.common.IMAGENAME
import com.netnovelreader.common.ObservableSyncArrayList
import com.netnovelreader.common.data.SQLHelper
import com.netnovelreader.common.download.DownloadCatalog
import com.netnovelreader.common.getSavePath
import com.netnovelreader.common.id2TableName
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.Executors

/**
 * Created by yangbo on 2018/1/12.
 */
class ShelfViewModel : IShelfContract.IShelfViewModel {

    var bookList: ObservableSyncArrayList<BookBean>

    init {
        bookList = ObservableSyncArrayList()
    }

    //检查书籍是否有更新
    override fun updateBooks(): Boolean {
        val threadPoolExecutor = Executors.newFixedThreadPool(5)
        var i = 0
        var tmp = 0
        Observable.fromIterable(bookList).flatMap { bean ->
            Observable.create<Int> { emitter ->
                try {
                    DownloadCatalog(
                            id2TableName(bean.bookid.get()),
                            bean.downloadURL.get() ?: ""
                    ).download()
                } catch (e: IOException) {
                } finally {
                    emitter.onNext(++i)
                }
            }.subscribeOn(Schedulers.from(threadPoolExecutor))
        }.observeOn(Schedulers.single()).subscribe {
            if (i > tmp && (i % 3 == 0 || i == bookList.size)) {  //避免刷新太频繁导致recyclerview崩溃
                refreshBookList()
                tmp = i
            }
        }
        return true
    }

    //取消书籍更新标志"●",设为最近阅读
    override fun cancelUpdateFlag(bookname: String) {
        SQLHelper.cancelUpdateFlag(bookname)
        SQLHelper.setLatestRead(bookname)
    }

    /**
     * 刷新书架，从数据库重新获取
     */
    @Synchronized
    override fun refreshBookList() {
        val arrayList = ArrayList<BookBean>()
        val bookDirList = dirBookList()
        val map = SQLHelper.queryShelfBookList()
        map.forEach {
            val bookBean = BookBean(ObservableInt(it.key), ObservableField(it.value[0]), ObservableField(it.value[1]),
                    ObservableField(it.value[2]), ObservableField(getBitmap(it.key)), ObservableField(it.value[3]))
            if (bookDirList.contains(id2TableName(bookBean.bookid.get()))) {
                arrayList.add(bookBean)
                Thread { updateCatalog(bookBean) }.start()
            } else {
                Thread { deleteBook(bookBean.bookname.get() ?: "") }.start()
            }
        }
        bookList.clear()
        bookList.addAll(arrayList)
    }

    //删除书籍
    override fun deleteBook(bookname: String) {
        val id = SQLHelper.removeBookFromShelf(bookname)
        if (id == -1) return
        Thread {
            SQLHelper.dropTable(id2TableName(id))
            File(getSavePath(), id2TableName(id)).deleteRecursively()
        }.start()
    }

    //获取文件夹里面的书列表
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

    //更新目录
    private fun updateCatalog(bookBean: BookBean) {
        val tableName = id2TableName(bookBean.bookid.get())
        if (SQLHelper.getChapterCount(tableName) == 0) {
            try {
                DownloadCatalog(tableName, bookBean.downloadURL.get() ?: "").download()
            } catch (e: IOException) {
                Log.d("Reader:ShelfViewModel", e.printStackTrace().toString())
            }
        }
    }

    //书架将要显示的书籍封面图片
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