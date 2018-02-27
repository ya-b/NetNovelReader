package com.netnovelreader.data.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase

@Database(entities = arrayOf(ShelfBean::class, SitePreferenceBean::class), version = 1)
abstract class ReaderDatabase : RoomDatabase() {
    abstract fun shelfDao(): ShelfDao
    abstract fun sitePreferenceDao(): SitePreferenceDao

    companion object {
        const val ID = "_id"

        const val TABLE_SHELF = "shelf"
        //书名
        const val BOOKNAME = "book_name"
        //最新章节
        const val LATESTCHAPTER = "latest_Chapter"
        //阅读记录
        const val READRECORD = "read_Record"
        //来源网址
        const val DOWNLOADURL = "download_url"
        //是否有更新
        const val ISUPDATE = "is_update"
        const val LATESTREAD = "latest_read"

        const val TABLE_SP = "sitepreference"
        //如qidian.com
        const val HOSTNAME = "hostname"
        //目录网址解析规则
        const val CATALOG_SELECTOR = "catalog_selector"
        //章节网址解析规则
        const val CHAPTER_SELECTOR = "chapter_selector"
        const val CATALOG_FILTER = "catalog_filter"
        const val CHAPTER_FILTER = "chapter_filter"
        const val SEARCHURL = "search_url"
        const val REDIRECTFILELD = "redirect_fileld"
        const val REDIRECTURL = "redirect_selector"
        const val NOREDIRECTURL = "no_redirect_selector"
        const val REDIRECTNAME = "redirect_name"
        const val NOREDIRECTNAME = "no_redirect_name"
        const val REDIRECTIMAGE = "redirect_image"
        const val NOREDIRECTIMAGE = "no_redirect_image"
        const val CHARSET = "charset"

        //章节名
        const val CHAPTERNAME = "chaptername"
        //章节来源网址
        const val CHAPTERURL = "chapterurl"
        const val ISDOWNLOADED = "is_downloaded"
        const val SEARCH_NAME = "searchname"
    }
}