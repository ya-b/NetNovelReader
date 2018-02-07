package com.netnovelreader.common.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.netnovelreader.ReaderApplication
import com.netnovelreader.common.UPDATEFLAG

/**
 * Created by yangbo on 17-12-24.
 */
object SQLHelper {
    private var db: SQLiteDatabase? = null
    private val dbName = "netnovelreader.db"

    fun getDB(): SQLiteDatabase {
        db ?: synchronized(SQLHelper) {
            db ?: kotlin.run {
                db = NovelSQLHelper(ReaderApplication.appContext, dbName, 1).writableDatabase
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

    //查询下载的所有书
    fun queryShelfBookList(): HashMap<Int, Array<String>> {
        val hashMap =
                LinkedHashMap<Int, Array<String>>()  //key=id, Array=BOOKNAME,LATESTCHAPTER,DOWNLOADURL,ISUPDATE
        val cursor =
                getDB().rawQuery("select * from $TABLE_SHELF order by $LATESTREAD DESC;", null)
        while (cursor.moveToNext()) {
            val array = arrayOf(
                    cursor.getString(cursor.getColumnIndex(SQLHelper.BOOKNAME)),
                    cursor.getString(cursor.getColumnIndex(SQLHelper.LATESTCHAPTER)),
                    cursor.getString(cursor.getColumnIndex(SQLHelper.DOWNLOADURL)),
                    cursor.getString(cursor.getColumnIndex(SQLHelper.ISUPDATE))
            )
            hashMap.put(cursor.getInt(cursor.getColumnIndex(SQLHelper.ID)), array)
        }
        cursor.close()
        return hashMap
    }

    //添加书
    fun addBookToShelf(bookname: String, url: String): Int {
        synchronized(SQLHelper) {
            var id = 0
            val cursor = getDB().rawQuery(
                    "select $ID from $TABLE_SHELF where $BOOKNAME='$bookname';",
                    null
            )
            if (cursor.moveToFirst()) {
                id = cursor.getInt(0)
            } else {
                val contentValue = ContentValues()
                contentValue.put(BOOKNAME, bookname)
                contentValue.put(DOWNLOADURL, url)
                contentValue.put(ISUPDATE, UPDATEFLAG)
                id = getDB().insert(TABLE_SHELF, null, contentValue).toInt()
            }
            cursor.close()
            return id
        }
    }

    //删除书
    fun removeBookFromShelf(bookname: String): Int {
        synchronized(SQLHelper) {
            val cursor = getDB().rawQuery(
                    "select $ID from $TABLE_SHELF where $BOOKNAME='$bookname';",
                    null
            )
            var id = -1
            if (cursor.moveToFirst()) {
                id = cursor.getInt(0)
                getDB().execSQL("delete from $TABLE_SHELF where $ID=$id;")
            }
            cursor.close()
            return id
        }
    }

    fun getBookId(bookname: String): Int {
        var id = 1
        val cursor = SQLHelper.getDB().rawQuery(
                "select ${SQLHelper.ID} from " +
                        "${SQLHelper.TABLE_SHELF} where ${SQLHelper.BOOKNAME}='$bookname';", null
        )
        if (cursor.moveToFirst()) {
            id = cursor.getInt(0)
        }
        cursor.close()
        return id
    }

    fun cancelUpdateFlag(bookname: String) {
        getDB().execSQL(
                "update ${SQLHelper.TABLE_SHELF} set ${SQLHelper.ISUPDATE}='' where ${SQLHelper.BOOKNAME}='$bookname';"
        )
    }

    fun setLatestRead(bookname: String) {
        var max = 0L
        val cursor = getDB().rawQuery("select max($LATESTREAD) from $TABLE_SHELF", null)
        if (cursor.moveToFirst()) {
            max = cursor.getLong(0)
        }
        cursor.close()
        getDB().execSQL("update $TABLE_SHELF set $LATESTREAD=${max + 1} where $BOOKNAME='$bookname';")
    }

    //获取阅读记录
    fun getRecord(bookname: String): Array<String> {
        val result = Array(2) { "" }
        val cursor = getDB().rawQuery(
                "select $ID,$READRECORD from $TABLE_SHELF where " +
                        "$BOOKNAME='$bookname';", null
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
        getDB().execSQL("update $TABLE_SHELF set $READRECORD='$record' where $BOOKNAME='$bookname';")
    }

    //查询所有可以搜索的网站
    fun queryAllSearchSite(): ArrayList<Array<String?>> {
        val arraylist = ArrayList<Array<String?>>()
        val cursor = getDB().rawQuery("select * from $TABLE_SEARCH;", null)
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
                "select $field from $TABLE_PARSERULES " +
                        "where $HOSTNAME='$hostname';", null
        )
        if (cursor!!.moveToFirst()) {
            rule = cursor.getString(0)
        }
        cursor.close()
        return rule ?: ""
    }

    //创建表，添加书的时侯用到，里面保存章节目录
    fun createTable(tableName: String) {
        synchronized(SQLHelper) {
            getDB().execSQL(
                    "create table if not exists $tableName ($ID " +
                            "integer primary key unique,$CHAPTERNAME text unique, " +
                            "$CHAPTERURL text, $ISDOWNLOADED var char(128));"
            )
        }
    }


    fun dropTable(tableName: String) {
        synchronized(SQLHelper) {
            getDB().execSQL("drop table if exists $tableName;")
        }
    }
    //设置章节是否下载完成
    fun setChapterFinish(
            tableName: String,
            chaptername: String,
            url: String,
            isDownloadSuccess: Boolean
    ) {
        val getId = "(select $ID from $tableName where $CHAPTERNAME='$chaptername')"
        getDB().execSQL("replace into $tableName ($ID, $CHAPTERNAME, $CHAPTERURL, $ISDOWNLOADED) " +
                "values ($getId, '$chaptername', '$url', '$isDownloadSuccess')")
    }

    /**
     * @isDownloaded  0表示未下载,1表示已下载
     * 获取未下载，或已下载的章节列表，反回map<章节名，该章节url>
     */
    fun getChapterNameAndUrl(tableName: String, isDownloaded: Int): LinkedHashMap<String, String> {
        val map = LinkedHashMap<String, String>()
        val cursor = getDB().rawQuery(
                "select $CHAPTERNAME," +
                        "$CHAPTERURL from $tableName where $ISDOWNLOADED=" +
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
        val cursor = getDB().rawQuery("select $CHAPTERNAME from $tableName;", null)
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
                "select $CHAPTERNAME from $tableName where " +
                        "$ID=$id;", null
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
                "select $CHAPTERURL from $tableName where " +
                        "$CHAPTERNAME='$chapterName';", null
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
                "select $ID from $tableName where " +
                        "$CHAPTERNAME='$chapterName';", null
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
                "select $CHAPTERNAME from $tableName where $ID<=$id " +
                        "and $ISDOWNLOADED='1';", null
        )
        while (cursor.moveToNext()) {
            arrayList.add(cursor.getString(0))
        }
        cursor.close()
        getDB().execSQL("update $tableName set $ISDOWNLOADED='2' where $ID<=$id;")
        return arrayList
    }

    //删除该章及之后的所有章节，并返回删除的章节名
    fun delChapterAfterSrc(tableName: String, chapterName: String): ArrayList<String> {
        val arrayList = ArrayList<String>()
        val id = getChapterId(tableName, chapterName)
        val cursor =
                getDB().rawQuery("select $CHAPTERNAME from $tableName where $ID>=$id;", null)
        while (cursor.moveToNext()) {
            arrayList.add(cursor.getString(0))
        }
        cursor.close()
        getDB().execSQL("delete from $tableName where $ID>=$id")
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

    val ID = "_id"

    val TABLE_PARSERULES = "parserules"
    //如qidian.com
    val HOSTNAME = "hostname"
    //目录网址解析规则
    val CATALOG_RULE = "catalog_rule"
    //章节网址解析规则
    val CHAPTER_RULE = "chapter_rule"
    val CATALOG_FILTER = "cover_filter"
    val CHAPTER_FILTER = "chapter_filter"

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
    //是否有更新
    val ISUPDATE = "isupdate"
    val LATESTREAD = "latest_read"

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

    class NovelSQLHelper(val context: Context, val name: String, val version: Int) :
            SQLiteOpenHelper(context, name, null, version) {

        override fun onCreate(db: SQLiteDatabase?) {
            initTable(db)
        }

        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

        }

        private fun initTable(db: SQLiteDatabase?) {
            db?.execSQL(
                    "create table if not exists $TABLE_PARSERULES ($ID integer primary key," +
                            "$HOSTNAME varchar(128) unique,$CATALOG_RULE text,$CHAPTER_RULE text," +
                            "$CATALOG_FILTER varchar(128),$CHAPTER_FILTER text);"
            )
            db?.execSQL(
                    "insert into $TABLE_PARSERULES values (1,'qidian.com','#volumes'," +
                            "'.read-section','分卷阅读|订阅本卷',NULL);"
            )
            db?.execSQL(
                    "insert into $TABLE_PARSERULES values (2,'yunlaige.com','#contenttable'," +
                            "'#content',NULL,NULL);"
            )
            db?.execSQL(
                    "insert into $TABLE_PARSERULES values (3,'yssm.org','.chapterlist'," +
                            "'#content',NULL,NULL);"
            )
            db?.execSQL(
                    "insert into $TABLE_PARSERULES values (4,'b5200.net','#list > dl:nth-child(1)'," +
                            "'#content',NULL,NULL);"
            )
            db?.execSQL(
                    "insert into $TABLE_PARSERULES values (5,'shudaizi.org','#list > dl:nth-child(1)'," +
                            "'#content',NULL,NULL);"
            )
            db?.execSQL(
                    "insert into $TABLE_PARSERULES values (6,'81xsw.com','#list > dl:nth-child(1)'," +
                            "'#content',NULL,NULL);"
            )
            db?.execSQL(
                    "insert into $TABLE_PARSERULES values (7,'sqsxs.com','#list > dl:nth-child(1)'," +
                            "'#content',NULL,NULL);"
            )
            db?.execSQL(
                    "create table if not exists $TABLE_SEARCH ($ID integer primary key, " +
                            "$SEARCH_HOSTNAME varchar(128) unique, $ISREDIRECT varchar(128), $SEARCHURL text, " +
                            "$REDIRECTFILELD varchar(128), ${REDIRECTSELECTOR} varchar(128), $NOREDIRECTSELECTOR " +
                            "varchar(128), $REDIRECTNAME varchar(128), $NOREDIRECTNAME varchar(128), " +
                            "$SEARCHCHARSET varchar(128),$REDIRECTIMAGE text,$NOREDIRECTIMAGE text);"
            )
            db?.execSQL(
                    "insert into $TABLE_SEARCH values (1,'qidian.com','0'," +
                            "'https://www.qidian.com/search/?kw=$SEARCH_NAME','',''," +
                            "'.book-img-text > ul:nth-child(1) > li:nth-child(1) > div:nth-child(2) > h4:nth-child(1) > a:nth-child(1)',''," +
                            "'.book-img-text > ul:nth-child(1) > li:nth-child(1) > div:nth-child(2) > h4:nth-child(1) > a:nth-child(1)'," +
                            "'utf-8','','.book-img-text > ul:nth-child(1) > li:nth-child(1) > div:nth-child(1) > a:nth-child(1) > img:nth-child(1)');"
            )
            db?.execSQL(
                    "insert into $TABLE_SEARCH values (2,'yunlaige.com','1'," +
                            "'http://www.yunlaige.com/modules/article/search.php?searchkey=$SEARCH_NAME&action=login&submit='," +
                            "'location','.readnow'," +
                            "'li.clearfix:nth-child(1) > div:nth-child(2) > div:nth-child(1) > h2:nth-child(2) > a:nth-child(1)'," +
                            "'#content > div.book-info > div.info > h2 > a'," +
                            "'li.clearfix:nth-child(1) > div:nth-child(2) > div:nth-child(1) > h2:nth-child(1) > a:nth-child(1)'," +
                            "'gbk','','');"
            )
            db?.execSQL(
                    "insert into $TABLE_SEARCH values (3,'yssm.org','0'," +
                            "'http://zhannei.baidu.com/cse/search?s=7295900583126281660&q=$SEARCH_NAME'," +
                            "'',''," +
                            "'div.result-item:nth-child(1) > div:nth-child(2) > h3:nth-child(1) > a:nth-child(1)'," +
                            "''," +
                            "'div.result-item:nth-child(1) > div:nth-child(2) > h3:nth-child(1) > a:nth-child(1)'," +
                            "'utf-8','','div.result-item:nth-child(1) > div:nth-child(1) > a:nth-child(1) > img:nth-child(1)');"
            )
            db?.execSQL(
                    "insert into $TABLE_SEARCH values (4,'b5200.net','0'," +
                            "'http://www.b5200.net/modules/article/search.php?searchkey=$SEARCH_NAME'," +
                            "'',''," +
                            "'.grid > tbody:nth-child(2) > tr:nth-child(2) > td:nth-child(1) > a:nth-child(1)'," +
                            "''," +
                            "'.grid > tbody:nth-child(2) > tr:nth-child(2) > td:nth-child(1) > a:nth-child(1)'," +
                            "'utf-8','','');"
            )
            db?.execSQL(
                    "insert into $TABLE_SEARCH values (5,'shudaizi.org','0'," +
                            "'http://zhannei.baidu.com/cse/search?q=$SEARCH_NAME&click=1&entry=1&s=16961354726626188066&nsid='," +
                            "'',''," +
                            "'div.result-item:nth-child(1) > div:nth-child(2) > h3:nth-child(1) > a:nth-child(1)'," +
                            "''," +
                            "'div.result-item:nth-child(1) > div:nth-child(2) > h3:nth-child(1) > a:nth-child(1)'," +
                            "'utf-8','','div.result-item:nth-child(1) > div:nth-child(1) > a:nth-child(1) > img:nth-child(1)');"
            )
            db?.execSQL(
                    "insert into $TABLE_SEARCH values (6,'81xsw.com','0'," +
                            "'http://zhannei.baidu.com/cse/search?s=16095493717575840686&q=$SEARCH_NAME'," +
                            "'',''," +
                            "'div.result-item:nth-child(1) > div:nth-child(2) > h3:nth-child(1) > a:nth-child(1)'," +
                            "''," +
                            "'div.result-item:nth-child(1) > div:nth-child(2) > h3:nth-child(1) > a:nth-child(1)'," +
                            "'utf-8','','div.result-item:nth-child(1) > div:nth-child(1) > a:nth-child(1) > img:nth-child(1)');"
            )
            db?.execSQL(
                    "insert into $TABLE_SEARCH values (7,'sqsxs.com','0'," +
                            "'https://www.sqsxs.com/modules/article/search.php?searchkey=$SEARCH_NAME'," +
                            "'',''," +
                            "'.grid > tbody:nth-child(2) > tr:nth-child(2) > td:nth-child(1) > a:nth-child(1)'," +
                            "''," +
                            "'.grid > tbody:nth-child(2) > tr:nth-child(2) > td:nth-child(1) > a:nth-child(1)'," +
                            "'gbk','','');"
            )
            db?.execSQL(
                    "create table if not exists $TABLE_SHELF ($ID integer primary key, " +
                            "$BOOKNAME varchar(128) unique, $READRECORD varchar(128), $DOWNLOADURL text, " +
                            "$LATESTCHAPTER varchar(128), $ISUPDATE varchar(128), $LATESTREAD integer);"
            )
        }
    }
}