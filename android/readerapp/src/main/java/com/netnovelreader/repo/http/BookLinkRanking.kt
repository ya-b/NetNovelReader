package com.netnovelreader.repo.http

import com.netnovelreader.repo.http.resp.BookLinkResp
import org.jsoup.Jsoup
import java.io.IOException

class BookLinkRanking {
    private val url1 = "https://booklink.me/top_click.php?page_id=%d"
    private val url2 = "https://2.booklink.me/top_click.php?page_id=%d"

    fun getRanking(pageNum: Int, pageSize: Int): List<BookLinkResp> =
        try {
            WebService.readerAPI.request(String.format(url1, pageNum)).execute().body()?.string()
        } catch (e: IOException) {
            WebService.readerAPI.request(String.format(url2, pageNum)).execute().body()?.string()
        }
            ?.let {
                Jsoup.parse(it).select(
                    "body > div:nth-child(1) > table:nth-child(5) > " +
                            "tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > table:nth-child(5) > " +
                            "tbody:nth-child(1) > tr"
                )
            }
            ?.map {
                val bookname = it.select("td:nth-child(2)").text()
                val author = it.select("td:nth-child(7)").text()
                val latestChapter = it.select("td:nth-child(5)").text()
                val updateTime = it.select("td:nth-child(5) > a")
                    .attr("title")
                    .toString()
                    .takeIf { it.length > 5 }
                    ?.substring(5)
                BookLinkResp(bookname, author, latestChapter, updateTime ?: "")
            }
            ?.filter { !it.bookname.equals("章节目录") } ?: emptyList()
}