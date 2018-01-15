package com.netnovelreader.data.network

import com.netnovelreader.data.database.BaseSQLManager
import com.netnovelreader.data.database.ParseSQLManager
import com.netnovelreader.utils.url2Hostname
import org.jsoup.Jsoup

/**
 * Created by yangbo on 18-1-14.
 */
class ParseHtml {
    fun getChapter(url: String): String {
        var selector = ParseSQLManager().getChapterRule(url2Hostname(url), BaseSQLManager.CHAPTER_RULE)
        selector ?: return ""
        val chapter = Jsoup.connect(url).get().select(selector)
        var txt = "    " + chapter.text().replace(" ", "\n\n    ")
        return txt
    }

    fun getCatalog(url: String): LinkedHashMap<String, String> {
        var selector = ParseSQLManager().getChapterRule(url2Hostname(url), BaseSQLManager.CATALOG_RULE)
        var catalog = LinkedHashMap<String, String>()
        selector ?: return catalog
        var list = Jsoup.connect(url).get().select(selector).select("a")
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