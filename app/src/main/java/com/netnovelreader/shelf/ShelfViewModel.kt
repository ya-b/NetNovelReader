package com.netnovelreader.shelf

import android.content.Intent
import android.databinding.BaseObservable
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

    private var shelfModel: ShelfModel? = null

    override fun getModel(): ShelfModel?{
        shelfModel ?: run{
            synchronized(this){
                shelfModel ?: run{ shelfModel = ShelfModel() }
            }
        }
        return shelfModel
    }

    override fun updateBookList(): Boolean{
        var dbManager = ShelfSQLManager()
        shelfModel ?: shelfModel!!.bookList.clear()
        var cursor = dbManager.queryBookList()
        while (cursor != null && cursor.moveToNext()){
            var bookBean = ShelfModel.BookInfoBean(cursor.getInt(cursor.getColumnIndex(BaseSQLManager.ID)),
                    cursor.getString(cursor.getColumnIndex(BaseSQLManager.BOOKNAME)),
                    cursor.getString(cursor.getColumnIndex(BaseSQLManager.LATESTCHAPTER)),
                    cursor.getString(cursor.getColumnIndex(BaseSQLManager.READRECORD)),
                    cursor.getString(cursor.getColumnIndex(BaseSQLManager.DOWNLOADURL)))
            shelfModel?.bookList?.add(bookBean)
        }
        cursor?.close()
        dbManager.closeDB()
        return true
    }

    class ShelfClickEvent : IClickEvent {
        fun startReaderActivity(v: View){
            var intent = Intent(v.context, ReaderActivity::class.java)
            intent.putExtra("bookname", v.nameView.text.toString())
            v.context.startActivity(intent)
        }
    }
}