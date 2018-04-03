package com.netnovelreader.service

import com.netnovelreader.dao.SitePreferenceDao
import com.netnovelreader.model.SitePreferenceBean
import com.netnovelreader.utils.*
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.newFixedThreadPoolContext
import kotlinx.coroutines.experimental.runBlocking
import org.jsoup.Jsoup
import org.jsoup.UncheckedIOException
import org.jsoup.nodes.Element
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLEncoder

class SearchBookService {
    val threadPoolDispatcher = newFixedThreadPoolContext(10, "appPoolContext")

    fun search(bookname: String?): ArrayList<Array<String>> = runBlocking {
        val resultList = ArrayList<Array<String>>()
        if(bookname.isNullOrEmpty()) return@runBlocking resultList
        val siteList = SitePreferenceDao().getAllPreference()
        if(siteList == null || siteList.isEmpty()) return@runBlocking resultList
        siteList.map {
            async(threadPoolDispatcher) {
                try {
                    searchBook(bookname!!, it)
                }catch (e: IOException){
                    e.printStackTrace()
                    null
                }
            }
        }.map { it.await() }.forEach { it?.let { resultList.add(it) } }
        return@runBlocking  resultList
    }

    @Throws(IOException::class)
    private fun searchBook(bookname: String, sitePreference: SitePreferenceBean): Array<String>? {
        return sitePreference.run {
            val url = search_url?.replace(SEARCH_NAME, URLEncoder.encode(bookname, charset)) ?: return null
            println("url====$url|||||bookname=$bookname|||||charset=$charset")
            if (redirect_fileld.isNullOrEmpty()) {
                searchBook(url, no_redirect_url!!, no_redirect_name!!, no_redirect_image!!)
            } else {
                val redirect_url = redirectToCatalog(url, redirect_fileld!!)
                searchBook(redirect_url, this.redirect_url!!, redirect_name!!, redirect_image!!)
            }
        }
    }

    @Throws(IOException::class)
    private fun searchBook(url: String, catalogSelector: String, nameSelector: String, imageSelector: String)
            : Array<String>? {
        val doc = try {
            Jsoup.connect(url).headers(getHeaders(url)).timeout(TIMEOUT).get()
        } catch (e: UncheckedIOException) {
            return null
        }catch (e: SocketTimeoutException){
            return null
        }
        return arrayOf(
            parseCatalogUrl(doc, url, catalogSelector), parseBookname(doc, nameSelector),
            parseImageUrl(doc, imageSelector)
        )
    }

    @Throws(IOException::class)
    private fun redirectToCatalog(url: String, redirectFileld: String): String {
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

    //对不合法url修复
    private fun fixUrl(referenceUrl: String, fixUrl: String): String {
        if (fixUrl.isEmpty()) return ""
        if (fixUrl.startsWith("http")) return fixUrl
        if (fixUrl.startsWith("//")) return "http:" + fixUrl
        val str = if (fixUrl.startsWith("/")) fixUrl else "/" + fixUrl
        val arr = str.split("/")
        if (arr.size < 2) return referenceUrl.substring(0, referenceUrl.lastIndexOf("/")) + str
        if (referenceUrl.contains(arr[1]))
            return referenceUrl.substring(0, referenceUrl.indexOf(arr[1]) - 1) + str
        return referenceUrl.substring(0, referenceUrl.lastIndexOf("/")) + str
    }
}