package com.netnovelreader.repo

import android.app.Application
import com.netnovelreader.repo.db.ReaderDatabase
import com.netnovelreader.repo.http.WebService
import com.netnovelreader.repo.http.resp.ChapterInfoResp
import com.netnovelreader.repo.http.resp.SearchBookResp
import com.netnovelreader.utils.IO_EXECUTOR
import com.netnovelreader.utils.url2Hostname
import io.reactivex.Single
import io.reactivex.SingleSource
import io.reactivex.schedulers.Schedulers
import java.io.IOException

abstract class Repo(val app: Application) {
    val db = ReaderDatabase.getInstance(app)

    @Throws(IOException::class)
    fun getChapter(chapterInfoResp: ChapterInfoResp): Single<String> =
        db.siteSelectorDao()
            .getItem(url2Hostname(chapterInfoResp.chapterUrl))
            .subscribeOn(Schedulers.from(IO_EXECUTOR))
            .flatMap { entity ->
                SingleSource<String> {
                    var result = WebService.searchBook
                        .getChapterContent(
                            chapterInfoResp.chapterUrl,
                            entity.chapterSelector
                        )
                        .replace(" ", "\n\n  ")
                    entity.chapterFilter.split("|")
                        .forEach { result = result.replace(it, "") }
                    it.onSuccess(result)
                }
            }

    @Throws(IOException::class)
    fun getCatalog(item: SearchBookResp): Single<List<ChapterInfoResp>> =
        db.siteSelectorDao()
            .getItem(url2Hostname(item.url))
            .subscribeOn(Schedulers.from(IO_EXECUTOR))
            .flatMap { selector ->
                SingleSource<List<ChapterInfoResp>> {
                    val chapterList =
                        WebService.searchBook.getCatalog(item.url, selector.catalogSelector)
                    //过滤
                    val arr = selector.catalogFilter.split("|")
                    val removeList = ArrayList<ChapterInfoResp>()
                    arr.forEach { filter ->
                        chapterList.filter { filter.isNotEmpty() && it.chapterName.contains(filter) }
                            .let { removeList.addAll(it) }
                    }
                    chapterList.removeAll(removeList)
                    chapterList.forEachIndexed { index, chapterInfoResp ->
                        chapterInfoResp.id = index + 1
                    }
                    it.onSuccess(chapterList)
                }
            }
}