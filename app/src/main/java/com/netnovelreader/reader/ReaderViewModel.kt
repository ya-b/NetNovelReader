package com.netnovelreader.reader

import android.databinding.ObservableArrayList
import android.databinding.ObservableField
import com.netnovelreader.common.NotDeleteNum
import com.netnovelreader.common.getSavePath
import com.netnovelreader.common.id2TableName
import com.netnovelreader.common.data.SQLHelper
import com.netnovelreader.common.download.ChapterCache
import java.io.File

/**
 * Created by yangbo on 18-1-13.
 */

class ReaderViewModel(private val bookName: String, private val CACHE_NUM: Int) :
        IReaderContract.IReaderViewModel {
    var catalog = ObservableArrayList<ReaderBean.Catalog>()
    /**
     * 一页显示的内容
     */
    var text: ObservableField<String> = ObservableField("")

    @Volatile
    var dirName: String? = null

    @Volatile
    var chapterName: String? = null
    /**
     * 章节数,最大章节数
     */
    @Volatile
    var chapterNum = 0
    @Volatile
    var maxChapterNum = 0

    private var tableName = ""
    val chapterCache: ChapterCache

    init {
        val cursor = SQLHelper.getDB().rawQuery(
                "select ${SQLHelper.ID} from " +
                        "${SQLHelper.TABLE_SHELF} where ${SQLHelper.BOOKNAME}='$bookName';",
                null
        )
        if (cursor.moveToFirst()) {
            tableName = id2TableName(cursor.getInt(0))
        }
        cursor.close()
        chapterCache = ChapterCache(CACHE_NUM, tableName)
    }

    /**
     * readerView第一次绘制时执行, 返还阅读记录页数，章节名称
     */
    override fun initData(): Int {
        maxChapterNum = SQLHelper.getChapterCount(tableName)
        if (maxChapterNum == 0) {
            return 0
        }
        val array = getRecord()
        chapterNum = array[0]
        chapterCache.prepare(maxChapterNum, dirName!!)
        return array[1]
    }

    /**
     * 获取下一章内容，返回章节名称
     */
    override fun nextChapter(): Boolean {
        if (chapterNum >= maxChapterNum) return false
        val str = chapterCache.getChapter(++chapterNum)
        text.set(str)
        setRecord(chapterNum, 1)
        return str.substring(str.indexOf("|") + 1) == ChapterCache.FILENOTFOUND
    }

    override fun previousChapter(): Boolean {
        if (chapterNum < 2) return false
        val str = chapterCache.getChapter(--chapterNum)
        text.set(str)
        setRecord(chapterNum, 1)
        return str.substring(str.indexOf("|") + 1) == ChapterCache.FILENOTFOUND
    }

    /**
     * 翻页到目录中的某章
     */
    override fun pageByCatalog(chapterName: String?): Boolean {
        chapterName?.run { chapterNum = SQLHelper.getChapterId(tableName, chapterName) }
        val str = chapterCache.getChapter(chapterNum)
        text.set(str)
        setRecord(chapterNum, 1)
        return str.substring(str.indexOf("|") + 1) == ChapterCache.FILENOTFOUND
    }

    /**
     * 保存阅读记录
     */
    @Synchronized
    override fun setRecord(chapterNum: Int, pageNum: Int) {
        if (chapterNum < 1) return
        if (pageNum < 1) {
            SQLHelper.setRecord(bookName, "$chapterNum#1")
        } else {
            SQLHelper.setRecord(bookName, "$chapterNum#$pageNum")
        }
    }

    /**
     * 重新读取目录
     */
    @Synchronized
    override fun updateCatalog(): ObservableArrayList<ReaderBean.Catalog> {
        catalog.clear()
        val catalogCursor = SQLHelper.getDB().rawQuery(
                "select ${SQLHelper.CHAPTERNAME} " +
                        "from $tableName", null
        )
        while (catalogCursor.moveToNext()) {
            catalog.add(ReaderBean.Catalog(catalogCursor.getString(0)))
        }
        catalogCursor.close()
        return catalog
    }

    override fun autoRemove() {
        val num = getRecord()[0]
        if (num < NotDeleteNum) return
        val id = num - NotDeleteNum
        Thread{
            SQLHelper.setReaded(tableName, id)
                    .map { File("${getSavePath()}/$tableName/$it") }
                    .forEach { if (it.exists()) it.delete() }
        }.start()
    }

    /**
     * 获取阅读记录
     */
    private fun getRecord(): IntArray {
        val queryResult = SQLHelper.getRecord(bookName) //阅读记录 3#2 表示第3章第2页
        dirName = id2TableName(queryResult[0])
        var readRecord = queryResult[1]
        if (readRecord.length < 1) {
            readRecord = "1#1"
        }
        val array = readRecord.split("#")
        return IntArray(2) { i -> array[i].toInt() }
    }
}