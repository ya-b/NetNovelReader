package com.netnovelreader.common.download

import android.databinding.ObservableField
import android.util.LruCache
import com.netnovelreader.common.data.ParseHtml
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
    val cache: LruCache<String, SearchBean>

    init {
        cache = LruCache(10)
    }

    fun addCatalog(bookname: String, catalogUrl: String) {

       // Logger.i("步骤3.正准备从目录Url【$catalogUrl】中解析出书籍【$bookname】的最新章节名")
        val map: LinkedHashMap<String, String>
        try {
            map = ParseHtml().getCatalog(catalogUrl)
        } catch (e: IOException) {
            return
        }
        var latestChapter: String? = null
        map.forEach {
            latestChapter = it.key
        }
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
        cache.evictAll()
    }
}