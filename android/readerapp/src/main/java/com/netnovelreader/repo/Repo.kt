package com.netnovelreader.repo

import android.app.Application
import com.netnovelreader.repo.db.ReaderDatabase
import com.netnovelreader.repo.http.WebService
import com.netnovelreader.repo.http.resp.ChapterInfoResp
import com.netnovelreader.repo.http.resp.SearchBookResp
import com.netnovelreader.utils.IO_EXECUTOR
import com.netnovelreader.utils.url2Hostname
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

abstract class Repo(val app: Application) {
    val db = ReaderDatabase.getInstance(app)

    fun getChapter(chapterInfoResp: ChapterInfoResp): Single<String> =
        db.siteSelectorDao()
            .getItem(url2Hostname(chapterInfoResp.chapterUrl))
            .subscribeOn(Schedulers.from(IO_EXECUTOR))
            .flatMap { entity ->
                WebService.searchBook
                    .getChapterContent(
                        chapterInfoResp.chapterUrl,
                        entity.chapterSelector
                    ).onErrorReturn { "" }
                    .map { str ->
                        var result = str.replace(Regex("[\\s　]+"), "\n\n    ")
                        entity.chapterFilter.split("|")
                            .forEach { result = result.replace(Regex(it), "") }
                        result
                    }
            }

    fun getCatalog(item: SearchBookResp): Single<List<ChapterInfoResp>> =
        db.siteSelectorDao()
            .getItem(url2Hostname(item.url))
            .subscribeOn(Schedulers.from(IO_EXECUTOR))
            .flatMap { selector ->
                WebService.searchBook
                    .getCatalog(item.url, selector.catalogSelector)
                    .onErrorReturn { emptyList() }
                    .map { chapterList ->
                        //过滤
                        selector.catalogFilter.split("|").let { filterList ->
                            chapterList.filter {
                                filterList.none { p -> p.isNotEmpty() && it.chapterName.contains(p) }
                            }.apply {
                                forEachIndexed { index, chapterInfoResp ->
                                    chapterInfoResp.id = index + 1
                                }
                            }
                        }
                    }
            }
}