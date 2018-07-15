package com.netnovelreader.vm

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.databinding.ObservableBoolean
import com.netnovelreader.repo.UserInfoRepo

class SyncRecordViewModel(val repo: UserInfoRepo, app: Application) : AndroidViewModel(app) {

    val isLoading = ObservableBoolean(false)
    val toastCommand = MutableLiveData<String>()

    fun saveRecord() {
        isLoading.set(true)
        repo.uploadRecord {
            if(it) {
                toastCommand.value = "Success"
            } else {
                toastCommand.value = "Failed"
            }
            isLoading.set(false)
        }
    }

    fun restoreRecord() {
        isLoading.set(true)
        repo.downloadRecord {
            if(it) {
                toastCommand.value = "Success"
            } else {
                toastCommand.value = "Failed"
            }
            isLoading.set(false)
        }
    }
}