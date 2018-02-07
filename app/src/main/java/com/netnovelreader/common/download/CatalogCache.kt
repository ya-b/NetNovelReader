package com.netnovelreader.common.download

import android.databinding.ObservableField
import com.netnovelreader.common.url2Hostname
import com.netnovelreader.common.data.ParseHtml
import com.netnovelreader.common.data.SQLHelper
import com.netnovelreader.search.SearchBean
import com.orhanobut.logger.Logger
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
        cache = Hashtable()
    }

    fun addCatalog(bookname: String, catalogUrl: String) {

        Logger.i("步骤3.正准备从目录Url【$catalogUrl】中解析出书籍【$bookname】的最新章节名")
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
        cache.clear()
    }
}