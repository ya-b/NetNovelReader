package com.netnovelreader.data.database

import android.content.ContentValues
import android.database.Cursor
import java.net.URL

/**
 * Created by yangbo on 18-1-12.
 */
class ShelfSQLManager : BaseSQLManager() {
    init {
        getDB().execSQL("create table if not exists $TABLE_SHELF ($ID integer primary key, " +
                "$BOOKNAME varchar(128) unique, $READRECORD varchar(128), $DOWNLOADURL indicator);")
    }

    fun queryBookList(): Cursor? = db?.rawQuery("select * from $TABLE_SHELF;", null)

    fun addBookToShelf(bookname: String, url: String): Int{
        var id = 0
        val cursor = db!!.rawQuery("select $ID from $TABLE_SHELF where $BOOKNAME='$bookname';", null)
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

    fun getRecord(bookname: String): Array<String>{
        val result = Array<String>(2){""}
        val cursor = getDB().
                rawQuery("select $ID,$READRECORD from $TABLE_SHELF where $BOOKNAME='$bookname';", null)
        if(cursor.moveToFirst()){
            result[0] = cursor.getString(0) ?: ""
            result[1] = cursor.getString(1) ?: ""
        }
        cursor.close()
        closeDB()
        return result
    }

    fun setRecord(bookname: String, record: String){
        getDB().execSQL("update $TABLE_SHELF set $READRECORD='$record' where $BOOKNAME='$bookname';");
        closeDB()
    }
}