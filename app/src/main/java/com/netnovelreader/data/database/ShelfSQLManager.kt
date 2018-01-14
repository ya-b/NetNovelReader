package com.netnovelreader.data.database

import android.database.Cursor

/**
 * Created by yangbo on 18-1-12.
 */
class ShelfSQLManager : BaseSQLManager {
    constructor() {
        db!!.execSQL("create table if not exists $TABLE_SHELF ($ID integer primary key, " +
                "$BOOKNAME varchar(128) unique,$LATESTCHAPTER text,$READRECORD varchar(128)," +
                "$BOOKTABLE varchar(128),$BOOKDIR varchar(128), $DOWNLOADURL text);")
        testShelf()
    }

    fun queryBookList(): Cursor? = db?.rawQuery("select * from $TABLE_SHELF;", null)

    //TODO delete
    fun testShelf(){
        var cursor = queryBookList()
        if(cursor != null && !cursor.moveToFirst()){
            db!!.execSQL("insert into $TABLE_SHELF ($BOOKNAME, $LATESTCHAPTER, $READRECORD, " +
                    "$BOOKTABLE, $BOOKDIR,$DOWNLOADURL) values ('${"c++ Primer"}', '${"chapter 32"}'," +
                    "'${"chapter32#3"}','${"33333"}', '${"33333"}','${"http://hello.world/nihao"}');")
        }
        cursor?.close()
    }
}