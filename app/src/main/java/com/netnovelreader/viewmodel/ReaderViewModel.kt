package com.netnovelreader.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.databinding.ObservableArrayList
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import com.netnovelreader.bean.ReaderBean
import com.netnovelreader.common.NotDeleteNum
import com.netnovelreader.common.getSavePath
import com.netnovelreader.data.db.ReaderDbManager
import com.netnovelreader.data.db.ShelfBean
import com.netnovelreader.data.network.ChapterCache
import com.netnovelreader.data.network.DownloadCatalog
import com.netnovelreader.interfaces.IReaderContract
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.io.File
import java.io.IOException

/**
 * Created by yangbo on 18-1-13.
 */

class ReaderViewModel(context: Application) : AndroidViewModel(context),
        IReaderContract.IReaderViewModel {

    enum class CHAPTERCHANGE {
        NEXT,                       //下一章
        PREVIOUS,                   //上一章
        BY_CATALOG                  //通过目录翻页
    }

    val catalog by lazy { ObservableArrayList<ReaderBean>() }
    /**
     * 一页显示的内容
     */
    var text: ObservableField<String> = ObservableField("")

    var isLoading = ObservableBoolean(true)

    @Volatile
    var chapterName: String? = null
    /**
     * 章节数,最大章节数
     */
    @Volatile
    var chapterNum = 1
    @Volatile
    var maxChapterNum = 0

    var chapterCache: ChapterCache? = null

    private lateinit var bookName: String
    private var CACHE_NUM: Int = 0
    /**
     * readerView第一次绘制时执行, 返还阅读记录页数
     */
    override suspend fun initData(bookName: String, CACHE_NUM: Int): Int {
        this.bookName = bookName
        this.CACHE_NUM = CACHE_NUM
        maxChapterNum = ReaderDbManager.getChapterCount(bookName).takeIf { it != 0 } ?: return 0
        val record = getRecord()
        chapterNum = record[0]
        chapterCache = ChapterCache(CACHE_NUM, bookName).apply { init(maxChapterNum, bookName) }
        return record[1]
    }

    //获取章节内容
    override suspend fun getChapter(type: CHAPTERCHANGE, chapterName: String?) {
        when (type) {
            CHAPTERCHANGE.NEXT -> if (chapterNum >= maxChapterNum) return else chapterNum++
            CHAPTERCHANGE.PREVIOUS -> if (chapterNum < 2) return else chapterNum--
            CHAPTERCHANGE.BY_CATALOG -> chapterName?.run {
                chapterNum = ReaderDbManager.getChapterId(bookName, chapterName)
            }
        }
        isLoading.set(true)
        launch { if (chapterNum == maxChapterNum) updateCatalog() }
        val str = chapterCache!!.getChapter(chapterNum, false)
        text.set(str)
        this.chapterName = str.substring(0, str.indexOf("|"))
        if (str.substring(str.indexOf("|") + 1) == ChapterCache.FILENOTFOUND) {
            downloadAndShow()
        } else {
            isLoading.set(false)
        }
    }

    //下载并显示，阅读到未下载章节时调用
    override suspend fun downloadAndShow() {
        chapterCache ?: return
        var str = ChapterCache.FILENOTFOUND
        var times = 0
        while (str == ChapterCache.FILENOTFOUND && times++ < 10) {
            str = chapterCache!!.getChapter(chapterNum)
            delay(500)
        }
        if (str != ChapterCache.FILENOTFOUND && str.isNotEmpty()) {
            isLoading.set(false)
            getChapter(ReaderViewModel.CHAPTERCHANGE.BY_CATALOG, null)
        }
    }

    override suspend fun reloadCurrentChapter() {
        chapterCache ?: return
        updateCatalog()
        getCatalog()
        chapterCache!!.clearCache()
        getChapter(CHAPTERCHANGE.BY_CATALOG, null)
    }

    /**
     * 保存阅读记录
     */
    @Synchronized
    override suspend fun setRecord(pageNum: Int) {
        if (chapterNum < 1) return
        ReaderDbManager.getRoomDB().shelfDao().replace(ShelfBean(bookName = bookName,
                 readRecord = "$chapterNum#${if (pageNum < 1) 1 else pageNum}"))
    }

    /**
     * 重新读取目录
     */
    @Synchronized
    override suspend fun getCatalog() {
        catalog.clear()
        catalog.addAll(ReaderDbManager.getAllChapter(bookName).map { ReaderBean(it) })
    }

    /**
     * 自动删除已读章节，但保留最近[NotDeleteNum]章
     */
    override suspend fun autoRemove() {
        val num = getRecord()[0]
        if (num < NotDeleteNum) return
        val id = num - NotDeleteNum
        ReaderDbManager.setReaded(bookName, id)
                .forEach { File("${getSavePath()}/$bookName/$it").delete() }
    }

    /**
     * 获取阅读记录
     */
    private fun getRecord(): Array<Int> {

        val queryResult = ReaderDbManager.getRoomDB().shelfDao().getBookInfo(bookName)?.readRecord
                ?.split("#")?.map { it.toInt() }          //阅读记录 3#2 表示第3章第2页
        return arrayOf(queryResult?.get(0) ?: 1, queryResult?.get(1) ?: 1)
    }

    private fun updateCatalog() {
        try {
            DownloadCatalog(bookName, ReaderDbManager.getRoomDB().shelfDao().getBookInfo(bookName)?.downloadUrl
                    ?: "").download()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        maxChapterNum = ReaderDbManager.getChapterCount(bookName)
    }
}