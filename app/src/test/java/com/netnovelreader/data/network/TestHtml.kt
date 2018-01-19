package com.netnovelreader.data.network

import com.netnovelreader.data.SearchBook
import org.junit.Test
import java.net.URLEncoder

/**
 * Created by yangbo on 18-1-14.
 */
class TestHtml {
    @Test
    fun testSearch(){
        val searchBook = SearchBook()
        var url = "http://se.qidian.com/?kw=" + URLEncoder.encode("极道天魔", "utf-8")
        var r1 = searchBook.search(url,".book-img-text > ul:nth-child(1) > li:nth-child(1) > div:nth-child(2) > h4:nth-child(1) > a:nth-child(1)",".book-img-text > ul:nth-child(1) > li:nth-child(1) > div:nth-child(2) > h4:nth-child(1) > a:nth-child(1)")
//        var r1 = searchBook.search("http://www.yunlaige.com/modules/article/search.php?searchkey=" + URLEncoder.encode("极道天魔", "gbk") + "&action=login&submit=",
//                "location",
//                ".readnow",
//                "li.clearfix:nth-child(1) > div:nth-child(2) > div:nth-child(1) > h2:nth-child(2) > a:nth-child(1)",
//                "#content > div.book-info > div.info > h2 > a",
//                "li.clearfix:nth-child(1) > div:nth-child(2) > div:nth-child(1) > h2:nth-child(1) > a:nth-child(1)")
//        var r1 = searchBook.search("http://www.yunlaige.com/modules/article/search.php?searchkey=" + URLEncoder.encode("极道天魔", "gbk") + "&action=login&submit=", ".readnow", "#content > div.book-info > div.info > h2 > a")
        println(r1)

    }

    @Test
    fun testGetChapter(){
//        println(ParseHtml().getChapter("http://www.yunlaige.com/html/9/9313/4641557.html",
//                "#content"))
    }

    @Test
    fun testGetCatalog(){
//        var map = ParseHtml().getCatalog("http://www.yunlaige.com/html/9/9313/index.html",
//                "#contenttable")
    }
}