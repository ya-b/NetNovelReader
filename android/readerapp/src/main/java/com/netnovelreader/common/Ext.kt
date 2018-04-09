package com.netnovelreader.common

import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.Toast
import com.netnovelreader.data.local.db.ShelfBean
import com.netnovelreader.data.local.db.ShelfDao
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


fun Context.toast(message: String) {
    Toast.makeText(this@toast, message, Toast.LENGTH_SHORT).show()
}

/**
 * 简化[Call.enqueue]书写
 */
inline fun <T> Call<T>.enqueueCall(crossinline block: (t: T?) -> Unit) {
    this.enqueue(object : Callback<T> {
        override fun onFailure(call: Call<T>?, t: Throwable?) {
            block(null)
        }

        override fun onResponse(call: Call<T>?, response: Response<T>?) {
            block(response?.body())
        }
    })
}

fun <T, E> RecyclerView.init(
        adapter: RecyclerAdapter<in T, in E>,
        decor: RecyclerView.ItemDecoration? = NovelItemDecoration(context),
        layoutManager: RecyclerView.LayoutManager = object : LinearLayoutManager(context) {
            override fun supportsPredictiveItemAnimations(): Boolean {
                return false
            }
        },
        animator: RecyclerView.ItemAnimator? = DefaultItemAnimator()
) {
    this.layoutManager = layoutManager
    this.adapter = adapter
    this.itemAnimator = animator
    if (decor != null) this.addItemDecoration(decor)
}

fun ShelfDao.replace(
        _id: Int? = null,
        bookName: String?,
        downloadUrl: String? = null,
        readRecord: String? = null,
        isUpdate: String? = null,
        latestChapter: String? = null,
        latestRead: Int? = null
) {
    if (bookName.isNullOrEmpty()) return
    val now = ShelfBean(_id, bookName, downloadUrl, readRecord, isUpdate, latestChapter, latestRead)
    val old = getBookInfo(now.bookName!!)
    if (old == null) {
        insert(now)
    } else {
        ShelfBean(
                now._id ?: old._id,
                now.bookName,
                now.downloadUrl ?: old.downloadUrl,
                now.readRecord ?: old.readRecord,
                now.isUpdate ?: old.isUpdate,
                now.latestChapter ?: old.latestChapter,
                now.latestRead ?: old.latestRead
        ).also { insert(it) }
    }
}


fun <T : AndroidViewModel> FragmentActivity.obtainViewModel(clazz: Class<T>): T =
        ViewModelProvider.AndroidViewModelFactory.getInstance(this.application)
                .let { ViewModelProviders.of(this, it).get(clazz) }


fun Context.checkPermission(permission: String): Boolean {
    return ActivityCompat.checkSelfPermission(
            this,
            permission
    ) == PackageManager.PERMISSION_GRANTED
}