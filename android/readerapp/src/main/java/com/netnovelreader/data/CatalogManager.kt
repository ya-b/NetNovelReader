package com.netnovelreader.data

import com.netnovelreader.ReaderApplication
import com.netnovelreader.bean.SearchBookResult
import com.netnovelreader.common.replace
import com.netnovelreader.common.tryIgnoreCatch
import com.netnovelreader.common.url2Hostname
import com.netnovelreader.data.local.ReaderDbManager
import com.netnovelreader.data.network.ParseHtml
import java.io.File
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.LinkedHashMap
import kotlin.collections.HashMap
import kotlin.collections.set

object CatalogManager {
    /**
     * HashMap<目录页url, LinkedHashMap<章节名, 章节url>>
     */
    private val memoryCache: HashMap<String, SearchBookResult> = HashMap()
    private val diskCache: HashMap<String, String> = HashMap()
    private var count = 0
    private val MAX_COUNT = 5000
    private val UPDATEFLAG = "●"  //书籍有更新，显示该标志
    /**
     * 下载目录并保存到数据库
     */
    @Throws(IOException::class)
    fun download(tableName: String, catalogUrl: String, isUpdate: String = UPDATEFLAG) {
        ReaderDbManager.createTable(tableName)
        val map = CatalogManager.getFromCache(catalogUrl)?.catalogMap
            .let { if (it.isNullOrEmpty()) getMapFromNet(catalogUrl) else it }
            ?.let { filtExistsInSql(tableName, it) }
            ?.takeIf { !it.isNullOrEmpty() } ?: return
        ReaderDbManager.runInTransaction {
            map.forEach { ReaderDbManager.setChapterFinish(tableName, it.key, it.value, 0) }
        }
        map.entries.toTypedArray().last().key
            .takeIf { it != ReaderDbManager.shelfDao().getBookInfo(tableName)?.latestChapter }
            ?.also { ReaderDbManager.shelfDao().replace(bookName = tableName, isUpdate = isUpdate, latestChapter = it) }
    }

    @Suppress("UNCHECKED_CAST")
    fun addToCache(bookname: String, catalogUrl: String) {
        val selector = ReaderDbManager.sitePreferenceDao().getRule(url2Hostname(catalogUrl)).catalogSelector
        val map = tryIgnoreCatch { ParseHtml().getCatalog(catalogUrl, selector) }
                ?.takeIf { !it.isNullOrEmpty() } ?: return
        val result = map.takeIf { map.isNotEmpty() }?.entries?.toTypedArray()?.last()?.key
            .let { SearchBookResult.new(bookname, catalogUrl, it ?: "", map) }
        count += map.size
        if (count < MAX_COUNT) {
            memoryCache[catalogUrl] = result
        } else {  //缓存到磁盘
            diskCache[catalogUrl] = writeToDisk(result)
        }
    }

    fun getFromCache(catalogUrl: String): SearchBookResult? {
        val result = memoryCache[catalogUrl]
                ?: let {
                    if (!diskCache[catalogUrl].isNullOrEmpty())
                        readFromDisk(diskCache[catalogUrl]!!)
                    else
                        null
                }
        return result
    }

    fun clearCache() {
        count = 0
        memoryCache.clear()
        File("${ReaderApplication.dirPath}/tmp").deleteRecursively()
    }

    private fun writeToDisk(searchBean: SearchBookResult): String {
        val file = File("${ReaderApplication.dirPath}/tmp")
            .apply { mkdirs() }
            .let { File(it, searchBean.hashCode().toString()) }
        tryIgnoreCatch {
            ObjectOutputStream(file.outputStream())
                .use { it.writeObject(searchBean) }
        }
        return file.path
    }

    private fun readFromDisk(path: String): SearchBookResult? {
        val file = File(path).takeIf { !it.exists() } ?: return null
        var result: SearchBookResult? = null
        try {
            ObjectInputStream(file.inputStream()).use {
                result = it.readObject() as SearchBookResult
            }
        }catch (e: IOException) {
            e.printStackTrace()
        }
        return result
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

    private fun <K, V> Map<K, V>?.isNullOrEmpty() = this == null || this.isEmpty()
}