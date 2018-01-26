package com.netnovelreader.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.netnovelreader.ReaderApplication

/**
 * Created by yangbo on 17-12-24.
 */
object SQLHelper {
    private var db: SQLiteDatabase? = null
    private val dbName = "netnovelreader.db"

    fun getDB(): SQLiteDatabase {
        db ?: synchronized(SQLHelper) {
            db ?: kotlin.run {
                db = NovelSQLHelper(ReaderApplication.appContext!!, dbName, 1).writableDatabase
            }
        }
        return db!!
    }

    fun closeDB() {
        synchronized(SQLHelper) {
            db?.close()
            db = null
        }
    }


    fun queryShelfBookList(): Cursor? {
        synchronized(SQLHelper) {
            return getDB().rawQuery("select * from $TABLE_SHELF;", null)
        }
    }

    fun addBookToShelf(bookname: String, url: String): Int {
        synchronized(SQLHelper) {
            var id = 0
            val cursor = getDB().rawQuery("select $ID from $TABLE_SHELF where $BOOKNAME='$bookname';",
                    null)
            if (cursor.moveToFirst()) {
                id = cursor.getInt(0)
            } else {
                val contentValue = ContentValues()
                contentValue.put(BOOKNAME, bookname)
                contentValue.put(DOWNLOADURL, url)
                id = getDB().insert(TABLE_SHELF, null, contentValue).toInt()
            }
            cursor.close()
            return id
        }
    }

    fun removeBookFromShelf(bookname: String): Int {
        synchronized(SQLHelper) {
            val cursor = getDB().rawQuery("select $ID from $TABLE_SHELF where $BOOKNAME='$bookname';",
                    null)
            var id = -1
            if (cursor.moveToFirst()) {
                id = cursor.getInt(0)
                getDB().execSQL("delete from $TABLE_SHELF where $ID=$id;")
            }
            cursor.close()
            return id
        }
    }

    fun getRecord(bookname: String): Array<String> {
        synchronized(SQLHelper) {
            val result = Array(2) { "" }
            val cursor = getDB().rawQuery("select $ID,$READRECORD from $TABLE_SHELF where " +
                    "$BOOKNAME='$bookname';", null)
            if (cursor.moveToFirst()) {
                result[0] = cursor.getString(0) ?: ""
                result[1] = cursor.getString(1) ?: ""
            }
            cursor.close()
            return result
        }
    }

    fun setRecord(bookname: String, record: String) {
        synchronized(SQLHelper) {
            getDB().execSQL("update $TABLE_SHELF set $READRECORD='$record' where " +
                    "$BOOKNAME='$bookname';")
        }
    }

    fun queryAllSearchSite(): ArrayList<Array<String?>> {
        synchronized(SQLHelper) {
            val arraylist = ArrayList<Array<String?>>()
            val cursor = getDB().rawQuery("select * from $TABLE_SEARCH;", null)
            while (cursor.moveToNext()) {
                arraylist.add(Array(10) { it -> cursor.getString(it + 2) })
            }
            cursor.close()
            return arraylist
        }
    }

    fun getParseRule(hostname: String, field: String): String? {
        synchronized(SQLHelper) {
            var rule: String? = null
            val cursor = getDB().rawQuery("select $field from $TABLE_PARSERULES " +
                    "where $HOSTNAME='$hostname';", null)
            if (cursor!!.moveToFirst()) {
                rule = cursor.getString(0)
            }
            cursor.close()
            return rule
        }
    }


    fun createTable(tableName: String) {
        synchronized(SQLHelper) {
            getDB().execSQL("create table if not exists $tableName ($ID " +
                    "integer primary key,$CHAPTERNAME varchar(128), " +
                    "$CHAPTERURL indicator, $ISDOWNLOADED var char(128));")
        }
    }


    fun dropTable(tableName: String) {
        synchronized(SQLHelper) {
            getDB().execSQL("drop table if exists $tableName;")
        }
    }

    fun setChapterFinish(tableName: String, chaptername: String, url: String, isDownloadSuccess: Boolean) {
        synchronized(SQLHelper) {
            val cursor = getDB().rawQuery("select * from $tableName where " +
                    "$CHAPTERNAME='$chaptername';", null)
            if (!cursor.moveToFirst()) {
                getDB().execSQL("insert into $tableName ($CHAPTERNAME, " +
                        "$CHAPTERURL, $ISDOWNLOADED) values ('$chaptername'," +
                        "'$url','${compareValues(isDownloadSuccess, false)}')")
            } else {
                getDB().execSQL("update $tableName set $ISDOWNLOADED=" +
                        "'${compareValues(isDownloadSuccess, false)}' " +
                        "where $CHAPTERNAME='$chaptername';")
            }
            cursor.close()
        }
    }

    /**
     * @isDownloaded  0表示未下载,1表示已下载
     */
    fun getDownloadedOrNot(tableName: String, isDownloaded: Int): LinkedHashMap<String, String> {
        synchronized(SQLHelper) {
            val map = LinkedHashMap<String, String>()
            val cursor = getDB().rawQuery("select $CHAPTERNAME," +
                    "$CHAPTERURL from $tableName where $ISDOWNLOADED=" +
                    "'$isDownloaded';", null)
            while (cursor.moveToNext()) {
                map.put(cursor.getString(0), cursor.getString(1))
            }
            cursor.close()
            return map
        }
    }

    fun getAllChapter(tableName: String): ArrayList<String> {
        synchronized(SQLHelper) {
            val arrayList = ArrayList<String>()
            val cursor = getDB().rawQuery("select $CHAPTERNAME from $tableName;", null)
            while (cursor.moveToNext()) {
                arrayList.add(cursor.getString(0))
            }
            cursor.close()
            return arrayList
        }
    }

    fun getChapterName(tableName: String, id: Int): String {
        synchronized(SQLHelper) {
            var chapterName: String? = null
            val cursor = getDB().rawQuery("select $CHAPTERNAME from $tableName where " +
                    "$ID=$id;", null)
            if (cursor.moveToFirst()) {
                chapterName = cursor.getString(0)
            }
            cursor.close()
            return chapterName ?: ""
        }
    }

    fun getChapterUrl(tableName: String, chapterName: String): String {
        synchronized(SQLHelper) {
            var chapterUrl: String? = null
            val cursor = getDB().rawQuery("select $CHAPTERURL from $tableName where " +
                    "$CHAPTERNAME='$chapterName';", null)
            if (cursor.moveToFirst()) {
                chapterUrl = cursor.getString(0)
            }
            cursor.close()
            return chapterUrl ?: ""
        }
    }

    fun getChapterId(tableName: String, chapterName: String): Int {
        synchronized(SQLHelper) {
            var id = 1
            val cursor = getDB().rawQuery("select $ID from $tableName where " +
                    "$CHAPTERNAME='$chapterName';", null)
            if (cursor.moveToFirst()) {
                id = cursor.getInt(0)
            }
            cursor.close()
            return id
        }
    }

    /**
     * 获取章节总数
     */
    fun getChapterCount(tableName: String): Int {
        synchronized(SQLHelper) {
            var c = -1
            val cursor = getDB().rawQuery("select count(*) from $tableName;", null)
            if (cursor.moveToFirst()) {
                c = cursor.getInt(0)
            }
            cursor.close()
            return c
        }
    }

    val ID = "_id"

    val TABLE_PARSERULES = "parserules"
    //如qidian.com
    val HOSTNAME = "hostname"
    //目录网址解析规则
    val CATALOG_RULE = "catalog_rule"
    //章节网址解析规则
    val CHAPTER_RULE = "chapter_rule"
    //目录网址封面解析规则
    val COVER_RULE = "cover_rule"

    val TABLE_SHELF = "shelf"
    //书名
    val BOOKNAME = "tablename"
    //最新章节
    val LATESTCHAPTER = "latestChapter"
    //阅读记录
    val READRECORD = "readRecord"
    //编码
    val CHARSET = "charset"
    //来源网址
    val DOWNLOADURL = "downloadurl"

    val TABLE_SEARCH = "search"
    val SEARCH_HOSTNAME = HOSTNAME
    val SEARCHURL = "search_url"
    val ISREDIRECT = "is_redirect"
    val REDIRECTFILELD = "redirect_fileld"
    val REDIRECTSELECTOR = "redirect_selector"
    val NOREDIRECTSELECTOR = "no_redirect_selector"
    val REDIRECTNAME = "redirect_name"
    val NOREDIRECTNAME = "no_redirect_name"
    val SEARCHCHARSET = CHARSET
    val REDIRECTIMAGE = "redirect_image"
    val NOREDIRECTIMAGE = "no_redirect_image"

    //章节名
    val CHAPTERNAME = "chaptername"
    //章节来源网址
    private val CHAPTERURL = "chapterurl"
    private val ISDOWNLOADED = "is_downloaded"
    val SEARCH_NAME = "searchname"

    class NovelSQLHelper(val context: Context, val name: String, val version: Int)
        : SQLiteOpenHelper(context, name, null, version) {

        override fun onCreate(db: SQLiteDatabase?) {
            initTable(db)
        }

        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        private fun initTable(db: SQLiteDatabase?) {
            db?.execSQL("create table if not exists $TABLE_PARSERULES ($ID integer primary key," +
                    "$HOSTNAME varchar(128) unique,$CATALOG_RULE text,$CHAPTER_RULE text," +
                    "$CHARSET varchar(128),$COVER_RULE text);")
            db?.execSQL("insert into $TABLE_PARSERULES values (1,'qidian.com','.volume-wrap'," +
                    "'.read-content','utf-8',NULL);")
            db?.execSQL("insert into $TABLE_PARSERULES values (2,'yunlaige.com','#contenttable'," +
                    "'#content','gbk',NULL);")
            db?.execSQL("create table if not exists $TABLE_SEARCH ($ID integer primary key, " +
                    "$SEARCH_HOSTNAME varchar(128) unique, $ISREDIRECT varchar(128), $SEARCHURL text, " +
                    "$REDIRECTFILELD varchar(128), ${REDIRECTSELECTOR} varchar(128), $NOREDIRECTSELECTOR " +
                    "varchar(128), $REDIRECTNAME varchar(128), $NOREDIRECTNAME varchar(128), " +
                    "$SEARCHCHARSET varchar(128),$REDIRECTIMAGE text,$NOREDIRECTIMAGE text);")
            db?.execSQL("insert into $TABLE_SEARCH values (1,'qidian.com','0'," +
                    "'https://www.qidian.com/search/?kw=$SEARCH_NAME','',''," +
                    "'.book-img-text > ul:nth-child(1) > li:nth-child(1) > div:nth-child(2) > h4:nth-child(1) > a:nth-child(1)',''," +
                    "'.book-img-text > ul:nth-child(1) > li:nth-child(1) > div:nth-child(2) > h4:nth-child(1) > a:nth-child(1)'," +
                    "'utf-8','','.book-img-text > ul:nth-child(1) > li:nth-child(1) > div:nth-child(1) > a:nth-child(1) > img:nth-child(1)');")
            db?.execSQL("insert into $TABLE_SEARCH values (2,'yunlaige.com','1'," +
                    "'http://www.yunlaige.com/modules/article/search.php?searchkey=$SEARCH_NAME&action=login&submit='," +
                    "'location','.readnow'," +
                    "'li.clearfix:nth-child(1) > div:nth-child(2) > div:nth-child(1) > h2:nth-child(2) > a:nth-child(1)'," +
                    "'#content > div.book-info > div.info > h2 > a'," +
                    "'li.clearfix:nth-child(1) > div:nth-child(2) > div:nth-child(1) > h2:nth-child(1) > a:nth-child(1)'," +
                    "'gbk','','');")
            db?.execSQL("create table if not exists $TABLE_SHELF ($ID integer primary key, " +
                    "$BOOKNAME varchar(128) unique, $READRECORD varchar(128), $DOWNLOADURL text, " +
                    "$LATESTCHAPTER varchar(128));")
        }
    }
}