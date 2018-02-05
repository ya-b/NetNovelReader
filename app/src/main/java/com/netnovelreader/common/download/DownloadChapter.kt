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
        if (chapterText.isEmpty()) {
            SQLHelper.setChapterFinish(tablename, chapterName, chapterUrl, false)
        } else {
            val file = File(dir, chapterName)
            if (file.exists()) return 1
            file.writeText(chapterText)
            SQLHelper.setChapterFinish(tablename, chapterName, chapterUrl, true)
        }
        return 1
    }

    @Throws(IOException::class)
    fun getChapterTxt(): String {
        if (!chapterUrl.startsWith("http")) return ""
        var str = ParseHtml().getChapter(chapterUrl)
        val filter = SQLHelper.getParseRule(url2Hostname(chapterUrl), SQLHelper.CATALOG_FILTER)
        if (filter.length > 0) {
            filter.split("|").forEach {
                str = str.replace(it, "")
            }
        }
        return str
    }
}