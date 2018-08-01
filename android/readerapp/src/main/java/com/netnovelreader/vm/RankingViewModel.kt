package com.netnovelreader.vm

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import com.netnovelreader.repo.RankingRepo

class RankingViewModel(val repo: RankingRepo, app: Application) : AndroidViewModel(app) {
    val sourceFactory = repo.getDataSourceFactory()
    val ranking = LivePagedListBuilder(
        sourceFactory,
        PagedList.Config.Builder()
            .setPageSize(20)
            .setEnablePlaceholders(false)
            .build()
    ).build()
    val networkState = Transformations.switchMap(sourceFactory.sourceLiveData, { it.networkState })
    val searchCommand = MutableLiveData<StringBuilder>()

    fun goToSearch(bookname: String) {
        searchCommand.value = StringBuilder(bookname)
    }

    fun retry() {
        sourceFactory.sourceLiveData.value?.retryFailed()
    }
}