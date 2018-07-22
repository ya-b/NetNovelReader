package com.netnovelreader.repo

import android.app.Application
import com.google.gson.Gson
import com.netnovelreader.R
import com.netnovelreader.repo.db.BookInfoEntity
import com.netnovelreader.repo.http.WebService
import com.netnovelreader.utils.*
import io.reactivex.Observable
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
                    if(token.isNullOrEmpty()) {
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
        Observable.create<String> {
            val str = db.bookInfoDao().getAll()
                .let { Gson().toJson(it) }
            it.onNext(str)
            it.onComplete()
        }.subscribeOn(Schedulers.from(IO_EXECUTOR))
            .flatMap { str ->
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
                    if(it != null) {
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
        ioThread {
            val local = db.bookInfoDao().getAll()
            record.forEach { recordItem ->
                recordItem._id = null
                local.firstOrNull { it.bookname == recordItem.bookname }
                    ?.let { recordItem._id = it._id }
            }
            db.bookInfoDao().insert(*record.toTypedArray())
        }
    }

    private fun getToken() = "Bearer ${ app.sharedPreferences().get(app.getString(R.string.tokenKey),"") }"
}