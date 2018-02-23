package com.netnovelreader.data.db

import android.database.Cursor
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 数据库空表情况下测试
 */
@RunWith(AndroidJUnit4::class)
class ReaderDbManagerTest {
    val bookname = "helloworld"
    val catalogurl = "http://hello.world/index.html"
    val records = "3#10"

    @Before
    fun initDatabase() {
        ReaderDbManager.db = ReaderSQLHelper(
            InstrumentationRegistry.getTargetContext(),
            ReaderDbManager.dbName, 1
        ).writableDatabase
        val id = ReaderDbManager.addBookToShelf(bookname, catalogurl)
        ReaderDbManager.createTable("BOOK$id")
        ReaderDbManager.setChapterFinish(
            "BOOK$id", "chapter1",
            "http://hello.world/1.html", 0
        )
        ReaderDbManager.setRecord(bookname, records)
    }

    @After
    fun closeDatabase() {
        ReaderDbManager.dropTable("BOOK1")
        ReaderDbManager.getDB().execSQL("delete from ${ReaderSQLHelper.TABLE_SHELF}")
        ReaderDbManager.db?.close()
    }

    @Test
    fun getDB() {
        val db = ReaderDbManager.getDB()
        assertThat(db.isOpen, `is`(true))
    }

    @Test
    fun queryShelfBookList() {
        val map = ReaderDbManager.queryShelfBookList()
        assertThat(map[1]!![0], `is`("helloworld"))
    }

    @Test
    fun addBookToShelf() {
        var cursor: Cursor? = null
        try {
            val id = ReaderDbManager.addBookToShelf("nihao", "http://ni.hao/index.html")
            cursor = ReaderDbManager.getDB().rawQuery(
                "select ${ReaderSQLHelper.BOOKNAME} from " +
                        "shelf where ${ReaderSQLHelper.ID}=$id;", null
            )
            cursor.moveToFirst()
            assertThat(cursor.getString(0), `is`("nihao"))
        } finally {
            cursor?.close()
        }


        val id = ReaderDbManager.removeBookFromShelf("nihao")
        assertThat(id > 0, `is`(true))
    }

    @Test
    fun getBookId() {
        val bookid = ReaderDbManager.getBookId("helloworld")
        assertThat(bookid, `is`(1))
    }

    @Test
    fun getCatalogUrl() {
        val url = ReaderDbManager.getCatalogUrl(bookname)
        assertThat(url, `is`(catalogurl))
    }

    @Test
    fun cancelUpdateFlag() {
    }

    @Test
    fun setLatestRead() {
        ReaderDbManager.setLatestRead(bookname)
        val cursor = ReaderDbManager.getDB().rawQuery(
            "select ${ReaderSQLHelper.LATESTREAD} " +
                    "from ${ReaderSQLHelper.TABLE_SHELF} where ${ReaderSQLHelper.BOOKNAME}='$bookname'",
            null
        )
        cursor.moveToFirst()
        assertThat(cursor.getInt(0), `is`(0))
        cursor.close()
    }

    @Test
    fun setLatestChapter() {
        ReaderDbManager.setLatestChapter("latestchapter", "1")
        val cursor = ReaderDbManager.getDB().rawQuery(
            "select ${ReaderSQLHelper.LATESTCHAPTER} " +
                    "from shelf where ${ReaderSQLHelper.ID}=1;", null
        )
        cursor.moveToFirst()
        cursor.close()
    }

    @Test
    fun getRecord() {
        val array = ReaderDbManager.getRecord(bookname)
        assertThat("${array[0]}#${array[1]}", `is`("1#$records"))
    }

    @Test
    fun queryAllSearchSite() {
        ReaderDbManager.queryAllSearchSite()
        assertThat(ReaderDbManager.queryAllSearchSite().size > 0, `is`(true))
    }

    @Test
    fun getParseRule() {
        val boo = ReaderDbManager.getParseRule("qidian.com", ReaderSQLHelper.CATALOG_RULE).isEmpty()
        assertThat(boo, `is`(false))
    }

    @Test
    fun createTable() {
        ReaderDbManager.createTable("BOOK234")
        val cursor = ReaderDbManager.getDB().rawQuery(
            "SELECT COUNT(*) FROM sqlite_master where type='table' " +
                    "and name='BOOK234'", null
        )
        cursor.moveToFirst()
        assertThat(cursor.getInt(0), `is`(1))
        cursor.close()
    }

    @Test
    fun dropTable() {
        ReaderDbManager.dropTable("BOOK234")
        val cursor = ReaderDbManager.getDB().rawQuery(
            "SELECT COUNT(*) FROM sqlite_master where type='table' " +
                    "and name='BOOK234'", null
        )
        cursor.moveToFirst()
        assertThat(cursor.getInt(0), `is`(0))
        cursor.close()
    }

    @Test
    fun setChapterFinish() {
        ReaderDbManager.setChapterFinish(
            "BOOK1", "chapter1",
            "http://hello.world/1.html", 1
        )
        val cursor = ReaderDbManager.getDB().rawQuery(
            "SELECT ${ReaderSQLHelper.ISDOWNLOADED} FROM " +
                    "BOOK1 where ${ReaderSQLHelper.CHAPTERNAME}='chapter1';", null
        )
        cursor.moveToFirst()
        assertThat(cursor.getInt(0), `is`(1))
        cursor.close()
    }

    @Test
    fun getChapterNameAndUrl() {
        val map = ReaderDbManager.getChapterNameAndUrl("BOOK1", 0)
        assertThat(map.size, `is`(1))
    }

    @Test
    fun getAllChapter() {
        val list = ReaderDbManager.getAllChapter("BOOK1")
        assertThat(list.size, `is`(1))
    }

    @Test
    fun getChapterName() {
        assertThat(ReaderDbManager.getChapterName("BOOK1", 1), `is`("chapter1"))
    }

    @Test
    fun getChapterUrl() {
        assertThat(
            ReaderDbManager.getChapterUrl("BOOK1", "chapter1"),
            `is`("http://hello.world/1.html")
        )
    }

    @Test
    fun getChapterId() {
        assertThat(ReaderDbManager.getChapterId("BOOK1", "chapter1"), `is`(1))
    }

    @Test
    fun setReaded() {
        ReaderDbManager.setReaded("BOOK1", 1)
        val cursor = ReaderDbManager.getDB().rawQuery(
            "SELECT ${ReaderSQLHelper.ISDOWNLOADED} FROM " +
                    "BOOK1 where ${ReaderSQLHelper.CHAPTERNAME}='chapter1';", null
        )
        cursor.moveToFirst()
        assertThat(cursor.getInt(0), `is`(2))
        cursor.close()
    }

//    @Test
//    fun delChapterAfterSrc() {
//        val count = ReaderDbManager.getChapterCount("BOOK1")
//        assertThat(count, `is`(1))
//
//        ReaderDbManager.delChapterAfterSrc("BOOK1","chapter1")
//    }

    @Test
    fun getChapterCount() {
        val count = ReaderDbManager.getChapterCount("BOOK1")
        assertThat(count, `is`(1))
    }
}