package com.netnovelreader.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
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