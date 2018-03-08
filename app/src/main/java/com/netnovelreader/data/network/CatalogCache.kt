package com.netnovelreader.data.network

import android.databinding.ObservableField
import com.netnovelreader.ReaderApplication
import com.netnovelreader.bean.SearchBean
import java.io.*
import java.util.LinkedHashMap
import kotlin.collections.HashMap
import kotlin.collections.set

/**
 * Created by yangbo on 18-1-30.
 */
object CatalogCache {
    /**
     * HashMap<目录页url, LinkedHashMap<章节名, 章节url>>
     */
    private val memoryCache: HashMap<String, SearchBean> = HashMap()
    private val diskCache: HashMap<String, String> = HashMap()
    private var count = 0
    private val MAX_COUNT = 5000

    @Suppress("UNCHECKED_CAST")
    fun addCatalog(bookname: String, catalogUrl: String) {
        val map: LinkedHashMap<String, String> = try {
            ParseHtml().getCatalog(catalogUrl)
        } catch (e: IOException) {
            null
        }
            ?.takeIf { it.isNotEmpty() } ?: return
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

    fun getCatalog(catalogUrl: String): SearchBean? {
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

    fun writeToDisk(searchBean: SearchBean): String {
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

    fun readFromDisk(path: String): SearchBean? {
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
}