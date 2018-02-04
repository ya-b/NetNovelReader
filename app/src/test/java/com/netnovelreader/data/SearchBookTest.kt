package com.netnovelreader.data

import com.netnovelreader.common.TIMEOUT
import com.netnovelreader.common.getHeaders
import org.jsoup.Jsoup
import org.junit.Test

/**
 * Created by yangbo on 18-1-28.
 */
class SearchBookTest {
    @Test
    fun search() {
        val url = "http://www.b5200.net/modules/article/search.php?searchkey=a"
        val doc = Jsoup.connect(url).headers(getHeaders(url)).timeout(TIMEOUT).get()

        println(doc.select(""))
    }

    @Test
    fun search1() {

        SearchBook().search(
            "http://www.b5200.net/modules/article/search.php?searchkey=a",
            "td.odd:nth-child(1) > a:nth-child(1)",
            "td.odd:nth-child(1) > a:nth-child(1)",
            ""
        )
            .forEach { println(it) }

    }

    @Test
    fun parseCatalogUrl() {
        val p = Jsoup.connect("http://www.b5200.net/2_2598/")
            .headers(getHeaders("http://www.b5200.net/2_2598/"))
            .timeout(TIMEOUT).get().select("#list > dl:nth-child(1)").select("a")
        println(p)
    }

    @Test
    fun parseBookname() {
    }

    @Test
    fun parseImageUrl() {
    }

}