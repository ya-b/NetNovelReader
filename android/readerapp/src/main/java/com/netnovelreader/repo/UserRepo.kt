package com.netnovelreader.repo

import android.app.Application
import com.google.gson.Gson
import com.netnovelreader.R
import com.netnovelreader.repo.db.BookInfoEntity
import com.netnovelreader.repo.http.WebService
import com.netnovelreader.utils.IO_EXECUTOR
import com.netnovelreader.utils.get
import com.netnovelreader.utils.put
import com.netnovelreader.utils.sharedPreferences
import io.reactivex.MaybeSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody

class UserRepo(app: Application) : Repo(app) {

    fun login(username: String, passwd: String, block: (String?) -> Unit) {
        WebService.readerAPI
            .login(username, passwd)
            .subscribeOn(Schedulers.from(IO_EXECUTOR))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    var token = it?.string()
                    if (token.isNullOrEmpty()) {
                        block.invoke(null)
                    } else {
                        app.sharedPreferences().put(app.getString(R.string.tokenKey), token!!)
                        app.sharedPreferences().put(app.getString(R.string.usernameKey), username)
                        block.invoke(token)
                    }
                },
                {
                    block.invoke(null)
                }
            )
    }

    fun logout() {
        app.sharedPreferences().put(app.getString(R.string.tokenKey), "")
        app.sharedPreferences().put(app.getString(R.string.usernameKey), "")
    }

    fun uploadRecord(block: (String) -> Unit) {
        db.bookInfoDao()
            .getAll()
            .subscribeOn(Schedulers.from(IO_EXECUTOR))
            .flatMap { list ->
                MaybeSource<String> {
                    it.onSuccess(Gson().toJson(list))
                    it.onComplete()
                }
            }
            .flatMapObservable { str ->
                val body = RequestBody.create(MediaType.parse("text/plain"), str)
                val filePart = MultipartBody.Part.createFormData("fileupload", "record", body)
                WebService.readerAPI.saveRecord(getToken(), filePart)
            }.subscribeOn(Schedulers.from(IO_EXECUTOR))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    block.invoke(it.string())
                },
                {
                    block.invoke("error")
                }
            )
    }

    fun downloadRecord(block: (Boolean) -> Unit) {
        WebService.readerAPI
            .restoreRecord(getToken())
            .subscribeOn(Schedulers.from(IO_EXECUTOR))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    if (it != null) {
                        saveRecordToDb(it)
                        block.invoke(true)
                    } else {
                        block.invoke(false)
                    }
                },
                { block.invoke(false) }
            )
    }

    private fun saveRecordToDb(record: List<BookInfoEntity>) {
        db.bookInfoDao()
            .getAll()
            .subscribeOn(Schedulers.from(IO_EXECUTOR))
            .subscribe {
                record.forEach { recordItem ->
                    recordItem._id = null
                    it.firstOrNull { it.bookname == recordItem.bookname }
                        ?.let { recordItem._id = it._id }
                }
                db.bookInfoDao().insert(*record.toTypedArray())
            }
    }

    private fun getToken() =
        "Bearer ${app.sharedPreferences().get(app.getString(R.string.tokenKey), "")}"
}