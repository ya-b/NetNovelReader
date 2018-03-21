package com.netnovelreader.data

import com.netnovelreader.ReaderApplication
import com.netnovelreader.bean.ChapterBean
import com.netnovelreader.common.SLASH
import com.netnovelreader.common.url2Hostname
import com.netnovelreader.data.local.ReaderDbManager
import com.netnovelreader.data.network.ParseHtml
import kotlinx.coroutines.experimental.launch
import java.io.File
import java.io.IOException
import java.util.*

/**
 * Created by yangbo on 18-1-15.
 */
class ChapterManager(val cacheNum: Int, val tableName: String, var maxChapterNum: Int) {
    companion object {
        const val FILENOTFOUND = "            "
    }

    /**
     * hashTablb<第几章，章节内容>
     */
    private val chapterTxtTable = Hashtable<Int, String>()

    fun getChapter(chapterNum: Int, enableDownload: Boolean = true): String {
        try {
            launch { readToCache(chapterNum) }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return if (chapterTxtTable.containsKey(chapterNum)) {
            chapterTxtTable[chapterNum] ?: " |"+FILENOTFOUND
        } else {
            try {
                getText(chapterNum, enableDownload).apply {
                    val str = this.substring(this.indexOf("|") + 1)
                    if (!str.isEmpty() && str != FILENOTFOUND) {
                        chapterTxtTable[chapterNum] = this
                    }
                }
            } catch (e: IOException) {
                " |" + FILENOTFOUND
            }
        }
    }

    fun clearCache() {
        chapterTxtTable.clear()
    }

    /**
     * 获取小说章节内容
     * @chapterNum:Int 章节数
     * @isCurrentChapter 是否为将要阅读的章节，如果不是，从网络下载，如果是，让主线程进行处理
     */
    @Throws(IOException::class)
    private fun getText(chapterNum: Int, enableDownload: Boolean): String {
        val sb = StringBuilder()
        val chapterName = ReaderDbManager.getChapterName(tableName, chapterNum)
        sb.append(chapterName + "|")
        val chapterFile =
                File("${ReaderApplication.dirPath}/$tableName/${chapterName.replace("/", SLASH)}")
        sb.append(
                if (chapterFile.exists() && chapterFile.isFile) chapterFile.readText()
                else if (!enableDownload) FILENOTFOUND
                else getFromNet("${ReaderApplication.dirPath}/$tableName", chapterName)
        )
        return sb.toString()
    }

    /**
     * 从网络获取章节内容
     */
    fun getFromNet(dir: String, chapterName: String): String {
        if (dir.isEmpty() || chapterName.isEmpty()) return FILENOTFOUND
        val bean = ChapterBean(
                tableName, dir, chapterName, ReaderDbManager.getChapterUrl(tableName, chapterName)
        )
        return try {
            getChapterTxt(bean).apply { writToDisk(bean, this) }
        } catch (e: IOException) {
            FILENOTFOUND
        }
    }


    @Throws(IOException::class)
    fun writToDisk(bean: ChapterBean, chapterText: String): Int {
        if (chapterText.isEmpty()) return 1
        File(bean.dir, bean.chapterName.replace("/", SLASH)).takeIf { !it.exists() }?.run {
            writeText(chapterText)
            ReaderDbManager.setChapterFinish(bean.bookname, bean.chapterName, bean.chapterUrl, 1)
        }
        return 1
    }

    @Throws(IOException::class)
    fun getChapterTxt(bean: ChapterBean): String {
        if (!bean.chapterUrl.startsWith("http")) return ""
        File(bean.dir, bean.chapterName).takeIf { it.exists() }?.run { return this.readText() }
        val selector = ReaderDbManager.sitePreferenceDao().getRule(url2Hostname(bean.chapterUrl))
                .chapterSelector
        var str = ParseHtml().getChapter(bean.chapterUrl, selector)
        ReaderDbManager.sitePreferenceDao().getRule(url2Hostname(bean.chapterUrl))
                .catalogFilter
                .takeIf { it.isNotEmpty() }
                ?.split("|")
                ?.forEach { str = str.replace(it, "") }
        return str
    }

    /**
     * 读取章节内容到缓存里（不包括正在阅读的章节）
     */
    @Throws(IOException::class)
    private fun readToCache(chapterNum: Int) {
        if (cacheNum == 0) return
        chapterTxtTable.filter { it.key + 1 < chapterNum || it.key - cacheNum > chapterNum || it.value.isEmpty() }
                .forEach { chapterTxtTable.remove(it.key) }
        if (chapterNum > 1 && !chapterTxtTable.contains(chapterNum - 1)) {
            chapterTxtTable[chapterNum - 1] = getText(chapterNum - 1, true)
        }
        (1..cacheNum)
                .filter { chapterNum + it <= maxChapterNum && !chapterTxtTable.contains(chapterNum + it) }
                .forEach { chapterTxtTable[chapterNum + it] = getText(chapterNum + it, true) }
    }
}