package com.netnovelreader.data.database

/**
 * Created by yangbo on 18-1-12.
 */
class ChapterSQLManager : BaseSQLManager() {
    fun createTable(bookid : Int) {
        db!!.execSQL("create table if not exists $bookid (${ID} integer primary key," +
                "${CHAPTERNAME} varchar(128) unique, ${CHAPTERURL} text);")
    }
}