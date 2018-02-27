package com.netnovelreader.data.network

import com.netnovelreader.common.UPDATEFLAG
import com.netnovelreader.common.replace
import com.netnovelreader.common.url2Hostname
import com.netnovelreader.data.db.ReaderDbManager
import com.netnovelreader.data.db.ShelfBean
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
        val map = (if (cacheMap != null && cacheMap.isNotEmpty()) cacheMap else getMapFromNet(catalogUrl))
                .let { filtExistsInSql(it) }
        var latestChapter: String? = null
        ReaderDbManager.getRoomDB().runInTransaction {
            map.forEach {
                ReaderDbManager.setChapterFinish(tableName, it.key, it.value, 0)
                latestChapter = it.key
            }
        }
        val dbLatestChapter = ReaderDbManager.getRoomDB().shelfDao().getBookInfo(tableName)?.latestChapter
        if(latestChapter != null && dbLatestChapter != latestChapter){
            latestChapter?.let {
                ReaderDbManager.getRoomDB().shelfDao().replace(
                        ShelfBean(bookName = tableName, isUpdate = UPDATEFLAG, latestChapter = it)
                )
            }
        }
    }

    /**
     * 从网上下载目录
     */
    @Throws(IOException::class)
    fun getMapFromNet(catalogUrl: String): LinkedHashMap<String, String> {
        val map = ParseHtml().getCatalog(catalogUrl)
        val filter = ReaderDbManager.getRoomDB().sitePreferenceDao().getRule(url2Hostname(catalogUrl)).catalogFilter
        if (filter.isNotEmpty()) {
            filtCatalog(map, filter.split("|"))
        }
        return map
    }

    /**
     * 过滤目录的某些章节，从map中过滤掉filter
     */
    fun filtCatalog(map: LinkedHashMap<String, String>, filters: List<String>): LinkedHashMap<String, String> {
        map.filter { entry -> filters.filter { entry.key.contains(it) }.size > 0 }.forEach{ map.remove(it.key) }
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