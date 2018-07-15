package com.netnovelreader.vm

import android.app.Activity
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.databinding.ObservableBoolean
import com.netnovelreader.R
import com.netnovelreader.repo.UserInfoRepo

class LoginViewModel(val repo: UserInfoRepo, app: Application) : AndroidViewModel(app) {

    val isLoading = ObservableBoolean(false)
    val toastCommand = MutableLiveData<String>()

    fun login(username: String, passwd: String) {
        if (username.length < 4 || passwd.length < 4) {
            return
        }
        isLoading.set(true)
        repo.login(username, passwd) { token ->
            val app = getApplication<Application>()
            isLoading.set(false)
            if(token.isNullOrEmpty()) {
                toastCommand.value = app.getString(R.string.login_error)
            } else {
                toastCommand.value = Activity.RESULT_OK.toString()
            }
        }
    }
}