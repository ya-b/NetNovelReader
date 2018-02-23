package com.netnovelreader.data.db

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.netnovelreader.ReaderApplication
import com.netnovelreader.common.UPDATEFLAG

/**
 * Created by yangbo on 17-12-24.
 */
object ReaderDbManager {
    var db: SQLiteDatabase? = null
    val dbName = "netnovelreader.db"
    var doTransaction = false

    fun getDB(): SQLiteDatabase {
        db ?: synchronized(ReaderDbManager::class) {
            db ?: kotlin.run {
                db = ReaderSQLHelper(ReaderApplication.appContext, dbName, 1)
                    .writableDatabase
            }
        }
        return db!!
    }

    fun closeDB() {
        synchronized(ReaderDbManager::class) {
            if (!doTransaction) {
                db?.close()
                db = null
            }
        }
    }

    //查询下载的所有书
    fun queryShelfBookList(): HashMap<Int, Array<String>> {
        val hashMap =
            LinkedHashMap<Int, Array<String>>()  //key=id, Array=BOOKNAME,LATESTCHAPTER,DOWNLOADURL,ISUPDATE
        val cursor =
            getDB().rawQuery(
                "select * from ${ReaderSQLHelper.TABLE_SHELF} order by ${ReaderSQLHelper.LATESTREAD} DESC;",
                null
            )
        while (cursor.moveToNext()) {
            val array = arrayOf(
                cursor.getString(cursor.getColumnIndex(ReaderSQLHelper.BOOKNAME)),
                cursor.getString(cursor.getColumnIndex(ReaderSQLHelper.LATESTCHAPTER)),
                cursor.getString(cursor.getColumnIndex(ReaderSQLHelper.DOWNLOADURL)),
                cursor.getString(cursor.getColumnIndex(ReaderSQLHelper.ISUPDATE))
            )
            hashMap.put(cursor.getInt(cursor.getColumnIndex(ReaderSQLHelper.ID)), array)
        }
        cursor.close()
        return hashMap
    }

    //添加书
    fun addBookToShelf(bookname: String, url: String): Int {
        val cursor = getDB().rawQuery(
            "select ${ReaderSQLHelper.ID} from ${ReaderSQLHelper.TABLE_SHELF} where ${ReaderSQLHelper.BOOKNAME}='$bookname';",
            null
        )
        val id = if (cursor.moveToFirst()) {
            cursor.getInt(0)
        } else {
            val contentValue = ContentValues()
            contentValue.put(ReaderSQLHelper.BOOKNAME, bookname)
            contentValue.put(ReaderSQLHelper.DOWNLOADURL, url)
            contentValue.put(ReaderSQLHelper.ISUPDATE, UPDATEFLAG)
            getDB().insert(ReaderSQLHelper.TABLE_SHELF, null, contentValue).toInt()
        }
        cursor.close()
        return id
    }

    //删除书
    fun removeBookFromShelf(bookname: String): Int {
        val cursor = getDB().rawQuery(
            "select ${ReaderSQLHelper.ID} from ${ReaderSQLHelper.TABLE_SHELF} where ${ReaderSQLHelper.BOOKNAME}='$bookname';",
            null
        )
        var id = -1
        if (cursor.moveToFirst()) {
            id = cursor.getInt(0)
            getDB().execSQL("delete from ${ReaderSQLHelper.TABLE_SHELF} where ${ReaderSQLHelper.ID}=$id;")
        }
        cursor.close()
        return id
    }

    fun getBookId(bookname: String): Int {
        var id = 1
        val cursor = getDB().rawQuery(
            "select ${ReaderSQLHelper.ID} from " +
                    "${ReaderSQLHelper.TABLE_SHELF} where ${ReaderSQLHelper.BOOKNAME}='$bookname';",
            null
        )
        if (cursor.moveToFirst()) {
            id = cursor.getInt(0)
        }
        cursor.close()
        return id
    }

    fun getCatalogUrl(bookname: String): String {
        var url = ""
        val cursor = getDB().rawQuery(
            "select ${ReaderSQLHelper.DOWNLOADURL} from ${ReaderSQLHelper.TABLE_SHELF} where ${ReaderSQLHelper.BOOKNAME} = " +
                    "'$bookname';", null
        )
        if (cursor.moveToFirst()) {
            url = cursor.getString(0)
        }
        cursor.close()
        return url
    }

    fun cancelUpdateFlag(bookname: String) {
        getDB().execSQL(
            "update ${ReaderSQLHelper.TABLE_SHELF} set ${ReaderSQLHelper.ISUPDATE}='' where ${ReaderSQLHelper.BOOKNAME}='$bookname';"
        )
    }

    fun setLatestRead(bookname: String) {
        getDB().execSQL(
            "update ${ReaderSQLHelper.TABLE_SHELF} set ${ReaderSQLHelper.LATESTREAD} = " +
                    "ifnull((select max(${ReaderSQLHelper.LATESTREAD}) from ${ReaderSQLHelper.TABLE_SHELF}),0) + 1 " +
                    "where ${ReaderSQLHelper.BOOKNAME}='$bookname';"
        )
        val cursor = getDB().rawQuery(
            "select min(${ReaderSQLHelper.LATESTREAD}) from ${ReaderSQLHelper.TABLE_SHELF}",
            null
        )
        if (cursor.moveToFirst()) {
            val min = cursor.getInt(0)
            if (min > 0) {
                getDB().execSQL(
                    "update ${ReaderSQLHelper.TABLE_SHELF} set ${ReaderSQLHelper.LATESTREAD}=${ReaderSQLHelper.LATESTREAD} - " +
                            "(select min(${ReaderSQLHelper.LATESTREAD}) from ${ReaderSQLHelper.TABLE_SHELF});"
                )
            }
        }
        cursor.close()
    }

    fun setLatestChapter(latestChapter: String?, id: String) {
        latestChapter ?: return
        getDB().execSQL(
            "update ${ReaderSQLHelper.TABLE_SHELF} set ${ReaderSQLHelper.LATESTCHAPTER}='$latestChapter',${ReaderSQLHelper.ISUPDATE}='$UPDATEFLAG' where ${ReaderSQLHelper.ID}=$id;"
        )
    }

    //获取阅读记录 1#3#10 表示id=3,第3章，第10页
    fun getRecord(bookname: String): Array<String> {
        val result = Array(2) { "" }
        val cursor = getDB().rawQuery(
            "select ${ReaderSQLHelper.ID},${ReaderSQLHelper.READRECORD} from ${ReaderSQLHelper.TABLE_SHELF} where " +
                    "${ReaderSQLHelper.BOOKNAME}='$bookname';", null
        )
        if (cursor.moveToFirst()) {
            result[0] = cursor.getString(0) ?: ""
            result[1] = cursor.getString(1) ?: ""
        }
        cursor.close()
        return result
    }

    //设置阅读记录
    fun setRecord(bookname: String, record: String) {
        getDB().execSQL("update ${ReaderSQLHelper.TABLE_SHELF} set ${ReaderSQLHelper.READRECORD}='$record' where ${ReaderSQLHelper.BOOKNAME}='$bookname';")
    }

    //查询所有可以搜索的网站
    fun queryAllSearchSite(): ArrayList<Array<String?>> {
        val arraylist = ArrayList<Array<String?>>()
        val cursor = getDB().rawQuery("select * from ${ReaderSQLHelper.TABLE_SEARCH};", null)
        while (cursor.moveToNext()) {
            arraylist.add(Array(10) { it -> cursor.getString(it + 2) })
        }
        cursor.close()
        return arraylist
    }

    //根据field对应的获取解析规则
    fun getParseRule(hostname: String, field: String): String {
        var rule: String? = null
        val cursor = getDB().rawQuery(
            "select $field from ${ReaderSQLHelper.TABLE_PARSERULES} " +
                    "where ${ReaderSQLHelper.HOSTNAME}='$hostname';", null
        )
        if (cursor!!.moveToFirst()) {
            rule = cursor.getString(0)
        }
        cursor.close()
        return rule ?: ""
    }

    //创建表，添加书的时侯用到，里面保存章节目录
    fun createTable(tableName: String) {
        synchronized(ReaderDbManager::class) {
            getDB().execSQL(
                "create table if not exists $tableName (${ReaderSQLHelper.ID} " +
                        "integer primary key unique,${ReaderSQLHelper.CHAPTERNAME} text unique, " +
                        "${ReaderSQLHelper.CHAPTERURL} text, ${ReaderSQLHelper.ISDOWNLOADED} var char(128));"
            )
        }
    }


    fun dropTable(tableName: String) {
        synchronized(ReaderDbManager::class) {
            getDB().execSQL("drop table if exists $tableName;")
        }
    }

    //设置章节是否下载完成
    fun setChapterFinish(
        tableName: String,
        chaptername: String,
        url: String,
        isDownloadSuccess: Int
    ) {
        val getId =
            "(select ${ReaderSQLHelper.ID} from $tableName where ${ReaderSQLHelper.CHAPTERNAME}='$chaptername')"
        getDB().execSQL(
            "replace into $tableName (${ReaderSQLHelper.ID}, ${ReaderSQLHelper.CHAPTERNAME}, ${ReaderSQLHelper.CHAPTERURL}, ${ReaderSQLHelper.ISDOWNLOADED}) " +
                    "values ($getId, '$chaptername', '$url', '$isDownloadSuccess')"
        )
    }

    /**
     * @isDownloaded  0表示未下载,1表示已下载
     * 获取未下载，或已下载的章节列表，反回map<章节名，该章节url>
     */
    fun getChapterNameAndUrl(tableName: String, isDownloaded: Int): LinkedHashMap<String, String> {
        val map = LinkedHashMap<String, String>()
        val cursor = getDB().rawQuery(
            "select ${ReaderSQLHelper.CHAPTERNAME}," +
                    "${ReaderSQLHelper.CHAPTERURL} from $tableName where ${ReaderSQLHelper.ISDOWNLOADED}=" +
                    "'$isDownloaded';", null
        )
        while (cursor.moveToNext()) {
            map.put(cursor.getString(0), cursor.getString(1))
        }
        cursor.close()
        return map
    }

    //所有章节列表
    fun getAllChapter(tableName: String): ArrayList<String> {
        val arrayList = ArrayList<String>()
        val cursor = getDB().rawQuery(
            "select ${ReaderSQLHelper.CHAPTERNAME} from $tableName order by ${ReaderSQLHelper.ID} asc;",
            null
        )
        while (cursor.moveToNext()) {
            arrayList.add(cursor.getString(0))
        }
        cursor.close()
        return arrayList
    }

    //根据id获取章节名
    fun getChapterName(tableName: String, id: Int): String {
        var chapterName: String? = null
        val cursor = getDB().rawQuery(
            "select ${ReaderSQLHelper.CHAPTERNAME} from $tableName where " +
                    "${ReaderSQLHelper.ID}=$id;", null
        )
        if (cursor.moveToFirst()) {
            chapterName = cursor.getString(0)
        }
        cursor.close()
        return chapterName ?: ""
    }


    fun getChapterUrl(tableName: String, chapterName: String): String {
        var chapterUrl: String? = null
        val cursor = getDB().rawQuery(
            "select ${ReaderSQLHelper.CHAPTERURL} from $tableName where " +
                    "${ReaderSQLHelper.CHAPTERNAME}='$chapterName';", null
        )
        if (cursor.moveToFirst()) {
            chapterUrl = cursor.getString(0)
        }
        cursor.close()
        return chapterUrl ?: ""
    }

    fun getChapterId(tableName: String, chapterName: String): Int {
        var id = 1
        val cursor = getDB().rawQuery(
            "select ${ReaderSQLHelper.ID} from $tableName where " +
                    "${ReaderSQLHelper.CHAPTERNAME}='$chapterName';", null
        )
        if (cursor.moveToFirst()) {
            id = cursor.getInt(0)
        }
        cursor.close()
        return id
    }

    //小于等于id的章节，全部标记为'2'(0为未下载，1为已下载)，并返回更改的章节名列表
    fun setReaded(tableName: String, id: Int): ArrayList<String> {
        val arrayList = ArrayList<String>()
        val cursor = getDB().rawQuery(
            "select ${ReaderSQLHelper.CHAPTERNAME} from $tableName where ${ReaderSQLHelper.ID}<=$id " +
                    "and ${ReaderSQLHelper.ISDOWNLOADED}='1';", null
        )
        while (cursor.moveToNext()) {
            arrayList.add(cursor.getString(0))
        }
        cursor.close()
        getDB().execSQL("update $tableName set ${ReaderSQLHelper.ISDOWNLOADED}='2' where ${ReaderSQLHelper.ID}<=$id;")
        return arrayList
    }

    //删除该章及之后的所有章节，并返回删除的章节名
    fun delChapterAfterSrc(tableName: String, chapterName: String): ArrayList<String> {
        val arrayList = ArrayList<String>()
        val id = getChapterId(tableName, chapterName)
        val cursor =
            getDB().rawQuery(
                "select ${ReaderSQLHelper.CHAPTERNAME} from $tableName where ${ReaderSQLHelper.ID}>=$id;",
                null
            )
        while (cursor.moveToNext()) {
            arrayList.add(cursor.getString(0))
        }
        cursor.close()
        getDB().execSQL("delete from $tableName where ${ReaderSQLHelper.ID}>=$id")
        return arrayList
    }

    /**
     * 获取章节总数
     */
    fun getChapterCount(tableName: String): Int {
        var c = 0
        createTable(tableName)
        val cursor = getDB().rawQuery("select count(*) from $tableName;", null)
        if (cursor.moveToFirst()) {
            c = cursor.getInt(0)
        }
        cursor.close()
        return c
    }

}