package com.netnovelreader.repo

import android.app.Application
import com.google.gson.Gson
import com.netnovelreader.R
import com.netnovelreader.repo.db.BookInfoEntity
import com.netnovelreader.repo.http.WebService
import com.netnovelreader.utils.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody

class UserInfoRepo(app: Application) : Repo(app) {

    fun login(username: String, passwd: String, block: (String?) -> Unit) {
        WebService.readerAPI
            .login(username, passwd)
            .subscribeOn(Schedulers.from(IO_EXECUTOR))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    block.invoke(it.token)
                    it.token?.let {
                        app.sharedPreferences().put(app.getString(R.string.tokenKey), it)
                    }
                },
                { block.invoke(null) }
            )
    }

    fun uploadRecord(block: (Boolean) -> Unit) {
        val records = db.bookInfoDao().getAll().let { Gson().toJson(it) }
        val body = RequestBody.create(MediaType.parse("text/plain"), records)
        val filePart = MultipartBody.Part.createFormData("fileupload", "record", body)
        WebService.readerAPI
            .saveRecord(getToken(), filePart)
            .subscribeOn(Schedulers.from(IO_EXECUTOR))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { block.invoke(it?.ret == 6) },
                { block.invoke(false) }
            )
    }

    fun downloadRecord(block: (Boolean) -> Unit) {
        WebService.readerAPI
            .restoreRecord(getToken())
            .subscribeOn(Schedulers.from(IO_EXECUTOR))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    val record = it?.books
                    if(record != null) {
                        saveRecordToDb(record)
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
                recordItem.coverPath ?: let { recordItem.coverPath = "" }
            }
            db.bookInfoDao().insert(*record.toTypedArray())
        }
    }

    private fun getToken() = "Bearer ${ app.sharedPreferences().get(app.getString(R.string.tokenKey),"") }"
}