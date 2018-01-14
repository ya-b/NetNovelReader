package com.netnovelreader.data.database

import android.database.Cursor

/**
 * Created by yangbo on 18-1-14.
 */
class SearchSQLManager : BaseSQLManager {
    companion object {
        val SEARCH_NAME = "searchname"
    }
    constructor(){
        db = super.getDataBase()
        db?.execSQL("create table if not exists ${BaseSQLManager.TABLE_SEARCH} (" +
                "${BaseSQLManager.ID} integer primary key, " +
                "${BaseSQLManager.SEARCH_HOSTNAME} varchar(128) unique, " +
                "${BaseSQLManager.ISREDIRECT} integer, " +
                "${BaseSQLManager.SEARCHURL} text, " +
                "${BaseSQLManager.REDIRECTFILELD} varchar(128), " +
                "${BaseSQLManager.REDIRECTSELECTOR} varchar(128), " +
                "${BaseSQLManager.NOREDIRECTSELECTOR} varchar(128), " +
                "${BaseSQLManager.SEARCHCHARSET} varchar(128));")
        var cursor = queryAll()
        if(cursor != null && !cursor.moveToFirst()){
            db?.execSQL("insert into ${BaseSQLManager.TABLE_SEARCH} values (1,'qidian.com',0," +
                    "'http://se.qidian.com/?kw=$SEARCH_NAME','',''," +
                    "'.book-img-text > ul:nth-child(1) > li:nth-child(1)','utf-8');")
            db?.execSQL("insert into ${BaseSQLManager.TABLE_SEARCH} values (2,'yunlaige.com',1," +
                    "'http://www.yunlaige.com/modules/article/search.php?searchkey=$SEARCH_NAME&action=login&submit='," +
                    "'location','.readnow','li.clearfix:nth-child(1) > div:nth-child(2) > div:nth-child(1) > h2:nth-child(2) > a:nth-child(1)','gbk');")
        }
        cursor?.close()
    }

    fun queryAll(): Cursor? {
        return db?.rawQuery("select * from ${BaseSQLManager.TABLE_SEARCH}", null)
    }
}