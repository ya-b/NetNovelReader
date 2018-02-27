package com.netnovelreader.data.network

import com.netnovelreader.common.TIMEOUT
import com.netnovelreader.common.fixUrl
import com.netnovelreader.common.getHeaders
import com.netnovelreader.common.url2Hostname
import com.netnovelreader.data.db.ReaderDbManager
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
        val selector = ReaderDbManager.getRoomDB().sitePreferenceDao().getRule(url2Hostname(url)).chapterSelector
        val txt = if (selector.isEmpty() || selector.length < 2) {
            getChapterWithOutSelector(url)
        } else {
            Jsoup.connect(url).headers(getHeaders(url))
                .timeout(TIMEOUT).get().select(selector).text()
        }
        return "    " + txt!!.replace(" ", "\n\n  ")
    }

    /**
     * 解析目录
     */
    @Throws(IOException::class)
    fun getCatalog(url: String): LinkedHashMap<String, String> {
        val selector = ReaderDbManager.getRoomDB().sitePreferenceDao().getRule(url2Hostname(url)).catalogSelector
        val catalog = LinkedHashMap<String, String>()
        val list =
            Jsoup.connect(url).headers(getHeaders(url)).timeout(TIMEOUT).get().select(selector)
                .select("a")

        //Logger.i("解析的目录网页来源为：【$url】,元素选择器为：【$selector】")
        list.forEach {
            val link = fixUrl(url, it.attr("href"))
            val name = it.text()
            if (catalog.containsKey(name)) {
                catalog.remove(name)
            }
            catalog.put(name, link)
        }
        return catalog
    }

    /**
     * 解析章节,没有选择器
     */
    @Throws(IOException::class)
    private fun getChapterWithOutSelector(url: String): String {
        val elements = Jsoup.connect(url).get().allElements
        val indexList = ArrayList<Element>()
        if (elements.size > 1) {
            (1 until elements.size)
                .filter { elements[0].text().length > elements[it].text().length * 2 }
                .forEach { indexList.add(elements[it]) }
        }
        elements.removeAll(indexList)
        return elements.last().text()
    }
}