package com.netnovelreader.repo

import android.app.Application
import com.netnovelreader.repo.http.paging.BookLinkTopClickDataSourceFactory

class RankingRepo(app: Application) : Repo(app) {

    fun getDataSourceFactory() = BookLinkTopClickDataSourceFactory(app.cacheDir)

}