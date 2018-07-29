package com.netnovelreader.vm

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import com.netnovelreader.repo.RankingRepo

class RankingViewModel(val repo: RankingRepo, app: Application) : AndroidViewModel(app) {
    val ranking = LivePagedListBuilder(
        repo.getDataSourceFactory(),
        PagedList.Config.Builder()
            .setPageSize(20)
            .setEnablePlaceholders(false)
            .build()
    ).build()
    val searchCommand = MutableLiveData<StringBuilder>()

    fun goToSearch(bookname: String) {
        searchCommand.value = StringBuilder(bookname)
    }
}