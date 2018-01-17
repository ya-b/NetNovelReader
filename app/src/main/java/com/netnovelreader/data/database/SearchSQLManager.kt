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
        getDB().execSQL("create table if not exists ${BaseSQLManager.TABLE_SEARCH} (" +
                "${BaseSQLManager.ID} integer primary key, " +
                "${BaseSQLManager.SEARCH_HOSTNAME} varchar(128) unique, " +
                "${BaseSQLManager.ISREDIRECT} varchar(128), " +
                "${BaseSQLManager.SEARCHURL} text, " +
                "${BaseSQLManager.REDIRECTFILELD} varchar(128), " +
                "${BaseSQLManager.REDIRECTSELECTOR} varchar(128), " +
                "${BaseSQLManager.NOREDIRECTSELECTOR} varchar(128), " +
                "${BaseSQLManager.REDIRECTNAME} varchar(128), " +
                "${BaseSQLManager.NOREDIRECTNAME} varchar(128), " +
                "${BaseSQLManager.SEARCHCHARSET} varchar(128));")
        initTable()
    }

    fun queryAll(): Cursor? {
        return getDB().rawQuery("select * from ${BaseSQLManager.TABLE_SEARCH}", null)
    }

    fun querySite(hostname: String): Map<String, String?>{
        var cursor = getDB().rawQuery("select * from ${BaseSQLManager.TABLE_SEARCH} where " +
                "${BaseSQLManager.SEARCH_HOSTNAME}='$hostname';", null)
        var map = HashMap<String, String?>()
        cursor ?: return map
        if(cursor.moveToFirst()){
            map.put(BaseSQLManager.ISREDIRECT, cursor.getString(cursor.getColumnIndex(BaseSQLManager.ISREDIRECT)))
            map.put(BaseSQLManager.SEARCHURL, cursor.getString(cursor.getColumnIndex(BaseSQLManager.SEARCHURL)))
            map.put(BaseSQLManager.REDIRECTFILELD, cursor.getString(cursor.getColumnIndex(BaseSQLManager.REDIRECTFILELD)))
            map.put(BaseSQLManager.REDIRECTSELECTOR, cursor.getString(cursor.getColumnIndex(BaseSQLManager.REDIRECTSELECTOR)))
            map.put(BaseSQLManager.NOREDIRECTSELECTOR, cursor.getString(cursor.getColumnIndex(BaseSQLManager.NOREDIRECTSELECTOR)))
            map.put(BaseSQLManager.REDIRECTNAME, cursor.getString(cursor.getColumnIndex(BaseSQLManager.REDIRECTNAME)))
            map.put(BaseSQLManager.NOREDIRECTNAME, cursor.getString(cursor.getColumnIndex(BaseSQLManager.NOREDIRECTNAME)))
            map.put(BaseSQLManager.SEARCHCHARSET, cursor.getString(cursor.getColumnIndex(BaseSQLManager.SEARCHCHARSET)))
        }
        cursor.close()
        return map
    }

    fun initTable(){
        var cursor = queryAll()
        if(cursor != null && !cursor.moveToFirst()){
            getDB().execSQL("insert into ${BaseSQLManager.TABLE_SEARCH} values (1,'qidian.com','0','',''," +
                    "'.book-img-text > ul:nth-child(1) > li:nth-child(1)',''," +
                    "'.book-img-text > ul:nth-child(1) > li:nth-child(1) > div:nth-child(2) > h4:nth-child(1) > a:nth-child(1) > cite'," +
                    "'utf-8');")
            getDB().execSQL("insert into ${BaseSQLManager.TABLE_SEARCH} values (2,'yunlaige.com','1'," +
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