package com.netnovelreader.data

import com.netnovelreader.common.TIMEOUT
import com.netnovelreader.common.getHeaders
import org.jsoup.Jsoup
import org.junit.Test

import org.junit.Assert.*

/**
 * Created by yangbo on 18-1-28.
 */
class SearchBookTest {
    @Test
    fun search() {
    }

    @Test
    fun search1() {
        SearchBook().search("http://www.b5200.net/modules/article/search.php?searchkey=%E9%9B%AA%E9%B9%B0%E9%A2%86%E4%B8%BB",
                "td.odd:nth-child(1) > a:nth-child(1)",
                "td.odd:nth-child(1) > a:nth-child(1)",
                "")
                .forEach { println(it) }
    }

    @Test
    fun parseCatalogUrl() {
        val p = Jsoup.connect("http://www.b5200.net/2_2598/").headers(getHeaders("http://www.b5200.net/2_2598/"))
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