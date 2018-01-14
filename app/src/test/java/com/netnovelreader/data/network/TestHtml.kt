package com.netnovelreader.data.network

import org.junit.Test
import java.net.URLEncoder

/**
 * Created by yangbo on 18-1-14.
 */
class TestHtml {
    @Test
    fun testSearch(){
        val searchBook = SearchBook()
        var r1 = searchBook.search("http://se.qidian.com/?kw=" + URLEncoder.encode("极道天魔", "utf-8"),".book-img-text > ul:nth-child(1) > li:nth-child(1)")
        var r2 = searchBook.search("http://se.qidian.com/?kw=" + URLEncoder.encode("极道天魔", "gbk"),".book-img-text > ul:nth-child(1) > li:nth-child(1)")
        var r3 = searchBook.search("http://www.yunlaige.com/modules/article/search.php?searchkey=" + URLEncoder.encode("极道天魔", "utf-8") + "&action=login&submit=", "location", ".readnow", "li.clearfix:nth-child(1) > div:nth-child(2) > div:nth-child(1) > h2:nth-child(2) > a:nth-child(1)")
        var r4 = searchBook.search("http://www.yunlaige.com/modules/article/search.php?searchkey=" + URLEncoder.encode("极道天魔", "gbk") + "&action=login&submit=", "location", ".readnow", "li.clearfix:nth-child(1) > div:nth-child(2) > div:nth-child(1) > h2:nth-child(2) > a:nth-child(1)")
        println(r1)
        println(r2)
        println(r3)
        println(r4)
    }

    @Test
    fun testGetChapter(){
//        println(ParseHtml().getChapter("http://www.yunlaige.com/html/9/9313/4641557.html",
//                "#content"))
        println(ParseHtml().getChapter("https://read.qidian.com/chapter/9r9u8W1evJUCpOPIBxLXdQ2/EKq5eZhajBtMs5iq0oQwLQ2",
                ".read-content"))
    }

    @Test
    fun testGetCatalog(){
//        var map = ParseHtml().getCatalog("http://www.yunlaige.com/html/9/9313/index.html",
//                "#contenttable")
        var map = ParseHtml().getCatalog("https://book.qidian.com/info/1010191960#Catalog",
                ".volume-wrap")
        var iterator = map.iterator()
        while (iterator.hasNext()){
            var entry = iterator.next()
            println(entry.key + "-------------" + entry.value)
        }
    }
}