package com.netnovelreader.repo.http

import com.netnovelreader.repo.db.SiteSelectorEntity
import com.netnovelreader.repo.http.resp.ChapterInfoResp
import com.netnovelreader.repo.http.resp.SearchBookResp
import io.reactivex.Single
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.net.URLEncoder
import java.util.regex.Pattern

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

    fun search(bookname: String, sitePreference: SiteSelectorEntity): Single<SearchBookResp> {
        val url = String.format(sitePreference.searchUrl, URLEncoder.encode(bookname, sitePreference.charset))
        return getDocument(url).map { doc ->
            if(parseBookname(doc, sitePreference.noRedirectName).isEmpty()) {
                SearchBookResp(
                    parseBookname(doc, sitePreference.redirectName),
                    parseCatalogUrl(doc, url, sitePreference.redirectUrl),
                    parseImageUrl(doc, sitePreference.redirectImage), ""
                )
            } else {
                SearchBookResp(
                    parseBookname(doc, sitePreference.noRedirectName),
                    parseCatalogUrl(doc, url, sitePreference.noRedirectUrl),
                    parseImageUrl(doc, sitePreference.noRedirectImage), ""
                )
            }
        }
    }

    /**
     * 解析章节
     */
    fun getChapterContent(url: String, selector: String) =
        if (selector.isEmpty() || selector.length < 2) {
            getChapterWithOutSelector(url)
        } else {
            getDocument(url).map { it.select(selector).text() }
        }

    /**
     * 解析目录
     */
    fun getCatalog(url: String, selector: String): Single<List<ChapterInfoResp>> =
        getDocument(url).map {
            val list = it.select(selector).select("a")
            val chapterList = ArrayList<ChapterInfoResp>()
            for (i in 0 until list.size) {
                val link = fixUrl(url, list[i].attr("href"))
                val name = list[i].text()
                val resp = ChapterInfoResp(0, name, link)
                if (chapterList.contains(resp)) {
                    chapterList.remove(resp)
                }
                chapterList.add(resp)
            }
            chapterList.apply {
                forEachIndexed { index, chapterInfoResp -> chapterInfoResp.id = index + 1 }
            }
        }

    /**
     * 解析章节,没有选择器
     */
    private fun getChapterWithOutSelector(url: String): Single<String> =
        getDocument(url).map {
            val elements = it.allElements ?: Elements()
            val indexList = ArrayList<Element>()
            if (elements.size > 1) {
                (1 until elements.size)
                    .filter { elements[0].text().length > elements[it].text().length * 2 }
                    .forEach { indexList.add(elements[it]) }
            }
            elements.removeAll(indexList)
            elements.last().text()
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
            url = "http:$url"
        }
        return url
    }

    private fun getDocument(url: String): Single<Document> =
        WebService.readerAPI
            .request(url)
            .map {
                val bytes = it.body()?.bytes()
                var document: Document? = null
                bytes?.inputStream()?.use {
                    document = Jsoup.parse(it, getCharset(bytes), url)
                }
                document
            }

    private fun getCharset(bytes: ByteArray): String {
        val matcher = Pattern.compile(
            ".*?<meta(?!\\s*(?:name|value)\\s*=)[^>]*?charset\\s*=[\\s\"']*([^\\s\"'/>]*)",
            Pattern.CASE_INSENSITIVE
        ).matcher(String(bytes))
        return if (matcher.find()) matcher.group(1) else "utf-8"
    }

    //对不合法url修复
    private fun fixUrl(referenceUrl: String, fixUrl: String): String {
        val str = if (fixUrl.startsWith("/")) fixUrl else "/$fixUrl"
        val arr = str.split("/")
        return when {
            fixUrl.isEmpty() -> ""
            fixUrl.startsWith("http") -> fixUrl
            fixUrl.startsWith("//") -> "http:$fixUrl"
            arr.size < 2 -> referenceUrl.substring(0, referenceUrl.lastIndexOf("/")) + str
            referenceUrl.contains(arr[1]) -> referenceUrl.substring(
                0,
                referenceUrl.indexOf(arr[1]) - 1
            ) + str
            else -> referenceUrl.substring(0, referenceUrl.lastIndexOf("/")) + str
        }
    }
}