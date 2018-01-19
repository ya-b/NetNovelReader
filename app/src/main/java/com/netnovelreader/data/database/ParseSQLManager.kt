package com.netnovelreader.data.database

import android.database.sqlite.SQLiteDatabase
import org.jsoup.Connection

/**
 * Created by yangbo on 18-1-12.
 */
class ParseSQLManager : BaseSQLManager() {
    init {
        getDB().execSQL("create table if not exists $TABLE_PARSERULES ($ID integer primary key," +
                "$HOSTNAME varchar(128) unique,$CATALOG_RULE indicator,$CHAPTER_RULE indicator," +
                "$CHARSET varchar(128),$COVER_RULE indicator);")
        val cursor = getDB().rawQuery("select * from $TABLE_PARSERULES", null)
        if(cursor != null && !cursor.moveToFirst()){
            getDB().execSQL("insert into $TABLE_PARSERULES values (1,'qidian.com','.volume-wrap','.read-content','utf-8',NULL);")
            getDB().execSQL("insert into $TABLE_PARSERULES values (2,'yunlaige.com','#contenttable','#content','gbk',NULL);")
        }
        cursor?.close()
        closeDB()
    }

    fun getChapterRule(hostname: String, field: String): String?{
        var rule: String? = null
        var cursor = getDB().rawQuery("select $field from ${BaseSQLManager.TABLE_PARSERULES} " +
                "where ${BaseSQLManager.HOSTNAME}='${hostname}';", null)
        if(cursor!!.moveToFirst()){
            rule = cursor.getString(0)
        }
        cursor.close()
        closeDB()
        return rule
    }
}