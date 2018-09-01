package com.netnovelreader.vm

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.databinding.ObservableBoolean
import com.netnovelreader.R
import com.netnovelreader.repo.UserRepo
import com.netnovelreader.repo.db.BookInfoEntity
import com.netnovelreader.utils.IO_EXECUTOR
import com.netnovelreader.utils.get
import com.netnovelreader.utils.put
import com.netnovelreader.utils.sharedPreferences
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers

class UserViewModel(val repo: UserRepo, app: Application) : AndroidViewModel(app) {

    val isLoading = ObservableBoolean(false)
    val toastCommand = MutableLiveData<String>()
    val exitCommand = MutableLiveData<Void>()
    var compositeDisposable: CompositeDisposable = CompositeDisposable()

    fun destroy() {
        compositeDisposable.clear()
    }

    fun login(username: String, passwd: String) {
        val app = getApplication<Application>()
        if (username.length < 4 || passwd.length < 4) {
            toastCommand.value = app.getString(R.string.editor_hint)
            return
        }
        isLoading.set(true)
        repo.login(username, passwd)
            .subscribeOn(Schedulers.from(IO_EXECUTOR))
            .subscribe (
                {
                    if(it.second.isNullOrEmpty()) {
                        toastCommand.value = app.getString(R.string.login_error)
                    } else {
                        app.sharedPreferences().put(app.getString(R.string.tokenKey), it.second)
                        app.sharedPreferences().put(app.getString(R.string.usernameKey), it.first)
                        toastCommand.value = Activity.RESULT_OK.toString()
                        isLoading.set(false)
                    }
                },
                {
                    toastCommand.value = app.getString(R.string.login_error)
                    isLoading.set(false)
                }).let { compositeDisposable.add(it) }
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
        repo.uploadRecord().subscribe(
            {
                toastCommand.value = it.string()
                isLoading.set(false)
            },
            {
                toastCommand.value = "error"
                isLoading.set(false)
            }
        ).let { compositeDisposable.add(it) }
    }

    fun restoreRecord() {
        isLoading.set(true)
        Single.zip(repo.downloadRecord(), repo.existsRecord(),
            BiFunction<List<BookInfoEntity>, List<BookInfoEntity>,
                    Pair<List<BookInfoEntity>, List<BookInfoEntity>>> { t1, t2 -> Pair(t1, t2) })
            .subscribeOn(Schedulers.from(IO_EXECUTOR))
            .observeOn(AndroidSchedulers.mainThread())
            .map { pair ->
                pair.apply {
                    first.forEach { recordItem ->
                        recordItem._id = null
                        second.firstOrNull { it.bookname == recordItem.bookname }
                            ?.let { recordItem._id = it._id }
                    }
                }
            }
            .subscribe (
                {
                    repo.insertRecord(it.first)
                    toastCommand.value = "Success"
                    isLoading.set(false)
                },
                {
                    toastCommand.value = "Failed"
                    isLoading.set(false)
                }).let { compositeDisposable.add(it) }
    }
}