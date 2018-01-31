package com.netnovelreader.download

import android.databinding.ObservableField
import com.netnovelreader.common.url2Hostname
import com.netnovelreader.data.ParseHtml
import com.netnovelreader.data.SQLHelper
import com.netnovelreader.search.SearchBean
import java.io.IOException
import java.util.*

/**
 * Created by yangbo on 18-1-30.
 */
object CatalogCache {
    /**
     * HashMap<目录页url, LinkedHashMap<章节名, 章节url>>
     */
    val cache: Hashtable<String, SearchBean>

    init {
        cache = Hashtable<String, SearchBean>()
    }

    fun addCatalog(bookname: String, catalogUrl: String) {
        val map: LinkedHashMap<String, String>
        try {
            map = ParseHtml().getCatalog(catalogUrl)
        } catch (e: IOException) {
            return
        }
        val filters = SQLHelper.getParseRule(url2Hostname(catalogUrl), SQLHelper.CATALOG_FILTER)
        val arr = ArrayList<String>()
        var latestChapter: String? = null
        map.forEach {
            filters.split("|").forEach { filter ->
                if (it.key.contains(filter)) arr.add(it.key)
                latestChapter = it.key
            }
        }
        arr.forEach { map.remove(it) }
        cache.put(
                catalogUrl, SearchBean(
                ObservableField(bookname),
                ObservableField(catalogUrl),
                ObservableField(latestChapter),
                map
        )
        )
    }

    fun clearCache() {
        cache.clear()
    }
}