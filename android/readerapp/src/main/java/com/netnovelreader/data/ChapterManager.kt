package com.netnovelreader.data

import com.netnovelreader.ReaderApplication
import com.netnovelreader.bean.ChapterBean
import com.netnovelreader.common.replaceSlash
import com.netnovelreader.common.tryIgnoreCatch
import com.netnovelreader.common.url2Hostname
import com.netnovelreader.data.local.ReaderDbManager
import com.netnovelreader.data.network.ParseHtml
import kotlinx.coroutines.experimental.launch
import java.io.File
import java.io.IOException
import java.util.*

class ChapterManager(val cacheNum: Int, val tableName: String, var maxChapterNum: Int) {
    companion object {
        const val FILENOTFOUND = "            "
    }

    /**
     * hashTable<第几章，章节内容>
     */
    private val chapterTxtTable = Hashtable<Int, String>()

    fun getChapter(chapterNum: Int, enableDownload: Boolean = true): String {
        launch {
            try {
                readToCache(chapterNum)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return if (chapterTxtTable.containsKey(chapterNum)) {
            chapterTxtTable[chapterNum] ?: " |"+FILENOTFOUND
        } else {
            getText(chapterNum, enableDownload).apply {
                substring(indexOf("|") + 1)
                    .takeIf { !it.isEmpty() && it != FILENOTFOUND }
                    ?.also { chapterTxtTable[chapterNum] = this }
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
    private fun getText(chapterNum: Int, enableDownload: Boolean): String {
        val sb = StringBuilder()
        val chapterName = ReaderDbManager.getChapterName(tableName, chapterNum).also { sb.append("$it|") }
        if (chapterName.isNotEmpty()) {
            val chapterFile = File("${ReaderApplication.dirPath}/$tableName/${chapterName.replaceSlash()}")
            when {
                chapterFile.exists() -> sb.append(chapterFile.readText())
                !enableDownload -> sb.append(FILENOTFOUND)
                else -> sb.append(getFromNet("${ReaderApplication.dirPath}/$tableName", chapterName))
            }
        }
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
        return tryIgnoreCatch { getChapterTxt(bean).apply { writToDisk(bean, this) } }
                ?: FILENOTFOUND
    }


    @Throws(IOException::class)
    fun writToDisk(bean: ChapterBean, chapterText: String) {
        if (chapterText.isEmpty()) return
        val file = File(bean.dir, bean.chapterName.replaceSlash())
        if (file.exists()) return
        file.writeText(chapterText)
        ReaderDbManager.setChapterFinish(bean.bookname, bean.chapterName, bean.chapterUrl, 1)
    }

    @Throws(IOException::class)
    fun getChapterTxt(bean: ChapterBean): String {
        if (!bean.chapterUrl.startsWith("http")) return ""
        File(bean.dir, bean.chapterName).takeIf { it.exists() }?.let { return it.readText() }
        val selector = ReaderDbManager.sitePreferenceDao().getRule(url2Hostname(bean.chapterUrl)).chapterSelector
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
        (1..cacheNum).filter { chapterNum + it <= maxChapterNum && !chapterTxtTable.contains(chapterNum + it) }
            .forEach { chapterTxtTable[chapterNum + it] = getText(chapterNum + it, true) }
    }
}