package com.netnovelreader.data.database

import android.database.sqlite.SQLiteDatabase

/**
 * Created by yangbo on 18-1-12.
 */
class ParseSQLManager : BaseSQLManager {
    constructor() {
        db = super.getDataBase()
        db?.execSQL("create table if not exists ${BaseSQLManager.TABLE_PARSERULES} (${BaseSQLManager.ID} integer primary key," +
                "${BaseSQLManager.HOSTNAME} varchar(128) unique,${BaseSQLManager.CATALOG_RULE} text,${BaseSQLManager.CHAPTER_RULE} text," +
                "${BaseSQLManager.CHARSET} varchar(128),${BaseSQLManager.COVER_RULE} text);")
        var cursor = db?.rawQuery("select * from ${BaseSQLManager.TABLE_PARSERULES}", null)
        if(cursor != null && !cursor.moveToFirst()){
            db?.execSQL("insert into ${BaseSQLManager.TABLE_PARSERULES} values (1,'qidian.com','.volume-wrap','.read-content','utf-8',NULL);")
            db?.execSQL("insert into ${BaseSQLManager.TABLE_PARSERULES} values (2,'yunlaige.com','#contenttable','#content','gbk',NULL);")
        }
        cursor?.close()
    }
}