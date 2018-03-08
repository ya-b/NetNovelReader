package com.netnovelreader.data.network

import android.databinding.ObservableField
import com.netnovelreader.bean.SearchBean
import java.io.IOException

/**
 * Created by yangbo on 18-1-30.
 */
object CatalogCache {
    /**
     * HashMap<目录页url, LinkedHashMap<章节名, 章节url>>
     */
    val cache: HashMap<String, SearchBean> = HashMap()

    @Suppress("UNCHECKED_CAST")
    fun addCatalog(bookname: String, catalogUrl: String) {
        val map = try {
                ParseHtml().getCatalog(catalogUrl)
            } catch (e: IOException) {
                null
            }
            ?.takeIf { it.isNotEmpty() } ?: return

        val tail = map.javaClass.getDeclaredField("tail").apply { isAccessible = true }
        val latestChapter = (tail.get(map) as Map.Entry<String, String>).key
        cache[catalogUrl] = SearchBean(
            ObservableField(bookname),
            ObservableField(catalogUrl),
            ObservableField(latestChapter),
            map
        )
    }

    fun clearCache() {
        cache.clear()
    }
}