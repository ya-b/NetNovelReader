package com.netnovelreader.reader

import com.netnovelreader.common.DownloadTask
import com.netnovelreader.common.getSavePath
import com.netnovelreader.data.SQLHelper
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.util.*

/**
 * Created by yangbo on 18-1-15.
 */
class ChapterCache(val cacheNum: Int, val tableName: String) {
    /**
     * hashTablb<第几章，章节内容>
     */
    val chapterTxtTable = Hashtable<Int, String>()
    /**
     * 最大章节数
     */
    var maxChapterNum = 0
    var dirName: String? = null
    fun prepare(chapterNum: Int, maxChapterNum: Int, dirName: String) {
        this.maxChapterNum = maxChapterNum
        this.dirName = dirName
        Thread { getChapter(chapterNum) }.start()
    }

    fun getChapter(chapterNum: Int): String {
        var result: String?
        if (chapterTxtTable.containsKey(chapterNum)) {
            result = chapterTxtTable.get(chapterNum)
        } else {
            try {
                result = getText(chapterNum, true)
                chapterTxtTable.put(chapterNum, result)
            } catch (e: IOException) {
                result = ""
            }
        }
        Thread { readToCache(chapterNum) }.start()
        return result ?: ""
    }

    /**
     * 获取小说章节内容
     * @chapterNum:Int 章节数
     * @isCurrentChapter 是否为将要阅读的章节，如果不是，从网络下载，如果是，让主线程进行处理
     */
    @Throws(IOException::class)
    fun getText(chapterNum: Int, isCurrentChapter: Boolean): String {
        val sb = StringBuilder()
        val chapterName = SQLHelper.getChapterName(dirName!!, chapterNum)
        sb.append(chapterName + "|")
        val chapterPath = "${getSavePath()}/$dirName/$chapterName"
        if (!File(chapterPath).exists()) {
            if (!isCurrentChapter) {
                sb.append(getFromNet("${getSavePath()}/$dirName",
                        chapterName))
            }
        } else {
            sb.append(getFromFile(chapterPath))
        }
        return sb.toString()
    }

    /**
     * 从本地文件获取章节内容
     */
    @Throws(IOException::class)
    fun getFromFile(chapterPath: String): String {
        val sb = StringBuilder()
        val fr = FileReader(chapterPath)
        fr.forEachLine { sb.append(it + "\n") }
        fr.close()
        return sb.toString()
    }

    /**
     * 从网络获取章节内容
     */
    fun getFromNet(dir: String, chapterName: String): String {
        val download = DownloadTask.DownloadChapterUnit(tableName, dir, chapterName,
                SQLHelper.getChapterUrl(tableName, chapterName))
        var chapterText: String? = null
        try {
            chapterText = download.getChapterTxt()
            download.download(chapterText)
        }catch (e: IOException){
            chapterText = ""
        }finally {
            return chapterText ?: ""
        }
    }

    /**
     * 章节内容读到map里
     */
    fun readToCache(chapterNum: Int) {
        val iterator = chapterTxtTable.iterator()
        val arrayList = ArrayList<Int>(0)
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.key + 1 < chapterNum || entry.key - cacheNum > chapterNum || entry.value.length == 0) {
                arrayList.add((entry.key))
            }
        }
        arrayList.forEach {
            chapterTxtTable.remove(it)
        }
        if (chapterNum > 1) {
            if (!chapterTxtTable.contains(chapterNum - 1)) {
                chapterTxtTable.put(chapterNum - 1, getText(chapterNum - 1, false))
            }
        }
        for (i in 1..cacheNum) {
            if (chapterNum + i <= maxChapterNum && !chapterTxtTable.contains(chapterNum + i)) {
                chapterTxtTable.put(chapterNum + i, getText(chapterNum + i, false))
            }
        }
    }
}