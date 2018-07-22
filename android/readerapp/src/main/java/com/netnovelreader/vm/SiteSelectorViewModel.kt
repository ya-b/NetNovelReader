package com.netnovelreader.vm

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import com.netnovelreader.repo.SiteSelectorRepo
import com.netnovelreader.repo.db.SiteSelectorEntity

class SiteSelectorViewModel(val repo: SiteSelectorRepo, app: Application) : AndroidViewModel(app) {
    val allSiteSelector = LivePagedListBuilder(
        repo.allSiteSelectors,
        PagedList.Config.Builder()
            .setPageSize(30)
            .setEnablePlaceholders(false)
            .build()
    ).build()
    val editPreferenceCommand = MutableLiveData<SiteSelectorEntity>()

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

    fun editPreference(entity: SiteSelectorEntity) {
        editPreferenceCommand.value = entity
    }

    fun savePreference(entity: SiteSelectorEntity) {
        if(entity.hostname.isEmpty()) {
            return
        }
        repo.saveSelector(entity)
    }
}