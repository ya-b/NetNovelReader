package com.netnovelreader.data.network

import com.netnovelreader.common.fixUrl
import com.netnovelreader.common.getHeaders
import com.netnovelreader.data.local.db.ReaderDatabase
import com.netnovelreader.data.local.db.SitePreferenceBean
import org.jsoup.Jsoup
import org.jsoup.UncheckedIOException
import org.jsoup.nodes.Element
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class SearchBook {

    /**
     * url
     * redirectFileld
     * redirectUrl    目录地址selector
     * noRedirectUrl
     * redirectName        书名selector
     * noRedirectName
     * redirectImage      图片网址 selector
     * noRedirectImage
     * 有些网站搜索到书名后，响应头例如Location：http://www.yunlaige.com/book/19984.html，然后跳转到书籍页,redirectFileld表示响应头跳转链接
     * 有些网站搜索到书名后，显示搜索列表,
     * Selector  jsoup选择结果页目录url
     * Name  jsoup选择结果页书名
     */

    @Throws(IOException::class)
    fun search(bookname: String, sitePreference: SitePreferenceBean): Array<String> {
        return sitePreference.run {
            val url = searchUrl.replace(ReaderDatabase.SEARCH_NAME, URLEncoder.encode(bookname, charset))

            if (redirectFileld == "") {
                search(url, noRedirectUrl, noRedirectName, noRedirectImage)
            } else {
                val redirect_url = redirectToCatalog(url, redirectFileld)
                search(redirect_url, redirectUrl, redirectName, redirectImage)
            }
        }
    }

    @Throws(IOException::class)
    fun search(url: String, catalogSelector: String, nameSelector: String, imageSelector: String)
            : Array<String> {
        val doc = try {
            Jsoup.connect(url).headers(getHeaders(url)).timeout(3000).get()
        } catch (e: UncheckedIOException) {
            return Array(3) { "" }
        }
        return arrayOf(
                parseCatalogUrl(doc, url, catalogSelector), parseBookname(doc, nameSelector),
                parseImageUrl(doc, imageSelector)
        )
    }

    @Throws(IOException::class)
    fun redirectToCatalog(url: String, redirectFileld: String): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.instanceFollowRedirects = false
        val header = getHeaders(url)
        conn.setRequestProperty("accept", header["accept"])
        conn.setRequestProperty("user-agent", header["user-agent"])
        conn.setRequestProperty("Upgrade-Insecure-Requests", header["Upgrade-Insecure-Requests"])
        conn.setRequestProperty("Connection", "keep-alive")
        conn.setRequestProperty("Referer", header["Referer"])
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
        if (nameSelector == "") return ""
        var name = doc.select(nameSelector).text()
        if (name.isNullOrEmpty()) {
            name = doc.select(nameSelector).attr("title")
        }
        return name
    }


    private fun parseImageUrl(doc: Element, imageSelector: String): String {
        if (imageSelector == "") return ""
        var url = doc.select(imageSelector).attr("src")
        if (url.startsWith("//")) {
            url = "http:" + url
        }
        return url
    }
}