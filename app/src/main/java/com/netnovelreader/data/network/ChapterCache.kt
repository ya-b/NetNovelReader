package com.netnovelreader.data.network

import com.netnovelreader.common.getSavePath
import com.netnovelreader.data.db.ReaderDbManager
import kotlinx.coroutines.experimental.launch
import java.io.File
import java.io.IOException
import java.util.*

/**
 * Created by yangbo on 18-1-15.
 */
class ChapterCache(private val cacheNum: Int, private val tableName: String) {
    companion object {
        val FILENOTFOUND = "            "
    }

    /**
     * hashTablb<第几章，章节内容>
     */
    private val chapterTxtTable = Hashtable<Int, String>()
    /**
     * 最大章节数
     */
    private var maxChapterNum = 0
    private var dirName: String? = null
    fun init(maxChapterNum: Int, dirName: String) {
        this.maxChapterNum = maxChapterNum
        this.dirName = dirName
    }

    fun getChapter(chapterNum: Int, enableDownload: Boolean = true): String {
        try {
            readToCache(chapterNum)
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
                        chapterTxtTable.put(chapterNum, this)
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
        val chapterName = ReaderDbManager.getChapterName(dirName!!, chapterNum)
        sb.append(chapterName + "|")
        val chapterFile = File("${getSavePath()}/$dirName/$chapterName")
        sb.append(
            if (chapterFile.exists() && chapterFile.isFile) chapterFile.readText()
            else if (!enableDownload) FILENOTFOUND
            else getFromNet("${getSavePath()}/$dirName", chapterName)
        )
        return sb.toString()
    }

    /**
     * 从网络获取章节内容
     */
    fun getFromNet(dir: String, chapterName: String): String {
        if (dir.isEmpty() || chapterName.isEmpty()) return FILENOTFOUND
        val download = DownloadChapter(
            tableName, dir, chapterName, ReaderDbManager.getChapterUrl(tableName, chapterName)
        )
        return try {
            download.getChapterTxt().apply { download.download(this) }
        } catch (e: IOException) {
            FILENOTFOUND
        }
    }

    /**
     * 读取章节内容到缓存里（不包括正在阅读的章节）
     */
    @Throws(IOException::class)
    private fun readToCache(chapterNum: Int) = launch {
        if (cacheNum == 0) return@launch
        chapterTxtTable.filter { it.key + 1 < chapterNum || it.key - cacheNum > chapterNum || it.value.isEmpty() }
            .forEach { chapterTxtTable.remove(it.key) }
        if (chapterNum > 1 && !chapterTxtTable.contains(chapterNum - 1)) {
            chapterTxtTable.put(chapterNum - 1, getText(chapterNum - 1, true))
        }
        (1..cacheNum)
            .filter { chapterNum + it <= maxChapterNum && !chapterTxtTable.contains(chapterNum + it) }
            .forEach { chapterTxtTable.put(chapterNum + it, getText(chapterNum + it, true)) }
    }
}