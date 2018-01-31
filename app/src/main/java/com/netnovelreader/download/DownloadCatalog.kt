package com.netnovelreader.download

import com.netnovelreader.common.tableName2Id
import com.netnovelreader.common.url2Hostname
import com.netnovelreader.data.ParseHtml
import com.netnovelreader.data.SQLHelper
import java.io.IOException

/**
 * Created by yangbo on 18-1-27.
 */
class DownloadCatalog(val tableName: String, val catalogUrl: String) {

    @Throws(IOException::class)
    fun download() {
        SQLHelper.createTable(tableName)
        val cacheMap = CatalogCache.cache.get(catalogUrl)?.catalogMap
        val map: LinkedHashMap<String, String>
        if (cacheMap != null && cacheMap.isNotEmpty()) {
            map = filtExistsInSql(cacheMap)
        } else {
            map = filtExistsInSql(getMap(catalogUrl))
        }
        var latestChapter: String? = null
        map.forEach {
            SQLHelper.setChapterFinish(tableName, it.key, it.value, false)
            latestChapter = it.key
        }
        latestChapter ?: return
        SQLHelper.getDB().execSQL(
                "update ${SQLHelper.TABLE_SHELF} set ${SQLHelper.LATESTCHAPTER}=" +
                        "'$latestChapter' where ${SQLHelper.ID}=${tableName2Id(tableName)}"
        )
        CatalogCache.clearCache()
    }

    @Throws(IOException::class)
    fun getMap(catalogUrl: String): LinkedHashMap<String, String> {
        val map = ParseHtml().getCatalog(catalogUrl)
        val filter = SQLHelper.getParseRule(url2Hostname(catalogUrl), SQLHelper.CATALOG_FILTER)
        if (filter.length > 0) {
            filtCatalog(map, filter.split("|"))
        }
        return map
    }

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