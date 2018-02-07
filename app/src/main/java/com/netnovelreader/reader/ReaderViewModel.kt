package com.netnovelreader.reader

import android.databinding.ObservableArrayList
import android.databinding.ObservableField
import com.netnovelreader.common.NotDeleteNum
import com.netnovelreader.common.data.SQLHelper
import com.netnovelreader.common.download.ChapterCache
import com.netnovelreader.common.getSavePath
import com.netnovelreader.common.id2TableName
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
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
    var chapterNum = 1
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
    override suspend fun initData(): Int = async{
        maxChapterNum = SQLHelper.getChapterCount(tableName)
        if (maxChapterNum == 0) {
            return@async 0
        }
        val array = getRecord()
        chapterNum = array[0]
        chapterCache.init(maxChapterNum, dirName!!)
        array[1]
    }.await()

    /**
     * 获取下一章内容，返回章节名称
     */
    override suspend fun nextChapter(): Boolean = async{
        if (chapterNum >= maxChapterNum) return@async false
        setRecord(chapterNum, 1)
        chapterCache.getChapter(++chapterNum)
            .apply { text.set(this) }
            .let { it.substring(it.indexOf("|") + 1) } == ChapterCache.FILENOTFOUND
    }.await()

    override suspend fun previousChapter(): Boolean = async {
        if (chapterNum < 2) return@async false
        setRecord(chapterNum, 1)
        chapterCache.getChapter(--chapterNum)
            .apply { text.set(this) }
            .let { it.substring(it.indexOf("|") + 1) } == ChapterCache.FILENOTFOUND
    }.await()

    /**
     * 翻页到目录中的某章
     */
    override suspend fun pageByCatalog(chapterName: String?): Boolean = async {
        chapterName?.run {
            chapterNum = SQLHelper.getChapterId(tableName, chapterName)
            setRecord(chapterNum, 1)
        }
        chapterCache.getChapter(chapterNum)
            .apply { text.set(this) }
            .let { it.substring(it.indexOf("|") + 1) } == ChapterCache.FILENOTFOUND
    }.await()

    override suspend fun downloadChapter(chapterName: String?): Boolean = async {
        var str = ChapterCache.FILENOTFOUND
        var times = 0
        while (str == ChapterCache.FILENOTFOUND && times++ < 10) {
            str = chapterCache.getFromNet(
                getSavePath() + "/" + dirName!!,
                chapterName ?: ""
            )
            delay(500)
        }
        !(str == ChapterCache.FILENOTFOUND || str.isEmpty()) && !(pageByCatalog(null))
    }.await()

    /**
     * 保存阅读记录
     */
    @Synchronized
    override suspend fun setRecord(chapterNum: Int, pageNum: Int) {
        if (chapterNum < 1) return
        SQLHelper.setRecord(bookName, "$chapterNum#${if (pageNum < 1) 1 else pageNum}")
    }

    /**
     * 重新读取目录
     */
    @Synchronized
    override suspend fun updateCatalog(): ObservableArrayList<ReaderBean.Catalog> = async {
        catalog.clear()
        val catalogCursor = SQLHelper.getDB().rawQuery(
            "select ${SQLHelper.CHAPTERNAME} " + "from $tableName", null
        )
        while (catalogCursor.moveToNext()) {
            catalog.add(ReaderBean.Catalog(catalogCursor.getString(0)))
        }
        catalogCursor.close()
        catalog
    }.await()

    override suspend fun autoRemove() {
        val num = getRecord()[0]
        if (num < NotDeleteNum) return
        val id = num - NotDeleteNum
        SQLHelper.setReaded(tableName, id)
                .forEach { File("${getSavePath()}/$tableName/$it").delete() }
    }

    /**
     * 获取阅读记录
     */
    private suspend fun getRecord(): IntArray {
        val queryResult = SQLHelper.getRecord(bookName) //阅读记录 3#2 表示第3章第2页
        dirName = id2TableName(queryResult[0])
        val array = queryResult[1]
            .let { if (it.length < 1) "1#1" else it }
            .split("#")
        return IntArray(2) { i -> array[i].toInt() }
    }
}