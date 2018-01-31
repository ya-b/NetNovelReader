package com.netnovelreader.download

import com.netnovelreader.common.url2Hostname
import com.netnovelreader.data.ParseHtml
import com.netnovelreader.data.SQLHelper
import java.io.File
import java.io.FileWriter
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
        var fos: FileWriter? = null
        try {
            fos = FileWriter(File(dir, chapterName))
            fos.write(chapterText)
            fos.flush()
            SQLHelper.setChapterFinish(tablename, chapterName, chapterUrl, true)
        } catch (e: Exception) {
            SQLHelper.setChapterFinish(tablename, chapterName, chapterUrl, false)
        } finally {
            fos?.close()
            return 1
        }
    }

    @Throws(IOException::class)
    fun getChapterTxt(): String {
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