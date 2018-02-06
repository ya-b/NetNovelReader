package com.netnovelreader.common.download

import com.netnovelreader.common.url2Hostname
import com.netnovelreader.common.data.ParseHtml
import com.netnovelreader.common.data.SQLHelper
import java.io.File
import java.io.IOException

/**
 * Created by yangbo on 18-1-27.
 */
class DownloadChapter(
    private val tablename: String, private val dir: String, private val chapterName: String,
    private val chapterUrl: String
) {

    @Throws(IOException::class)
    fun download(chapterText: String): Int {
        if (chapterText.isEmpty()) return 1
        File(dir, chapterName).takeIf { !it.exists() }
                ?.run {
                    this.writeText(chapterText)
                    SQLHelper.setChapterFinish(tablename, chapterName, chapterUrl, true)
                }
        return 1
    }

    @Throws(IOException::class)
    fun getChapterTxt(): String {
        if (!chapterUrl.startsWith("http") || File(dir, chapterName).exists()) return ""
        var str = ParseHtml().getChapter(chapterUrl)
        SQLHelper.getParseRule(url2Hostname(chapterUrl), SQLHelper.CATALOG_FILTER)
                .takeIf { it.length > 0 }
                ?.split("|")
                ?.forEach { str = str.replace(it, "") }
        return str
    }
}