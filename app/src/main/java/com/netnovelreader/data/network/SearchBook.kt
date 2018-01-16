package com.netnovelreader.data.network

import com.netnovelreader.data.database.SearchSQLManager
import com.netnovelreader.utils.TIMEOUT
import com.netnovelreader.utils.UA
import com.netnovelreader.utils.url2Hostname
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.jsoup.Jsoup
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Created by yangbo on 18-1-14.
 * search("http://se.qidian.com/?kw=" + URLEncoder.encode(tablename, "utf-8"),".book-img-text > ul:nth-child(1) > li:nth-child(1)"))
 * search("http://www.yunlaige.com/modules/article/search.php?searchkey=" + URLEncoder.encode(tablename, "gbk") + "&action=login&submit=", "location", ".readnow", "li.clearfix:nth-child(1) > div:nth-child(2) > div:nth-child(1) > h2:nth-child(2) > a:nth-child(1)")
 */
class SearchBook : Cloneable{

    @Throws(ConnectException::class)
    fun search(url: String, redirectFileld: String, redirectSelector: String, noRedirectSelector: String, redirectName: String, noRedirectName: String): String?{
        var result: String?
        if(redirectFileld.equals("")){
            search(url, noRedirectSelector, noRedirectName)
        }
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.instanceFollowRedirects = false
        conn.setRequestProperty("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
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
    fun search(url: String, noRedirectSelector: String, noRedirectName: String): String? {
        val doc = Jsoup.connect(url).userAgent(UA)
                .timeout(TIMEOUT).get()
        var result = doc.select(noRedirectSelector).select("a").attr("href")
        if(!result!!.contains("//")){
            result = url.substring(0, url.lastIndexOf('/') + 1) + result
        }else if(result.startsWith("//")){
            result = "http:" + result
        }
        if(result.contains("qidian.com")){
            result += "#Catalog"
        }
        result = doc.select(noRedirectName).text() + "~~~" + result
        return result
    }

    override fun clone(): SearchBook {
        return super.clone() as SearchBook
    }
}