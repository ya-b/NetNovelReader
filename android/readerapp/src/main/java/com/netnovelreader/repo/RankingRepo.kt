package com.netnovelreader.repo

import android.app.Application
import android.arch.paging.DataSource
import android.arch.paging.PageKeyedDataSource
import com.netnovelreader.repo.http.WebService
import com.netnovelreader.repo.http.resp.BookLinkResp
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class RankingRepo(app: Application) : Repo(app) {

    fun getDataSourceFactory() = BookLinkTopClickDataSourceFactory(app.cacheDir)

    class BookLinkTopClickDataSourceFactory(val cacheDir: File) :
        DataSource.Factory<String, BookLinkResp>() {
        override fun create(): DataSource<String, BookLinkResp> {
            return BookLinkTopClickDataSource(cacheDir)
        }
    }

    class BookLinkTopClickDataSource(val cacheDir: File) : PageKeyedDataSource<String, BookLinkResp>() {
        val cacheName = "rank%s"

        override fun loadInitial(
            params: LoadInitialParams<String>,
            callback: LoadInitialCallback<String, BookLinkResp>
        ) {
            val result = readObject("1")
                    ?: WebService.bookLinkRanking
                        .getRanking(1, params.requestedLoadSize)
                        .also { writeObject("1", it) }
            val list = arrayListOf(BookLinkResp("", "", "", ""),
                *result.toTypedArray())
            callback.onResult(list, null, "2")
        }

        override fun loadAfter(
            params: LoadParams<String>,
            callback: LoadCallback<String, BookLinkResp>
        ) {
            val list = readObject(params.key)
                    ?: WebService.bookLinkRanking
                        .getRanking(params.key.toInt(), params.requestedLoadSize)
                        .also { writeObject(params.key, it) }
            val nextKey = params.key.toInt() + 1
            callback.onResult(list, if(nextKey == 51) null else nextKey.toString())
        }

        override fun loadBefore(
            params: LoadParams<String>,
            callback: LoadCallback<String, BookLinkResp>
        ) {
            val list = readObject(params.key)
                    ?: WebService.bookLinkRanking
                        .getRanking(params.key.toInt(), params.requestedLoadSize)
                        .also { writeObject(params.key, it) }
            val previousKey = params.key.toInt() - 1
            callback.onResult(list, if(previousKey == 0) null else previousKey.toString())
        }

        @Suppress("UNCHECKED_CAST")
        fun readObject(pageNum: String): List<BookLinkResp>? {
            val file = File(cacheDir, String.format(cacheName, pageNum))
            if(!file.exists()) return null
            var list: List<BookLinkResp>? = null
            ObjectInputStream(file.inputStream())
                .use { list = it.readObject() as List<BookLinkResp> }
            return list
        }

        fun writeObject(pageNum: String, list: List<BookLinkResp>) {
            val file = File(cacheDir, String.format(cacheName, pageNum))
            if(file.exists()) file.delete()
            ObjectOutputStream(file.outputStream()).use { it.writeObject(list) }
        }
    }
}