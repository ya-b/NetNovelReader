package com.netnovelreader.reader

import android.databinding.ObservableArrayList
import android.databinding.ObservableField
import com.netnovelreader.common.NotDeleteNum
import com.netnovelreader.common.data.SQLHelper
import com.netnovelreader.common.download.ChapterCache
import com.netnovelreader.common.download.DownloadCatalog
import com.netnovelreader.common.getSavePath
import com.netnovelreader.common.id2TableName
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.io.File

/**
 * Created by yangbo on 18-1-13.
 */

class ReaderViewModel(private val bookName: String, private val CACHE_NUM: Int) :
    IReaderContract.IReaderViewModel {

    enum class CHAPTERCHANGE {
        NEXT,                       //下一章
        PREVIOUS,                   //上一章
        BY_CATALOG                  //通过目录翻页
    }

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
    lateinit var chapterCache: ChapterCache

    /**
     * readerView第一次绘制时执行, 返还阅读记录页数
     */
    override suspend fun initData(): Int = async {
        tableName = id2TableName(SQLHelper.getBookId(bookName))
        maxChapterNum = SQLHelper.getChapterCount(tableName).takeIf { it != 0 } ?: return@async 0
        getRecord()
            .apply {
                chapterNum = this[0]
                chapterCache =
                        ChapterCache(CACHE_NUM, tableName).apply { init(maxChapterNum, dirName!!) }
            }
            .let { it[1] }
    }.await()

    //获取章节内容
    override suspend fun getChapter(type: CHAPTERCHANGE, chapterName: String?): Deferred<Boolean> =
        async {
        when (type) {
            CHAPTERCHANGE.NEXT -> if (chapterNum >= maxChapterNum) return@async false else chapterNum++
            CHAPTERCHANGE.PREVIOUS -> if (chapterNum < 2) return@async false else chapterNum--
            CHAPTERCHANGE.BY_CATALOG -> chapterName?.run {
                chapterNum = SQLHelper.getChapterId(tableName, chapterName)
            }
        }
            launch { if (chapterNum == maxChapterNum) updateCatalog() }
        val str = chapterCache.getChapter(chapterNum)
        text.set(str)
        this@ReaderViewModel.chapterName = str.substring(0, str.indexOf("|"))
        str.substring(str.indexOf("|") + 1) == ChapterCache.FILENOTFOUND
        }

    //下载并显示，阅读到未下载章节时调用
    override suspend fun downloadAndShow(): Deferred<Boolean> = async {
        var str = ChapterCache.FILENOTFOUND
        var times = 0
        while (str == ChapterCache.FILENOTFOUND && times++ < 10) {
            str = chapterCache.getFromNet(
                getSavePath() + "/" + dirName!!,
                chapterName ?: SQLHelper.getChapterName(tableName, chapterNum)
            )
            delay(500)
        }
        str != ChapterCache.FILENOTFOUND && str.isNotEmpty()

    }

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
    override suspend fun getCatalog() {
        catalog.clear()
        catalog.addAll(SQLHelper.getAllChapter(tableName).map { ReaderBean.Catalog(it) })
    }
    /**
     * 自动删除已读章节，但保留最近[NotDeleteNum]章
     */
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

    private fun updateCatalog() {
        DownloadCatalog(tableName, SQLHelper.getCatalogUrl(bookName)).download()
        maxChapterNum = SQLHelper.getChapterCount(tableName)
    }
}