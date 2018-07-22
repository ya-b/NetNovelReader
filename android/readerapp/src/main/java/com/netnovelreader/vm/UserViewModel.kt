package com.netnovelreader.vm

import android.app.Activity
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.databinding.ObservableBoolean
import com.netnovelreader.R
import com.netnovelreader.repo.UserRepo
import com.netnovelreader.utils.get
import com.netnovelreader.utils.sharedPreferences

class UserViewModel(val repo: UserRepo, app: Application) : AndroidViewModel(app) {

    val isLoading = ObservableBoolean(false)
    val toastCommand = MutableLiveData<String>()
    val exitCommand = MutableLiveData<Void>()

    fun login(username: String, passwd: String) {
        val app = getApplication<Application>()
        if (username.length < 4 || passwd.length < 4) {
            toastCommand.value = app.getString(R.string.editor_hint)
            return
        }
        isLoading.set(true)
        repo.login(username, passwd) { token ->
            isLoading.set(false)
            if(token.isNullOrEmpty()) {
                toastCommand.value = app.getString(R.string.login_error)
            } else {
                toastCommand.value = Activity.RESULT_OK.toString()
            }
        }
    }

    fun logout() {
        repo.logout()
        exitCommand.value = null
    }

    fun isLogin(): Boolean {
        val app = getApplication<Application>()
        return app.sharedPreferences().get(app.getString(R.string.tokenKey), "").isNotEmpty()
    }

    fun getUserName(): String {
        val app = getApplication<Application>()
        return app.sharedPreferences().get(app.getString(R.string.usernameKey), "")
    }

    fun saveRecord() {
        isLoading.set(true)
        repo.uploadRecord {
            toastCommand.value = it
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