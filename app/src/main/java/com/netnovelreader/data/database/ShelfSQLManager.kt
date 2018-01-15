package com.netnovelreader.data.database

import android.content.ContentValues
import android.database.Cursor
import java.net.URL

/**
 * Created by yangbo on 18-1-12.
 */
class ShelfSQLManager : BaseSQLManager {
    constructor() : super() {
        db!!.execSQL("create table if not exists $TABLE_SHELF ($ID integer primary key, " +
                "$BOOKNAME varchar(128) unique, $READRECORD varchar(128), $DOWNLOADURL text);")
    }

    fun queryBookList(): Cursor? = db?.rawQuery("select * from $TABLE_SHELF;", null)

    fun addBookToShelf(bookname: String, url: String): Int{
        var id = 0
        var cursor = db!!.rawQuery("select $ID from $TABLE_SHELF where $BOOKNAME='$bookname';", null)
        if(cursor.moveToFirst()){
            id = cursor.getInt(0)
        }else{
            var contentValue = ContentValues()
            contentValue.put(BOOKNAME, bookname)
            contentValue.put(DOWNLOADURL, url)
            id = db!!.insert(TABLE_SHELF, null, contentValue).toInt()
        }
        cursor.close()
        closeDB()
        return id
    }
}