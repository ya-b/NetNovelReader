package com.netnovelreader.repo.http.paging

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.netnovelreader.repo.http.resp.BookLinkResp
import java.io.File


class BookLinkTopClickDataSourceFactory(val cacheDir: File) :
    DataSource.Factory<String, BookLinkResp>() {
    val sourceLiveData = MutableLiveData<BookLinkTopClickDataSource>()

    override fun create(): DataSource<String, BookLinkResp> {
        val dataSource = BookLinkTopClickDataSource(cacheDir)
        sourceLiveData.postValue(dataSource)
        return dataSource
    }
}