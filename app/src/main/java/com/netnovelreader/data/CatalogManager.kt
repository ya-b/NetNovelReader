package com.netnovelreader.data

import android.databinding.ObservableField
import com.netnovelreader.ReaderApplication
import com.netnovelreader.bean.SearchBean
import com.netnovelreader.common.UPDATEFLAG
import com.netnovelreader.common.replace
import com.netnovelreader.common.url2Hostname
import com.netnovelreader.data.local.ReaderDbManager
import com.netnovelreader.data.network.ParseHtml
import java.io.*
import java.util.LinkedHashMap
import kotlin.collections.HashMap
import kotlin.collections.set

/**
 * Created by yangbo on 18-1-30.
 */
object CatalogManager {
    /**
     * HashMap<目录页url, LinkedHashMap<章节名, 章节url>>
     */
    private val memoryCache: HashMap<String, SearchBean> = HashMap()
    private val diskCache: HashMap<String, String> = HashMap()
    private var count = 0
    private val MAX_COUNT = 5000

    /**
     * 下载目录并保存到数据库
     */
    @Throws(IOException::class)
    fun download(tableName: String, catalogUrl: String, isUpdate: String = UPDATEFLAG) {
        ReaderDbManager.createTable(tableName)
        val cacheMap = CatalogManager.getFromCache(catalogUrl)?.catalogMap
        val map =
                (if (cacheMap != null && cacheMap.isNotEmpty()) cacheMap else getMapFromNet(catalogUrl))
                        .let { filtExistsInSql(tableName, it) }
        var latestChapter: String? = null
        ReaderDbManager.runInTransaction {
            map.forEach {
                ReaderDbManager.setChapterFinish(tableName, it.key, it.value, 0)
                latestChapter = it.key
            }
        }
        val dbLatestChapter = ReaderDbManager.shelfDao().getBookInfo(tableName)?.latestChapter
        if (latestChapter != null && dbLatestChapter != latestChapter) {
            ReaderDbManager.shelfDao().replace(
                    bookName = tableName, isUpdate = isUpdate, latestChapter = latestChapter
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun addToCache(bookname: String, catalogUrl: String) {
        val selector = ReaderDbManager.sitePreferenceDao().getRule(url2Hostname(catalogUrl)).catalogSelector
        val map: LinkedHashMap<String, String> = try {
            ParseHtml().getCatalog(catalogUrl, selector)
        } catch (e: IOException) {
            null
        }?.takeIf { it.isNotEmpty() } ?: return
        count += map.size
        val latestChapter = map.entries.toTypedArray().last().key
        val result = SearchBean(
                ObservableField(bookname),
                ObservableField(catalogUrl),
                ObservableField(latestChapter),
                map
        )
        if (count < MAX_COUNT) {
            memoryCache[catalogUrl] = result
        } else {  //缓存到磁盘
            diskCache[catalogUrl] = writeToDisk(result)
        }
    }

    fun getFromCache(catalogUrl: String): SearchBean? {
        var result = memoryCache[catalogUrl]
        if (result == null && !diskCache[catalogUrl].isNullOrEmpty()) {
            result = readFromDisk(diskCache[catalogUrl]!!)
        }
        return result
    }

    fun clearCache() {
        count = 0
        memoryCache.clear()
        try {
            File("${ReaderApplication.dirPath}/tmp").listFiles().forEach { it.delete() }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun writeToDisk(searchBean: SearchBean): String {
        val file = File("${ReaderApplication.dirPath}/tmp", searchBean.hashCode().toString())
        var outputStream: ObjectOutputStream? = null
        try {
            file.parentFile.mkdirs()
            outputStream = ObjectOutputStream(FileOutputStream(file))
            outputStream.writeObject(searchBean)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            outputStream?.close()
        }
        return file.path
    }

    private fun readFromDisk(path: String): SearchBean? {
        var inputStream: ObjectInputStream? = null
        val file = File(path)
        if (!file.exists()) return null
        return try {
            inputStream = ObjectInputStream(FileInputStream(file))
            inputStream.readObject() as SearchBean
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } finally {
            inputStream?.close()
        }
    }


    /**
     * 从网上下载目录
     */
    @Throws(IOException::class)
    private fun getMapFromNet(catalogUrl: String): LinkedHashMap<String, String> {
        val selector = ReaderDbManager.sitePreferenceDao().getRule(url2Hostname(catalogUrl)).catalogSelector
        val map = ParseHtml().getCatalog(catalogUrl, selector)
        ReaderDbManager.sitePreferenceDao().getRule(url2Hostname(catalogUrl)).catalogFilter
                .takeIf { it.isNotEmpty() }
                ?.split("|")
                ?.also { list ->
                    map.filter { entry -> !list.none { entry.key.contains(it) } }.forEach { map.remove(it.key) }
                }
        return map
    }

    /**
     * 过滤掉已经存在的目录
     */
    private fun filtExistsInSql(tableName: String, map: LinkedHashMap<String, String>): LinkedHashMap<String, String> {
        val arrlist = ReaderDbManager.getAllChapter(tableName)
        val result = LinkedHashMap<String, String>()
        map.forEach {
            if (arrlist.contains(it.key)) {
                result.clear()
            } else {
                result[it.key] = it.value
            }
        }
        return result
    }

}