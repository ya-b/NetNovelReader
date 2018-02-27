package com.netnovelreader.data.db

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import com.netnovelreader.ReaderApplication

/**
 * Created by yangbo on 17-12-24.
 */
object ReaderDbManager {
    private var roomDb: ReaderDatabase? = null
    val dbName = "netnovelreader.db"

    fun getRoomDB(): ReaderDatabase {
        roomDb ?: synchronized(ReaderDbManager::class) {
            roomDb ?: kotlin.run {
                roomDb = Room.databaseBuilder(ReaderApplication.appContext, ReaderDatabase::class.java, dbName)
                        .addCallback(dbCallBack).build()
            }
        }
        return roomDb!!
    }

    //创建表，添加书的时侯用到，里面保存章节目录
    fun createTable(tableName: String) {
        synchronized(ReaderDbManager::class) {
            getRoomDB().openHelper.writableDatabase.execSQL(
                    "create table if not exists $tableName (${ReaderDatabase.ID} " +
                            "integer primary key unique,${ReaderDatabase.CHAPTERNAME} text unique, " +
                            "${ReaderDatabase.CHAPTERURL} text, ${ReaderDatabase.ISDOWNLOADED} var char(128));"
            )
        }
    }


    fun dropTable(tableName: String) {
        synchronized(ReaderDbManager::class) {
            getRoomDB().openHelper.writableDatabase.execSQL("drop table if exists $tableName;")
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
                "(select ${ReaderDatabase.ID} from $tableName where ${ReaderDatabase.CHAPTERNAME}='$chaptername')"
        getRoomDB().openHelper.writableDatabase.execSQL(
                "replace into $tableName (${ReaderDatabase.ID}, ${ReaderDatabase.CHAPTERNAME}, ${ReaderDatabase.CHAPTERURL}, ${ReaderDatabase.ISDOWNLOADED}) " +
                        "values ($getId, '$chaptername', '$url', '$isDownloadSuccess')"
        )
    }

    /**
     * @isDownloaded  0表示未下载,1表示已下载
     * 获取未下载，或已下载的章节列表，反回map<章节名，该章节url>
     */
    fun getChapterNameAndUrl(tableName: String, isDownloaded: Int): LinkedHashMap<String, String> {
        val map = LinkedHashMap<String, String>()
        val cursor = getRoomDB().openHelper.writableDatabase.query(
                "select ${ReaderDatabase.CHAPTERNAME}," +
                        "${ReaderDatabase.CHAPTERURL} from $tableName where ${ReaderDatabase.ISDOWNLOADED}=" +
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
        val cursor = getRoomDB().openHelper.writableDatabase.query(
                "select ${ReaderDatabase.CHAPTERNAME} from $tableName order by ${ReaderDatabase.ID} asc;",
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
        val cursor = getRoomDB().openHelper.writableDatabase.query(
                "select ${ReaderDatabase.CHAPTERNAME} from $tableName where " +
                        "${ReaderDatabase.ID}=$id;", null
        )
        if (cursor.moveToFirst()) {
            chapterName = cursor.getString(0)
        }
        cursor.close()
        return chapterName ?: ""
    }


    fun getChapterUrl(tableName: String, chapterName: String): String {
        var chapterUrl: String? = null
        val cursor = getRoomDB().openHelper.writableDatabase.query(
                "select ${ReaderDatabase.CHAPTERURL} from $tableName where " +
                        "${ReaderDatabase.CHAPTERNAME}='$chapterName';", null
        )
        if (cursor.moveToFirst()) {
            chapterUrl = cursor.getString(0)
        }
        cursor.close()
        return chapterUrl ?: ""
    }

    fun getChapterId(tableName: String, chapterName: String): Int {
        var id = 1
        val cursor = getRoomDB().openHelper.writableDatabase.query(
                "select ${ReaderDatabase.ID} from $tableName where " +
                        "${ReaderDatabase.CHAPTERNAME}='$chapterName';", null
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
        val cursor = getRoomDB().openHelper.writableDatabase.query(
                "select ${ReaderDatabase.CHAPTERNAME} from $tableName where ${ReaderDatabase.ID}<=$id " +
                        "and ${ReaderDatabase.ISDOWNLOADED}='1';", null
        )
        while (cursor.moveToNext()) {
            arrayList.add(cursor.getString(0))
        }
        cursor.close()
        getRoomDB().openHelper.writableDatabase.execSQL("update $tableName set ${ReaderDatabase.ISDOWNLOADED}='2' where ${ReaderDatabase.ID}<=$id;")
        return arrayList
    }

    //删除该章及之后的所有章节，并返回删除的章节名
    fun delChapterAfterSrc(tableName: String, chapterName: String): ArrayList<String> {
        val arrayList = ArrayList<String>()
        val id = getChapterId(tableName, chapterName)
        val cursor =
                getRoomDB().openHelper.writableDatabase.query(
                        "select ${ReaderDatabase.CHAPTERNAME} from $tableName where ${ReaderDatabase.ID}>=$id;",
                        null
                )
        while (cursor.moveToNext()) {
            arrayList.add(cursor.getString(0))
        }
        cursor.close()
        getRoomDB().openHelper.writableDatabase.execSQL("delete from $tableName where ${ReaderDatabase.ID}>=$id")
        return arrayList
    }

    /**
     * 获取章节总数
     */
    fun getChapterCount(tableName: String): Int {
        var c = 0
        createTable(tableName)
        val cursor = getRoomDB().openHelper.writableDatabase.query("select count(*) from $tableName;", null)
        if (cursor.moveToFirst()) {
            c = cursor.getInt(0)
        }
        cursor.close()
        return c
    }

    val dbCallBack = object : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            val array = arrayOf(
                    "(1,'qidian.com','.volume-wrap','.read-content','分卷阅读|订阅本卷',''," +
                            "'https://www.qidian.com/search/?kw=${ReaderDatabase.SEARCH_NAME}','',''," +
                            "'.book-img-text > ul:nth-child(1) > li:nth-child(1) > div:nth-child(2) > h4:nth-child(1) > a:nth-child(1)',''," +
                            "'.book-img-text > ul:nth-child(1) > li:nth-child(1) > div:nth-child(2) > h4:nth-child(1) > a:nth-child(1)'," +
                            "'','.book-img-text > ul:nth-child(1) > li:nth-child(1) > div:nth-child(1) > a:nth-child(1) > img:nth-child(1)','utf-8')",
                    "(2,'yunlaige.com','#contenttable','#content','',''," +
                            "'http://www.yunlaige.com/modules/article/search.php?searchkey=${ReaderDatabase.SEARCH_NAME}&action=login&submit='," +
                            "'location','.readnow'," +
                            "'li.clearfix:nth-child(1) > div:nth-child(2) > div:nth-child(1) > h2:nth-child(2) > a:nth-child(1)'," +
                            "'#content > div.book-info > div.info > h2 > a'," +
                            "'li.clearfix:nth-child(1) > div:nth-child(2) > div:nth-child(1) > h2:nth-child(1) > a:nth-child(1)'," +
                            "'','','gbk')",
                    "(3,'yssm.org','.chapterlist','#content','',''," +
                            "'http://zhannei.baidu.com/cse/search?s=7295900583126281660&q=${ReaderDatabase.SEARCH_NAME}'," +
                            "'',''," +
                            "'div.result-item:nth-child(1) > div:nth-child(2) > h3:nth-child(1) > a:nth-child(1)'," +
                            "''," +
                            "'div.result-item:nth-child(1) > div:nth-child(2) > h3:nth-child(1) > a:nth-child(1)'," +
                            "'','div.result-item:nth-child(1) > div:nth-child(1) > a:nth-child(1) > img:nth-child(1)','utf-8')",
                    "(4,'b5200.net','#list > dl:nth-child(1)','#content','',''," +
                            "'http://www.b5200.net/modules/article/search.php?searchkey=${ReaderDatabase.SEARCH_NAME}'," +
                            "'',''," +
                            "'.grid > tbody:nth-child(2) > tr:nth-child(2) > td:nth-child(1) > a:nth-child(1)'," +
                            "''," +
                            "'.grid > tbody:nth-child(2) > tr:nth-child(2) > td:nth-child(1) > a:nth-child(1)'," +
                            "'','','utf-8')",
                    "(5,'shudaizi.org','#list > dl:nth-child(1)','#content','',''," +
                            "'http://zhannei.baidu.com/cse/search?q=${ReaderDatabase.SEARCH_NAME}&click=1&entry=1&s=16961354726626188066&nsid='," +
                            "'',''," +
                            "'div.result-item:nth-child(1) > div:nth-child(2) > h3:nth-child(1) > a:nth-child(1)'," +
                            "''," +
                            "'div.result-item:nth-child(1) > div:nth-child(2) > h3:nth-child(1) > a:nth-child(1)'," +
                            "'','div.result-item:nth-child(1) > div:nth-child(1) > a:nth-child(1) > img:nth-child(1)','utf-8')",
                    "(6,'81xsw.com','#list > dl:nth-child(1)','#content','',''," +
                            "'http://zhannei.baidu.com/cse/search?s=16095493717575840686&q=${ReaderDatabase.SEARCH_NAME}'," +
                            "'',''," +
                            "'div.result-item:nth-child(1) > div:nth-child(2) > h3:nth-child(1) > a:nth-child(1)'," +
                            "''," +
                            "'div.result-item:nth-child(1) > div:nth-child(2) > h3:nth-child(1) > a:nth-child(1)'," +
                            "'','div.result-item:nth-child(1) > div:nth-child(1) > a:nth-child(1) > img:nth-child(1)','utf-8')",
                    "(7,'sqsxs.com','#list > dl:nth-child(1)','#content','',''," +
                            "'https://www.sqsxs.com/modules/article/search.php?searchkey=${ReaderDatabase.SEARCH_NAME}'," +
                            "'',''," +
                            "'.grid > tbody:nth-child(2) > tr:nth-child(2) > td:nth-child(1) > a:nth-child(1)'," +
                            "''," +
                            "'.grid > tbody:nth-child(2) > tr:nth-child(2) > td:nth-child(1) > a:nth-child(1)'," +
                            "'','','gbk')"
            )
            db.beginTransaction()
            array.forEach {
                db.execSQL("insert into sitepreference values $it;")
            }
            db.setTransactionSuccessful()
            db.endTransaction()
        }
    }
}