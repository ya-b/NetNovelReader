package com.netnovelreader.data.network

import com.netnovelreader.data.database.BaseSQLManager
import com.netnovelreader.data.database.ParseSQLManager
import com.netnovelreader.utils.TIMEOUT
import com.netnovelreader.utils.UA
import com.netnovelreader.utils.url2Hostname
import org.jsoup.Jsoup
import java.net.SocketTimeoutException

/**
 * Created by yangbo on 18-1-14.
 */
class ParseHtml {
    @Throws(SocketTimeoutException::class)
    fun getChapter(url: String): String {
        var selector = ParseSQLManager().getChapterRule(url2Hostname(url), BaseSQLManager.CHAPTER_RULE)
        selector ?: return ""
        val chapter = Jsoup.connect(url).userAgent(UA)
                .timeout(TIMEOUT).get().select(selector)
        var txt = "    " + chapter.text().replace(" ", "\\n\\n  ")
        return txt
    }

    @Throws(SocketTimeoutException::class)
    fun getCatalog(url: String): LinkedHashMap<String, String> {
        var selector = ParseSQLManager().getChapterRule(url2Hostname(url), BaseSQLManager.CATALOG_RULE)
        var catalog = LinkedHashMap<String, String>()
        selector ?: return catalog
        var list = Jsoup.connect(url).userAgent(UA)
                .timeout(TIMEOUT).get().select(selector).select("a")
        list.forEach {
            var link = it.attr("href")
            if(!link.contains("//")){
                link = url.substring(0, url.lastIndexOf('/') + 1) + link
            }else if(link.startsWith("//")){
                link = "http:" + link
            }
            catalog.put(it.text(), link)
        }
        return catalog
    }
}