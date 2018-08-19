package com.netnovelreader.repo

import android.app.Application
import com.google.gson.Gson
import com.netnovelreader.R
import com.netnovelreader.repo.db.BookInfoEntity
import com.netnovelreader.repo.http.WebService
import com.netnovelreader.utils.*
import io.reactivex.MaybeSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody

class UserRepo(app: Application) : Repo(app) {

    fun login(username: String, passwd: String) =
        WebService.readerAPI
            .login(username, passwd)
            .subscribeOn(Schedulers.from(IO_EXECUTOR))
            .observeOn(AndroidSchedulers.mainThread())
            .map { Pair(username, it.string()) }

    fun logout() {
        app.sharedPreferences().put(app.getString(R.string.tokenKey), "")
        app.sharedPreferences().put(app.getString(R.string.usernameKey), "")
    }

    fun uploadRecord() =
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

    fun downloadRecord() = WebService.readerAPI .restoreRecord(getToken())

    fun existsRecord() = db.bookInfoDao().getAll()

    fun insertRecord(record: List<BookInfoEntity>) {
        ioThread {
            db.bookInfoDao().insert(*record.toTypedArray())
        }
    }

    private fun getToken() =
        "Bearer ${app.sharedPreferences().get(app.getString(R.string.tokenKey), "")}"
}