package com.netnovelreader.common.download

import com.netnovelreader.common.UPDATEFLAG
import com.netnovelreader.common.data.ParseHtml
import com.netnovelreader.common.data.SQLHelper
import com.netnovelreader.common.tableName2Id
import com.netnovelreader.common.url2Hostname
import java.io.IOException

/**
 * Created by yangbo on 18-1-27.
 */
class DownloadCatalog(val tableName: String, val catalogUrl: String) {

    /**
     * 下载目录并保存到数据库
     */
    @Throws(IOException::class)
    fun download() {
        SQLHelper.createTable(tableName)
        val cacheMap = CatalogCache.cache.get(catalogUrl)?.catalogMap
        var latestChapter: String? = null
        try {
            SQLHelper.doTransaction = true
            SQLHelper.getDB().beginTransaction()
            filtExistsInSql(if (cacheMap != null && cacheMap.isNotEmpty()) cacheMap else getMapFromNet(catalogUrl))
                .forEach {
                    SQLHelper.setChapterFinish(tableName, it.key, it.value, 0)
                    latestChapter = it.key
                }
            SQLHelper.getDB().setTransactionSuccessful()
        }finally {
            SQLHelper.getDB().endTransaction()
            SQLHelper.doTransaction = false
        }
        latestChapter ?: return
        SQLHelper.getDB().execSQL(
            "update ${SQLHelper.TABLE_SHELF} set ${SQLHelper.LATESTCHAPTER}=" +
                    "'$latestChapter',${SQLHelper.ISUPDATE}='$UPDATEFLAG' where ${SQLHelper.ID}=" +
                    "${tableName2Id(tableName)};"
        )
        CatalogCache.clearCache()
    }

    /**
     * 从网上下载目录
     */
    @Throws(IOException::class)
    fun getMapFromNet(catalogUrl: String): LinkedHashMap<String, String> {
        val map = ParseHtml().getCatalog(catalogUrl)
        val filter = SQLHelper.getParseRule(url2Hostname(catalogUrl), SQLHelper.CATALOG_FILTER)
        if (filter.length > 0) {
            filtCatalog(map, filter.split("|"))
        }
        return map
    }

    /**
     * 过滤目录的某些章节，从map中过滤掉filter
     */
    fun filtCatalog(
        map: LinkedHashMap<String, String>,
        filters: List<String>
    ): LinkedHashMap<String, String> {
        val arr = ArrayList<String>()
        map.forEach {
            filters.forEach { filter ->
                if (it.key.contains(filter)) {
                    arr.add(it.key)
                }
            }
        }
        arr.forEach { map.remove(it) }
        return map
    }

    /**
     * 过滤掉已经存在的目录
     */
    fun filtExistsInSql(map: LinkedHashMap<String, String>): LinkedHashMap<String, String> {
        val arrlist = SQLHelper.getAllChapter(tableName)
        val result = LinkedHashMap<String, String>()
        map.forEach {
            if (arrlist.contains(it.key)) {
                result.clear()
            } else {
                result.put(it.key, it.value)
            }
        }
        return result
    }
}