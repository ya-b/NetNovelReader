package com.netnovelreader.common.data

import com.netnovelreader.common.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Created by yangbo on 18-1-14.
 */
class SearchBook : Cloneable {

    /**
     * @url
     * @redirectFileld
     * @redirectSelector    目录地址selector
     * @noRedirectSelector
     * @redirectName        书名selector
     * @noRedirectName
     * @redirectImage imageurl selector
     * @noRedirectImage
     * 有些网站搜索到书名后，响应头例如Location：http://www.yunlaige.com/book/19984.html，然后跳转到书籍页,redirectFileld表示响应头跳转链接
     * 有些网站搜索到书名后，显示搜索列表,
     * Selector  jsoup选择结果页目录url
     * Name  jsoup选择结果页书名
     */
    @Throws(IOException::class)
    fun search(
            url: String, redirectFileld: String, redirectSelector: String, noRedirectSelector: String,
            redirectName: String, noRedirectName: String, redirectImage: String, noRedirectImage: String
    )
            : Array<String> {
        if (redirectFileld.equals("")) {
            return search(url, noRedirectSelector, noRedirectName, noRedirectImage)
        }
        val redirect_url = redirectToCatalog(url, redirectFileld)
        return if (redirect_url.isEmpty()) {
            search(url, noRedirectSelector, noRedirectName, noRedirectImage)
        } else {
            search(redirect_url, redirectSelector, redirectName, redirectImage)
        }
    }

    @Throws(IOException::class)
    fun search(url: String, catalogSelector: String, nameSelector: String, imageSelector: String)
            : Array<String> {
        val doc = Jsoup.connect(url).headers(getHeaders(url)).timeout(TIMEOUT).get()
        val arr = arrayOf(
            parseCatalogUrl(doc, url, catalogSelector), parseBookname(doc, nameSelector),
            parseImageUrl(doc, imageSelector)
        )
        return arr
    }

    @Throws(IOException::class)
    fun redirectToCatalog(url: String, redirectFileld: String): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.instanceFollowRedirects = false
        conn.setRequestProperty(
            "accept",
            "indicator/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
        )
        conn.setRequestProperty("user-agent", UA)
        conn.setRequestProperty("Upgrade-Insecure-Requests", "1")
        conn.setRequestProperty("Connection", "keep-alive")
        conn.setRequestProperty("Referer", "http://www.${url2Hostname(url)}/")
        var redirect_url = conn.getHeaderField(redirectFileld)
        if (redirect_url != null) {
            redirect_url = fixUrl(url, redirect_url)
        }
        conn.disconnect()
        return if (redirect_url.isNullOrEmpty()) {
            return url
        } else {
            redirectToCatalog(redirect_url, redirectFileld)
        }
    }

    private fun parseCatalogUrl(doc: Element, url: String, urlSelector: String): String {
        var result = doc.select(urlSelector).select("a").attr("href")
        result = fixUrl(url, result)
        if (result.contains("qidian.com")) {
            result += "#Catalog"
        }
        return result
    }


    private fun parseBookname(doc: Element, nameSelector: String): String {
        if (nameSelector.equals("")) return ""
        var name = doc.select(nameSelector).text()
        if (name.isNullOrEmpty()) {
            name = doc.select(nameSelector).attr("title")
        }
        return name
    }


    private fun parseImageUrl(doc: Element, imageSelector: String): String {
        if (imageSelector.equals("")) return ""
        var url = doc.select(imageSelector).attr("src")
        if (url.startsWith("//")) {
            url = "http:" + url
        }
        return url
    }

    override fun clone(): SearchBook {
        return super.clone() as SearchBook
    }
}