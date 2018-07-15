package com.netnovelreader.repo

import android.app.Application
import com.netnovelreader.repo.db.ReaderDatabase
import com.netnovelreader.repo.http.WebService
import com.netnovelreader.repo.http.resp.ChapterInfoResp
import com.netnovelreader.repo.http.resp.SearchBookResp
import com.netnovelreader.utils.url2Hostname
import java.io.IOException

abstract class Repo(var app: Application) {
    val db = ReaderDatabase.getInstance(app)

    @Throws(IOException::class)
    fun getChapter(chapterInfoResp: ChapterInfoResp): String {
        val selector = db.siteSelectorDao().getItem(url2Hostname(chapterInfoResp.chapterUrl))
        var result = WebService.searchBook
            .getChapterContent(chapterInfoResp.chapterUrl, selector?.chapterSelector ?: "")
            .replace(" ", "\n\n  ")
        val arr = db.siteSelectorDao().getItem(url2Hostname(chapterInfoResp.chapterUrl))
            ?.chapterFilter
            ?.split("|")
        arr?.forEach {
            result = result.replace(it, "")
        }
        return result
    }

    @Throws(IOException::class)
    fun getCatalog(item: SearchBookResp): List<ChapterInfoResp> {
        val selector = db.siteSelectorDao().getItem(url2Hostname(item.url))
        val chapterList =
            WebService.searchBook.getCatalog(item.url, selector?.catalogSelector ?: "")
        //过滤
        val arr = db.siteSelectorDao().getItem(url2Hostname(item.url))?.catalogFilter?.split("|")
        val removeList = ArrayList<ChapterInfoResp>()
        arr?.forEach { filter ->
            chapterList.filter { it.chapterName == filter }
                .let { removeList.addAll(it) }
        }
        chapterList.removeAll(removeList)
        chapterList.forEachIndexed { index, chapterInfoResp -> chapterInfoResp.id = index + 1 }
        return chapterList
    }
}