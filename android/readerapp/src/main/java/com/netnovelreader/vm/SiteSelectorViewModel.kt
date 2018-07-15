package com.netnovelreader.vm

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import com.netnovelreader.repo.SiteSelectorRepo

class SiteSelectorViewModel(val repo: SiteSelectorRepo, app: Application) : AndroidViewModel(app) {
    val allSiteSelector = LivePagedListBuilder(
        repo.allSiteSelectors,
        PagedList.Config.Builder()
            .setPageSize(30)
            .setEnablePlaceholders(false)
            .build()
    ).build()


    fun updatePreference(perferNet: Boolean) {
        repo.getSelectorsFromNet { netList ->
            repo.getSelectorSFromLocal { localList ->
                if(perferNet) {
                    netList.forEach { item ->
                        localList.firstOrNull { item.hostname == it.hostname }
                            ?.let { item._id = it._id }
                    }
                    repo.saveAll(netList)
                } else {
                    val list = netList.filter { item ->
                        localList.none { item.hostname == it.hostname }
                    }
                    repo.saveAll(list)
                }
            }
        }
    }
}