package com.netnovelreader.data.database

import android.database.Cursor

/**
 * Created by yangbo on 18-1-14.
 */
class SearchSQLManager : BaseSQLManager() {
    companion object {
        val SEARCH_NAME = "searchname"
    }
    init {
        getDB().execSQL("create table if not exists $TABLE_SEARCH (" +
                "$ID integer primary key, " +
                "$SEARCH_HOSTNAME varchar(128) unique, " +
                "$ISREDIRECT varchar(128), " +
                "$SEARCHURL indicator, " +
                "$REDIRECTFILELD varchar(128), " +
                "$REDIRECTSELECTOR varchar(128), " +
                "$NOREDIRECTSELECTOR varchar(128), " +
                "$REDIRECTNAME varchar(128), " +
                "$NOREDIRECTNAME varchar(128), " +
                "$SEARCHCHARSET varchar(128));")
        initTable()
    }

    fun queryAll(): ArrayList<Array<String?>> {
        var arraylist = ArrayList<Array<String?>>()
        var cursor = getDB().rawQuery("select * from $TABLE_SEARCH;", null)
        while (cursor.moveToNext()){
            arraylist.add(Array<String?>(8){it -> cursor.getString(it + 2 ) })
        }
        cursor.close()
        closeDB()
        return arraylist
    }

    fun querySite(hostname: String): Map<String, String?>{
        var cursor = getDB().rawQuery("select * from $TABLE_SEARCH where " +
                "${SEARCH_HOSTNAME}='$hostname';", null)
        var map = HashMap<String, String?>()
        cursor ?: return map
        if(cursor.moveToFirst()){
            map.put(ISREDIRECT, cursor.getString(cursor.getColumnIndex(ISREDIRECT)))
            map.put(SEARCHURL, cursor.getString(cursor.getColumnIndex(SEARCHURL)))
            map.put(REDIRECTFILELD, cursor.getString(cursor.getColumnIndex(REDIRECTFILELD)))
            map.put(REDIRECTSELECTOR, cursor.getString(cursor.getColumnIndex(REDIRECTSELECTOR)))
            map.put(NOREDIRECTSELECTOR, cursor.getString(cursor.getColumnIndex(NOREDIRECTSELECTOR)))
            map.put(REDIRECTNAME, cursor.getString(cursor.getColumnIndex(REDIRECTNAME)))
            map.put(NOREDIRECTNAME, cursor.getString(cursor.getColumnIndex(NOREDIRECTNAME)))
            map.put(SEARCHCHARSET, cursor.getString(cursor.getColumnIndex(SEARCHCHARSET)))
        }
        cursor.close()
        return map
    }

    fun initTable(){
        var cursor = getDB().rawQuery("select * from $TABLE_SEARCH;", null)
        if(cursor != null && !cursor.moveToFirst()){
            getDB().execSQL("insert into $TABLE_SEARCH values (1,'qidian.com','0'," +
                    "'https://www.qidian.com/search/?kw=$SEARCH_NAME','',''," +
                    "'.book-img-text > ul:nth-child(1) > li:nth-child(1) > div:nth-child(2) > h4:nth-child(1) > a:nth-child(1)',''," +
                    "'.book-img-text > ul:nth-child(1) > li:nth-child(1) > div:nth-child(2) > h4:nth-child(1) > a:nth-child(1)'," +
                    "'utf-8');")
            getDB().execSQL("insert into $TABLE_SEARCH values (2,'yunlaige.com','1'," +
                    "'http://www.yunlaige.com/modules/article/search.php?searchkey=$SEARCH_NAME&action=login&submit='," +
                    "'location','.readnow'," +
                    "'li.clearfix:nth-child(1) > div:nth-child(2) > div:nth-child(1) > h2:nth-child(2) > a:nth-child(1)'," +
                    "'#content > div.book-info > div.info > h2 > a'," +
                    "'li.clearfix:nth-child(1) > div:nth-child(2) > div:nth-child(1) > h2:nth-child(1) > a:nth-child(1)'," +
                    "'gbk');")
        }
        cursor?.close()
    }
}