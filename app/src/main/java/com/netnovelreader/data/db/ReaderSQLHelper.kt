package com.netnovelreader.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ReaderSQLHelper(val context: Context, val name: String, val version: Int) :
    SQLiteOpenHelper(context, name, null, version) {

    companion object {

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
        val CHAPTERURL = "chapterurl"
        val ISDOWNLOADED = "is_downloaded"
        val SEARCH_NAME = "searchname"

    }

    override fun onCreate(db: SQLiteDatabase?) {
        try {
            db?.beginTransaction()
            initTable(db)
            db?.setTransactionSuccessful()
        } finally {
            db?.endTransaction()
        }
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
            "create table if not exists $TABLE_SEARCH ($ID integer primary key, " +
                    "$SEARCH_HOSTNAME varchar(128) unique, $ISREDIRECT varchar(128), $SEARCHURL text, " +
                    "$REDIRECTFILELD varchar(128), $REDIRECTSELECTOR varchar(128), $NOREDIRECTSELECTOR " +
                    "varchar(128), $REDIRECTNAME varchar(128), $NOREDIRECTNAME varchar(128), " +
                    "$SEARCHCHARSET varchar(128),$REDIRECTIMAGE text,$NOREDIRECTIMAGE text);"
        )
        db?.execSQL(
            "create table if not exists $TABLE_SHELF ($ID integer primary key, " +
                    "$BOOKNAME varchar(128) unique, $READRECORD varchar(128), $DOWNLOADURL text, " +
                    "$LATESTCHAPTER varchar(128), $ISUPDATE varchar(128), $LATESTREAD integer);"
        )
        db?.execSQL(
            "insert into $TABLE_PARSERULES values " +
                    "(1,'qidian.com','.volume-wrap','.read-content','分卷阅读|订阅本卷',NULL)," +
                    "(2,'yunlaige.com','#contenttable','#content',NULL,NULL)," +
                    "(3,'yssm.org','.chapterlist','#content',NULL,NULL)," +
                    "(4,'b5200.net','#list > dl:nth-child(1)','#content',NULL,NULL)," +
                    "(5,'shudaizi.org','#list > dl:nth-child(1)','#content',NULL,NULL)," +
                    "(6,'81xsw.com','#list > dl:nth-child(1)','#content',NULL,NULL)," +
                    "(7,'sqsxs.com','#list > dl:nth-child(1)','#content',NULL,NULL);"
        )
        db?.execSQL(
            "insert into $TABLE_SEARCH values (1,'qidian.com','0'," +
                    "'https://www.qidian.com/search/?kw=$SEARCH_NAME','',''," +
                    "'.book-img-text > ul:nth-child(1) > li:nth-child(1) > div:nth-child(2) > h4:nth-child(1) > a:nth-child(1)',''," +
                    "'.book-img-text > ul:nth-child(1) > li:nth-child(1) > div:nth-child(2) > h4:nth-child(1) > a:nth-child(1)'," +
                    "'utf-8','','.book-img-text > ul:nth-child(1) > li:nth-child(1) > div:nth-child(1) > a:nth-child(1) > img:nth-child(1)')," +
                    "(2,'yunlaige.com','1'," +
                    "'http://www.yunlaige.com/modules/article/search.php?searchkey=$SEARCH_NAME&action=login&submit='," +
                    "'location','.readnow'," +
                    "'li.clearfix:nth-child(1) > div:nth-child(2) > div:nth-child(1) > h2:nth-child(2) > a:nth-child(1)'," +
                    "'#content > div.book-info > div.info > h2 > a'," +
                    "'li.clearfix:nth-child(1) > div:nth-child(2) > div:nth-child(1) > h2:nth-child(1) > a:nth-child(1)'," +
                    "'gbk','','')," +
                    "(3,'yssm.org','0'," +
                    "'http://zhannei.baidu.com/cse/search?s=7295900583126281660&q=$SEARCH_NAME'," +
                    "'',''," +
                    "'div.result-item:nth-child(1) > div:nth-child(2) > h3:nth-child(1) > a:nth-child(1)'," +
                    "''," +
                    "'div.result-item:nth-child(1) > div:nth-child(2) > h3:nth-child(1) > a:nth-child(1)'," +
                    "'utf-8','','div.result-item:nth-child(1) > div:nth-child(1) > a:nth-child(1) > img:nth-child(1)')," +
                    "(4,'b5200.net','0'," +
                    "'http://www.b5200.net/modules/article/search.php?searchkey=$SEARCH_NAME'," +
                    "'',''," +
                    "'.grid > tbody:nth-child(2) > tr:nth-child(2) > td:nth-child(1) > a:nth-child(1)'," +
                    "''," +
                    "'.grid > tbody:nth-child(2) > tr:nth-child(2) > td:nth-child(1) > a:nth-child(1)'," +
                    "'utf-8','','')," +
                    "(5,'shudaizi.org','0'," +
                    "'http://zhannei.baidu.com/cse/search?q=$SEARCH_NAME&click=1&entry=1&s=16961354726626188066&nsid='," +
                    "'',''," +
                    "'div.result-item:nth-child(1) > div:nth-child(2) > h3:nth-child(1) > a:nth-child(1)'," +
                    "''," +
                    "'div.result-item:nth-child(1) > div:nth-child(2) > h3:nth-child(1) > a:nth-child(1)'," +
                    "'utf-8','','div.result-item:nth-child(1) > div:nth-child(1) > a:nth-child(1) > img:nth-child(1)')," +
                    "(6,'81xsw.com','0'," +
                    "'http://zhannei.baidu.com/cse/search?s=16095493717575840686&q=$SEARCH_NAME'," +
                    "'',''," +
                    "'div.result-item:nth-child(1) > div:nth-child(2) > h3:nth-child(1) > a:nth-child(1)'," +
                    "''," +
                    "'div.result-item:nth-child(1) > div:nth-child(2) > h3:nth-child(1) > a:nth-child(1)'," +
                    "'utf-8','','div.result-item:nth-child(1) > div:nth-child(1) > a:nth-child(1) > img:nth-child(1)')," +
                    "(7,'sqsxs.com','0'," +
                    "'https://www.sqsxs.com/modules/article/search.php?searchkey=$SEARCH_NAME'," +
                    "'',''," +
                    "'.grid > tbody:nth-child(2) > tr:nth-child(2) > td:nth-child(1) > a:nth-child(1)'," +
                    "''," +
                    "'.grid > tbody:nth-child(2) > tr:nth-child(2) > td:nth-child(1) > a:nth-child(1)'," +
                    "'gbk','','');"
        )
    }
}