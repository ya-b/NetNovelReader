package com.netnovelreader.data.network

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.jsoup.Jsoup
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Created by yangbo on 18-1-14.
 * search("http://se.qidian.com/?kw=" + URLEncoder.encode(bookname, "utf-8"),".book-img-text > ul:nth-child(1) > li:nth-child(1)"))
 * search("http://www.yunlaige.com/modules/article/search.php?searchkey=" + URLEncoder.encode(bookname, "gbk") + "&action=login&submit=", "location", ".readnow", "li.clearfix:nth-child(1) > div:nth-child(2) > div:nth-child(1) > h2:nth-child(2) > a:nth-child(1)")
 */
class SearchBook : Cloneable{

    @Synchronized
    fun search(url: String, redirectFileld: String, redirectSelector: String, noRedirectSelector: String): String?{
        var result: String?
        if(redirectFileld.equals("")){
            search(url, noRedirectSelector)
        }
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.instanceFollowRedirects = false
        conn.setRequestProperty("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
        conn.setRequestProperty("user-agent", "Mozilla/5.0 (X11; Linux x86_64; rv:58.0) Gecko/20100101 Firefox/58.0")
        conn.setRequestProperty("Upgrade-Insecure-Requests", "1")
        val redirect_url = conn.getHeaderField(redirectFileld)
        if(redirect_url != null && redirect_url.length > 5){
            result = Jsoup.connect(redirect_url).get().select(redirectSelector).select("a").attr("href")
        }else{
            result = search(url, noRedirectSelector)
        }
        return result
    }

    @Synchronized
    fun search(url: String, noRedirectSelector: String): String? {
        val doc = Jsoup.connect(url).get().select(noRedirectSelector)
        var result = doc.select("a").attr("href")
        if(!result!!.contains("//")){
            result = url.substring(0, url.lastIndexOf('/') + 1) + result
        }else if(result!!.startsWith("//")){
            result = "http:" + result
        }
        if(result.contains("qidian.com")){
            result += "#Catalog"
        }
        return result
    }

    override fun clone(): SearchBook {
        return super.clone() as SearchBook
    }
}