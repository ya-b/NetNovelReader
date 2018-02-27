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

    }
}