package com.netnovelreader.data.database

import android.database.sqlite.SQLiteDatabase

/**
 * Created by yangbo on 18-1-12.
 */
class ChapterSQLManager : BaseSQLManager() {
    fun createTable(tableName : String): ChapterSQLManager {
        db!!.execSQL("create table if not exists $tableName (${ID} integer primary key," +
                "${CHAPTERNAME} varchar(128) unique, ${CHAPTERURL} text);")
        return this
    }

    fun addAllChapter(map: LinkedHashMap<String, String>, tableName : String): Boolean{
        try {
            db!!.beginTransaction()
            var ite = map.iterator()
            while (ite.hasNext()){
                val entry = ite.next()
                db!!.execSQL("insert into $tableName (${CHAPTERNAME}, ${CHAPTERURL}) values (" +
                        "'${entry.key}','${entry.value}')")
            }
            db!!.setTransactionSuccessful()
        }finally {
            db!!.endTransaction()
        }
        closeDB()
        return true
    }
}