package com.netnovelreader.data

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
        SearchBook().search("https://www.qidian.com/search/?kw=%E6%9E%81%E9%81%93%E5%A4%A9%E9%AD%94",
                ".book-img-text > ul:nth-child(1) > li:nth-child(1) > div:nth-child(2) > h4:nth-child(1) > a:nth-child(1)",
                ".book-img-text > ul:nth-child(1) > li:nth-child(1) > div:nth-child(2) > h4:nth-child(1) > a:nth-child(1)",
                ".book-img-text > ul:nth-child(1) > li:nth-child(1) > div:nth-child(1) > a:nth-child(1) > img:nth-child(1)")
                .forEach { println(it) }
    }

    @Test
    fun parseCatalogUrl() {
    }

    @Test
    fun parseBookname() {
    }

    @Test
    fun parseImageUrl() {
    }

}