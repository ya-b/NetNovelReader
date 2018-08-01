package com.netnovelreader.repo.http.paging

import android.arch.lifecycle.MutableLiveData
import android.arch.paging.PageKeyedDataSource
import com.netnovelreader.repo.http.WebService
import com.netnovelreader.repo.http.resp.BookLinkResp
import com.netnovelreader.utils.IO_EXECUTOR
import com.netnovelreader.utils.ioThread
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class BookLinkTopClickDataSource(val cacheDir: File) : PageKeyedDataSource<String, BookLinkResp>() {
    val cacheName = "rank%s"
    private var retry: (() -> Any)? = null
    val networkState = MutableLiveData<NetworkState>()
    val initialLoad = MutableLiveData<NetworkState>()

    fun retryFailed() {
        val prevRetry = retry
        retry = null
        prevRetry?.let {
            ioThread { it.invoke() }
        }
    }

    override fun loadInitial(
        params: LoadInitialParams<String>,
        callback: LoadInitialCallback<String, BookLinkResp>
    ) {
        networkState.postValue(NetworkState.LOADING)
        initialLoad.postValue(NetworkState.LOADING)
        getObject("1").subscribeOn(Schedulers.from(IO_EXECUTOR)).subscribe(
            {
                putObject("1", it)
                retry = null
                networkState.postValue(NetworkState.LOADED)
                initialLoad.postValue(NetworkState.LOADED)
                callback.onResult(it, null, "2")
            },
            {
                val error = NetworkState.error(it.toString() ?: "unknown err")
                networkState.postValue(error)
                initialLoad.postValue(error)
                retry = { loadInitial(params, callback) }
            })
    }

    override fun loadAfter(
        params: LoadParams<String>,
        callback: LoadCallback<String, BookLinkResp>
    ) {
        networkState.postValue(NetworkState.LOADING)
        getObject(params.key).subscribeOn(Schedulers.from(IO_EXECUTOR)).subscribe(
            {
                putObject(params.key, it)
                retry = null
                networkState.postValue(NetworkState.LOADED)
                val nextKey = params.key.toInt() + 1
                callback.onResult(it, if (nextKey == 51) null else nextKey.toString())
            },
            {
                networkState.postValue(NetworkState.error(it.toString() ?: "unknown err"))
                retry = { loadAfter(params, callback) }
            })
    }

    override fun loadBefore(
        params: LoadParams<String>,
        callback: LoadCallback<String, BookLinkResp>
    ) {

    }

    @Suppress("UNCHECKED_CAST")
    fun getObject(pageNum: String): Single<List<BookLinkResp>> {
        val file = File(cacheDir, String.format(cacheName, pageNum))
        return if(file.exists()) {
            Single.create<List<BookLinkResp>> {
                var list: List<BookLinkResp>? = null
                ObjectInputStream(file.inputStream())
                    .use { list = it.readObject() as List<BookLinkResp> }
                if(list != null) {
                    it.onSuccess(list!!)
                } else {
                    it.onError(Throwable("unknown err"))
                }
            }
        } else {
            WebService.bookLinkRanking.getRanking(pageNum.toInt(), 20)
        }
    }

    fun putObject(pageNum: String, list: List<BookLinkResp>) {
        val file = File(cacheDir, String.format(cacheName, pageNum))
        if (file.exists()) file.delete()
        ObjectOutputStream(file.outputStream()).use { it.writeObject(list) }
    }
}