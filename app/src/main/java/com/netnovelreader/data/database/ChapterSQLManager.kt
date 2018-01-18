package com.netnovelreader.data.database

import android.database.Cursor
import android.util.Log

/**
 * Created by yangbo on 18-1-12.
 */
class ChapterSQLManager : BaseSQLManager() {
    @Synchronized
    fun createTable(tableName : String): ChapterSQLManager {
        getDB().execSQL("create table if not exists $tableName (${ID} integer primary key," +
                "${CHAPTERNAME} varchar(128), ${CHAPTERURL} indicator, ${ISDOWNLOADED} var char(128));")
        return this
    }

    @Synchronized
    fun isTableExists(tableName: String): Boolean{
        var result = false
        var cursor: Cursor? = null
        try {
            cursor = getDB().rawQuery("select * from Sqlite_master  where type ='table' and name ='"
                    + tableName.trim() + "' ", null)
            if (cursor!!.moveToNext()) {
                result = true
            }
        } catch (e: Exception) {
        }finally {
            cursor?.close()
        }
        return result
    }

    @Synchronized
    fun addAllChapter(map: LinkedHashMap<String, String>, tableName : String): Boolean{
        try {
            getDB().beginTransaction()
            var ite = map.iterator()
            while (ite.hasNext()){
                val entry = ite.next()
                getDB().execSQL("insert into $tableName (${CHAPTERNAME}, ${CHAPTERURL}, ${ISDOWNLOADED}) "
                        + "values ('${entry.key}','${entry.value}','0')")
            }
            getDB().setTransactionSuccessful()
        }finally {
            getDB().endTransaction()
        }
        return true
    }

    @Synchronized
    fun setChapterFinish(tableName: String, chaptername: String, isDownloadSuccess: Boolean, url: String){
        var cursor = getDB().rawQuery("select * from $tableName where $CHAPTERNAME='$chaptername';",null)
        if(!cursor.moveToNext()){
            getDB().execSQL("insert into $tableName ($CHAPTERNAME, $CHAPTERURL, $ISDOWNLOADED) "
                    + "values ('$chaptername','$url','${compareValues(isDownloadSuccess, false)}')")
        }else{
            getDB().execSQL("update $tableName set ${ISDOWNLOADED}='${compareValues(isDownloadSuccess, false)}' " +
                    "where ${CHAPTERNAME}='$chaptername';")
        }
        cursor.close()
    }

    @Synchronized
    fun getDownloaded(tableName: String): ArrayList<String>{
        val arrayList = ArrayList<String>()
        val cursor = getDB().rawQuery("select $CHAPTERNAME from $tableName where " +
                "$ISDOWNLOADED='1';", null)
        while (cursor.moveToNext()){
            arrayList.add(cursor.getString(0))
        }
        cursor.close()
        return arrayList
    }

    fun getChapterName(tableName: String, id: Int): String{
        var chapterName: String? = null
        val cursor = getDB().rawQuery("select $CHAPTERNAME from $tableName where $ID=$id;", null)
        if (cursor.moveToFirst()){
            chapterName = cursor.getString(0)
        }
        cursor.close()
        closeDB()
        return chapterName ?: ""
    }

    fun getChapterCount(tableName: String): Int{
        var c = 1
        var cursor = getDB().rawQuery("select count(*) from $tableName;", null)
        if(cursor.moveToFirst()){
            c = cursor.getInt(0)
        }
        cursor.close()
        closeDB()
        return c
    }
}