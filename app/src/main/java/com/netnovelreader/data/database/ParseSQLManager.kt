package com.netnovelreader.data.database

import android.database.sqlite.SQLiteDatabase
import org.jsoup.Connection

/**
 * Created by yangbo on 18-1-12.
 */
class ParseSQLManager : BaseSQLManager() {
    init {
        getDB().execSQL("create table if not exists ${BaseSQLManager.TABLE_PARSERULES} (${BaseSQLManager.ID} integer primary key," +
                "${BaseSQLManager.HOSTNAME} varchar(128) unique,${BaseSQLManager.CATALOG_RULE} text,${BaseSQLManager.CHAPTER_RULE} text," +
                "${BaseSQLManager.CHARSET} varchar(128),${BaseSQLManager.COVER_RULE} text);")
        val cursor = getDB().rawQuery("select * from ${BaseSQLManager.TABLE_PARSERULES}", null)
        if(cursor != null && !cursor.moveToFirst()){
            getDB().execSQL("insert into ${BaseSQLManager.TABLE_PARSERULES} values (1,'qidian.com','.volume-wrap','.read-content','utf-8',NULL);")
            getDB().execSQL("insert into ${BaseSQLManager.TABLE_PARSERULES} values (2,'yunlaige.com','#contenttable','#content','gbk',NULL);")
        }
        cursor?.close()
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