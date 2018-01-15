package com.netnovelreader.data.database

import android.database.sqlite.SQLiteDatabase

/**
 * Created by yangbo on 18-1-12.
 */
class ChapterSQLManager : BaseSQLManager() {
    fun createTable(tableName : String): ChapterSQLManager {
        db!!.execSQL("create table if not exists $tableName (${ID} integer primary key," +
                "${CHAPTERNAME} varchar(128) unique, ${CHAPTERURL} text, ${ISDOWNLOADED} var char(128);")
        return this
    }

    fun addAllChapter(map: LinkedHashMap<String, String>, tableName : String): Boolean{
        try {
            db!!.beginTransaction()
            var ite = map.iterator()
            while (ite.hasNext()){
                val entry = ite.next()
                //0表示没有下载
                db!!.execSQL("insert into $tableName (${CHAPTERNAME}, ${CHAPTERURL}, ${ISDOWNLOADED}) " +
                        "values ('${entry.key}','${entry.value}','0')")
            }
            db!!.setTransactionSuccessful()
        }finally {
            db!!.endTransaction()
        }
        closeDB()
        return true
    }

    fun finishChapter(tableName: String, chaptername: String, isDownloadSuccess: Boolean){
        if(isDownloadSuccess){
            db!!.execSQL("update $tableName set ${ISDOWNLOADED}='1' where ${CHAPTERNAME}='$chaptername';")
        }else{
            db!!.execSQL("update $tableName set ${ISDOWNLOADED}='0' where ${CHAPTERNAME}='$chaptername';")
        }
        closeDB()
    }
}