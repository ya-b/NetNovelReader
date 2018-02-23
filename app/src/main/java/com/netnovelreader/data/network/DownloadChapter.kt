package com.netnovelreader.data.network

import com.netnovelreader.common.url2Hostname
import com.netnovelreader.data.db.ReaderDbManager
import com.netnovelreader.data.db.ReaderSQLHelper
import java.io.File
import java.io.IOException

/**
 * Created by yangbo on 18-1-27.
 */
class DownloadChapter(
    private val tablename: String, private val dir: String, val chapterName: String,
    private val chapterUrl: String
) {

    @Throws(IOException::class)
    fun download(chapterText: String): Int {
        if (chapterText.isEmpty()) return 1
        File(dir, chapterName).takeIf { !it.exists() }?.run {
            writeText(chapterText)
            ReaderDbManager.setChapterFinish(tablename, chapterName, chapterUrl, 1)
        }
        return 1
    }

    @Throws(IOException::class)
    fun getChapterTxt(): String {
        if (!chapterUrl.startsWith("http")) return ""
        File(dir, chapterName).takeIf { it.exists() }?.run { return this.readText() }
        var str = ParseHtml().getChapter(chapterUrl)
        ReaderDbManager.getParseRule(url2Hostname(chapterUrl), ReaderSQLHelper.CATALOG_FILTER)
            .takeIf { it.isNotEmpty() }
            ?.split("|")
            ?.forEach { str = str.replace(it, "") }
        return str
    }
}