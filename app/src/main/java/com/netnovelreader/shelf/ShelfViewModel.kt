package com.netnovelreader.shelf

import android.databinding.ObservableArrayList
import android.databinding.ObservableField
import android.databinding.ObservableInt
import com.netnovelreader.data.database.BaseSQLManager
import com.netnovelreader.data.database.ShelfSQLManager

/**
 * Created by yangbo on 2018/1/12.
 */
class ShelfViewModel : IShelfContract.IShelfViewModel {

    var bookList: ObservableArrayList<ShelfBean>
    init {
        bookList = ObservableArrayList<ShelfBean>()
    }

    /**
     * 更新书架数据
     */
    override fun updateBookList(){
        var dbManager = ShelfSQLManager()
        bookList.clear()
        var cursor = dbManager.queryBookList()
        while (cursor != null && cursor.moveToNext()){
            var bookBean = ShelfBean(ObservableInt(cursor.getInt(cursor.getColumnIndex(BaseSQLManager.ID))),
                    ObservableField(cursor.getString(cursor.getColumnIndex(BaseSQLManager.BOOKNAME))),
                    ObservableField(cursor.getString(cursor.getColumnIndex(BaseSQLManager.READRECORD)) ?: ""),
                    ObservableField(cursor.getString(cursor.getColumnIndex(BaseSQLManager.DOWNLOADURL))))
            bookList.add(bookBean)
        }
        cursor?.close()
        dbManager.closeDB()
    }
}