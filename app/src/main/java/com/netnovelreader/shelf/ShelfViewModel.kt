package com.netnovelreader.shelf

import android.content.Intent
import android.view.View
import com.netnovelreader.base.IClickEvent
import com.netnovelreader.data.database.BaseSQLManager
import com.netnovelreader.data.database.ShelfSQLManager
import com.netnovelreader.reader.ReaderActivity
import kotlinx.android.synthetic.main.item_shelf_recycler_view.view.*

/**
 * Created by yangbo on 2018/1/12.
 */
class ShelfViewModel : IShelfContract.IShelfViewModel {

    private var shelfBean: ShelfBean? = null

    override fun getModel(): ShelfBean?{
        shelfBean ?: run{
            synchronized(this){
                shelfBean ?: run{ shelfBean = ShelfBean() }
            }
        }
        return shelfBean
    }

    override fun updateBookList(): Boolean{
        var dbManager = ShelfSQLManager()
        shelfBean?.bookList?.clear()
        var cursor = dbManager.queryBookList()
        while (cursor != null && cursor.moveToNext()){
            var bookBean = ShelfBean.BookInfoBean(cursor.getInt(cursor.getColumnIndex(BaseSQLManager.ID)),
                    cursor.getString(cursor.getColumnIndex(BaseSQLManager.BOOKNAME)),
                    cursor.getString(cursor.getColumnIndex(BaseSQLManager.READRECORD)) ?: "",
                    cursor.getString(cursor.getColumnIndex(BaseSQLManager.DOWNLOADURL)))
            shelfBean?.bookList?.add(bookBean)
        }
        cursor?.close()
        dbManager.closeDB()
        return true
    }
}