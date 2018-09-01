package com.netnovelreader.repo.db

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.netnovelreader.utils.ioThread

@Database(entities = [BookInfoEntity::class, SiteSelectorEntity::class, ChapterInfoEntity::class], version = 1)
abstract class ReaderDatabase : RoomDatabase() {
    abstract fun bookInfoDao(): BookInfoDao
    abstract fun siteSelectorDao(): SiteSelectorDao
    abstract fun chapterInfoDao(): ChapterInfoDao

    class DbCallback(var context: Context) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            ioThread {
                getInstance(context).siteSelectorDao().insert(
                    SiteSelectorEntity(
                        1,
                        "qidian.com",
                        ".volume-wrap",
                        ".read-content",
                        "分卷阅读|订阅本卷",
                        "",
                        "https://www.qidian.com/search/?kw=%s",
                        "",
                        "",
                        ".book-img-text > ul:nth-child(1) > li:nth-child(1) > div:nth-child(2) > h4:nth-child(1) > a:nth-child(1)",
                        "",
                        ".book-img-text > ul:nth-child(1) > li:nth-child(1) > div:nth-child(2) > h4:nth-child(1) > a:nth-child(1)",
                        "",
                        ".book-img-text > ul:nth-child(1) > li:nth-child(1) > div:nth-child(1) > a:nth-child(1) > img:nth-child(1)",
                        "utf-8"
                    ),
                    SiteSelectorEntity(
                        2,
                        "yunlaige.com",
                        "#contenttable",
                        "#content",
                        "",
                        "",
                        "http://www.yunlaige.com/modules/article/search.php?searchkey=searchname&action=login&submit=",
                        "location",
                        ".readnow",
                        "li.clearfix:nth-child(1) > div:nth-child(2) > div:nth-child(1) > h2:nth-child(2) > a:nth-child(1)",
                        "#content > div.book-info > div.info > h2 > a",
                        "li.clearfix:nth-child(1) > div:nth-child(2) > div:nth-child(1) > h2:nth-child(1) > a:nth-child(1)",
                        "",
                        "",
                        "gbk"
                    ),
                    SiteSelectorEntity(
                        3,
                        "yssm.org",
                        ".chapterlist",
                        "#content",
                        "",
                        "",
                        "http://zhannei.baidu.com/cse/search?s=7295900583126281660&q=searchname",
                        "",
                        "",
                        "div.result-item:nth-child(1) > div:nth-child(2) > h3:nth-child(1) > a:nth-child(1)",
                        "",
                        "div.result-item:nth-child(1) > div:nth-child(2) > h3:nth-child(1) > a:nth-child(1)",
                        "",
                        "div.result-item:nth-child(1) > div:nth-child(1) > a:nth-child(1) > img:nth-child(1)",
                        "utf-8"
                    ),
                    SiteSelectorEntity(
                        4,
                        "b5200.net",
                        "#list > dl:nth-child(1)",
                        "#content",
                        "",
                        "",
                        "http://www.b5200.net/modules/article/search.php?searchkey=searchname",
                        "",
                        "",
                        ".grid > tbody:nth-child(2) > tr:nth-child(2) > td:nth-child(1) > a:nth-child(1)",
                        "",
                        ".grid > tbody:nth-child(2) > tr:nth-child(2) > td:nth-child(1) > a:nth-child(1)",
                        "",
                        "",
                        "utf-8"
                    ),
                    SiteSelectorEntity(
                        4,
                        "b5200.net",
                        "#list > dl:nth-child(1)",
                        "#content",
                        "",
                        "",
                        "http://www.b5200.net/modules/article/search.php?searchkey=searchname",
                        "",
                        "",
                        ".grid > tbody:nth-child(2) > tr:nth-child(2) > td:nth-child(1) > a:nth-child(1)",
                        "",
                        ".grid > tbody:nth-child(2) > tr:nth-child(2) > td:nth-child(1) > a:nth-child(1)",
                        "",
                        "",
                        "utf-8"
                    )
                )
            }
        }
    }

    companion object {
        private var instance: ReaderDatabase? = null
        fun getInstance(context: Context): ReaderDatabase =
                instance ?: synchronized(ReaderDatabase::class) {
                    instance ?: Room.databaseBuilder(context, ReaderDatabase::class.java, "reader.db")
                                    .addCallback(DbCallback(context))
                                    .build()
                                    .also { instance = it }
                }


        const val ID = "_id"

        /////////////////////////////////////////////////////////////////////////////////
        const val TABLE_SHELF = "shelf"
        //书名
        const val BOOKNAME = "bookname"
        //最后一章
        const val LATEST_CHAPTER = "latest_Chapter"
        //阅读记录
        const val READ_RECORD = "read_Record"
        //目录网址
        const val DOWNLOAD_URL = "download_url"
        //是否有更新
        const val HAS_UPDATE = "has_update"
        //最新阅读章节
        const val ORDER_NUM = "order_number"
        //封面图片路径
        const val COVER_PATH = "cover_path"
        /////////////////////////////////////////////////////////////////////////////////
        const val TABLE_SS = "site_selector"
        //如qidian.com
        const val HOSTNAME = "hostname"
        //目录网址解析规则
        const val CATALOG_SELECTOR = "catalog_selector"
        //章节网址解析规则
        const val CHAPTER_SELECTOR = "chapter_selector"
        const val CATALOG_FILTER = "catalog_filter"
        const val CHAPTER_FILTER = "chapter_filter"
        const val SEARCH_URL = "search_url"
        const val REDIRECT_FILELD = "redirect_fileld"
        const val REDIRECT_URL = "redirect_selector"
        const val NO_REDIRECT_URL = "no_redirect_selector"
        const val REDIRECT_NAME = "redirect_name"
        const val NO_REDIRECT_NAME = "no_redirect_name"
        const val REDIRECT_IMAGE = "redirect_image"
        const val NO_REDIRECT_IMAGE = "no_redirect_image"
        const val CHARSET = "charset"

        /////////////////////////////////////////////////////////////////////////////////
        //目录
        const val TABLE_CATALOG = "catalog"
        //章节序号
        const val CHAPTER_NUM = "chapter_num"
        //章节名
        const val CHAPTER_NAME = "chapter_name"
        //章节来源网址
        const val CHAPTER_URL = "chapter_url"
        //是否已下载
        const val IS_DOWNLOADED = "is_downloaded"

        const val ALREADY_DOWN = 1
        const val NOT_DOWN = 2
    }
}