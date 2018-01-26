package com.netnovelreader.download

import com.netnovelreader.common.tableName2Id
import com.netnovelreader.data.ParseHtml
import java.io.IOException
import java.util.*

/**
 * Created by yangbo on 18-1-27.
 */
class DownloadCatalog(val tableName: String, val url: String) {

    @Throws(IOException::class)
    fun download() {
        val map = ParseHtml().getCatalog(url)
        SQLHelper.createTable(tableName)
        val chapterInSql = SQLHelper.getAllChapter(tableName)
        val iterator = map.iterator()
        var entry: MutableMap.MutableEntry<String, String>? = null
        while (iterator.hasNext()) {
            entry = iterator.next()
            if (!chapterInSql.contains(entry.key)) {
                SQLHelper.setChapterFinish(tableName, entry.key, entry.value, false)
            }
        }
        if (entry != null) {
            synchronized(SQLHelper) {
                SQLHelper.getDB().execSQL("update ${SQLHelper.TABLE_SHELF} set " +
                        "${SQLHelper.LATESTCHAPTER}='${entry.key}' where " +
                        "${SQLHelper.ID}=${tableName2Id(tableName)}")
            }
        }
    }

    fun getCatalog(url: String): HashMap<String, String> {
        val map = ParseHtml().getCatalog(url)
        return map
    }

    fun filtCatalog(map: HashMap<String, String>) {
        val arrayList = ArrayList<Int>()
        map.forEach {
            if (it.key.contains("分卷阅读") || !it.key.contains("订阅本卷")) {

            }
        }
    }
}