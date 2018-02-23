package com.netnovelreader.data.network

import com.netnovelreader.common.tableName2Id
import com.netnovelreader.common.url2Hostname
import com.netnovelreader.data.db.ReaderDbManager
import com.netnovelreader.data.db.ReaderSQLHelper
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
        ReaderDbManager.createTable(tableName)
        val cacheMap = CatalogCache.cache.get(catalogUrl)?.catalogMap
        var map =
            if (cacheMap != null && cacheMap.isNotEmpty()) cacheMap else getMapFromNet(catalogUrl)
        map = filtExistsInSql(map)
        var latestChapter: String? = null
        try {
            ReaderDbManager.doTransaction = true
            ReaderDbManager.getDB().beginTransaction()
            map.forEach {
                ReaderDbManager.setChapterFinish(tableName, it.key, it.value, 0)
                latestChapter = it.key
            }
            ReaderDbManager.getDB().setTransactionSuccessful()
        } finally {
            ReaderDbManager.getDB().endTransaction()
            ReaderDbManager.doTransaction = false
        }
        ReaderDbManager.setLatestChapter(latestChapter, tableName2Id(tableName))
    }

    /**
     * 从网上下载目录
     */
    @Throws(IOException::class)
    fun getMapFromNet(catalogUrl: String): LinkedHashMap<String, String> {
        val map = ParseHtml().getCatalog(catalogUrl)
        val filter =
            ReaderDbManager.getParseRule(url2Hostname(catalogUrl), ReaderSQLHelper.CATALOG_FILTER)
        if (filter.isNotEmpty()) {
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
        val arrlist = ReaderDbManager.getAllChapter(tableName)
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