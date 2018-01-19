package com.netnovelreader.data.network

import android.util.Log
import com.netnovelreader.utils.TIMEOUT
import com.netnovelreader.utils.UA
import com.netnovelreader.utils.getHeaders
import com.netnovelreader.utils.url2Hostname
import org.jsoup.Jsoup
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Created by yangbo on 18-1-14.
 */
class SearchBook : Cloneable{

    /**
     * @url
     * @redirectFileld
     * @redirectSelector
     * @noRedirectSelector
     * @redirectName
     * @noRedirectName
     * 有些网站搜索到书名后，响应头例如Location：http://www.yunlaige.com/book/19984.html，然后跳转到书籍页,redirectFileld表示响应头跳转链接
     * 有些网站搜索到书名后，显示搜索列表,
     * Selector  jsoup选择结果页目录url
     * Name  jsoup选择结果页书名
     */
    @Throws(ConnectException::class)
    fun search(url: String, redirectFileld: String, redirectSelector: String, noRedirectSelector: String, redirectName: String, noRedirectName: String): String?{
        var result: String?
        if(redirectFileld.equals("")){
            search(url, noRedirectSelector, noRedirectName)
        }
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.instanceFollowRedirects = false
        conn.setRequestProperty("accept", "indicator/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
        conn.setRequestProperty("user-agent", UA)
        conn.setRequestProperty("Upgrade-Insecure-Requests", "1")
        conn.setRequestProperty("Connection","keep-alive")
        conn.setRequestProperty("Referer", "http://www.${url2Hostname(url)}/")
        val redirect_url = conn.getHeaderField(redirectFileld)
        conn.disconnect()
        if(redirect_url != null && redirect_url.length > 5){
            result = search(url, redirectSelector, redirectName)
        }else{
            result = search(url, noRedirectSelector, noRedirectName)
        }
        return result
    }

    @Throws(ConnectException::class)
    fun search(url: String, selector: String, name: String): String? {
        val doc = Jsoup.connect(url).headers(getHeaders(url))
                .timeout(TIMEOUT).get()
        var result = doc.select(selector).select("a").attr("href")
        if(!result!!.contains("//")){
            result = url.substring(0, url.lastIndexOf('/') + 1) + result
        }else if(result.startsWith("//")){
            result = "http:" + result
        }
        if(result.contains("qidian.com")){
            result += "#Catalog"
        }
        result = doc.select(name).text() + "~~~" + result
        return result
    }

    override fun clone(): SearchBook {
        return super.clone() as SearchBook
    }
}