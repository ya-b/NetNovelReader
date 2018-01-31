package com.netnovelreader.data

import com.netnovelreader.common.TIMEOUT
import com.netnovelreader.common.getHeaders
import com.netnovelreader.common.url2Hostname
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.IOException

/**
 * Created by yangbo on 18-1-14.
 */
class ParseHtml {
    /**
     * 解析章节
     */
    @Throws(IOException::class)
    fun getChapter(url: String): String {
        val txt: String?
        val selector = SQLHelper.getParseRule(url2Hostname(url), SQLHelper.CHAPTER_RULE)
        if (selector.isEmpty() || selector.length < 2) {
            txt = getChapterWithSelector(url)
        } else {
            txt = Jsoup.connect(url).headers(getHeaders(url))
                    .timeout(TIMEOUT).get().select(selector).text()
        }
        return "    " + txt!!.replace(" ", "\n\n  ")
    }

    /**
     * 解析目录
     */
    @Throws(IOException::class)
    fun getCatalog(url: String): LinkedHashMap<String, String> {
        val selector = SQLHelper.getParseRule(url2Hostname(url), SQLHelper.CATALOG_RULE)
        val catalog = LinkedHashMap<String, String>()
        val list = Jsoup.connect(url).headers(getHeaders(url))
                .timeout(TIMEOUT).get().select(selector).select("a")
        list.forEach {
            val link = fixChapterUrl(url, it.attr("href"))
            val name = it.text()
            if (catalog.containsKey(name)) {
                catalog.remove(name)
            }
            catalog.put(name, link)
        }
        return catalog
    }

    private fun fixChapterUrl(catalogUrl: String, chapterUrl: String): String {
        if (chapterUrl.contains("http")) return chapterUrl
        if (chapterUrl.startsWith("//")) return "http:" + chapterUrl
        val arr = chapterUrl.split("/")
        if (arr.size > 1 && catalogUrl.contains(arr[1])) {
            return catalogUrl.substring(0, catalogUrl.indexOf(arr[1]) - 1) + chapterUrl
        }
        return catalogUrl.substring(0, catalogUrl.lastIndexOf("/")) + chapterUrl
    }

    @Throws(IOException::class)
    private fun getChapterWithSelector(url: String): String {
        val elements = Jsoup.connect(url).get().allElements
        val indexList = ArrayList<Element>()
        if (elements.size > 1) {
            for (i in 1..elements.size - 1) {
                if (elements[0].text().length > elements.get(i).text().length * 2) {
                    indexList.add(elements[i])
                }
            }
        }
        elements.removeAll(indexList)
        return elements.last().text()
    }
}