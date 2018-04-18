package com.netnovelreader.common

import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.SharedPreferences
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.Toast
import com.netnovelreader.R
import com.netnovelreader.data.local.db.ShelfBean
import com.netnovelreader.data.local.db.ShelfDao
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File


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

fun Context.sharedPreferences(name: String = this.applicationContext.packageName, type: Int = Context.MODE_PRIVATE) =
        getSharedPreferences(name, type)

fun <T: Any> SharedPreferences.put(key: String, value: T) {
    edit().apply {
        when (value) {
            is String -> putString(key, value)
            is Int -> putInt(key, value)
            is Float -> putFloat(key, value)
            is Long -> putLong(key, value)
            is Boolean -> putBoolean(key, value)
            else -> throw IllegalArgumentException()
        }
    }.apply()
}

@Suppress("UNCHECKED_CAST")
fun <T: Any> SharedPreferences.get(key: String, default: T) : T =
    when (default) {
        is String -> getString(key, default) as T
        is Int -> getInt(key, default) as T
        is Float -> getFloat(key, default) as T
        is Long -> getLong(key, default) as T
        is Boolean -> getBoolean(key, default) as T
        else -> throw IllegalArgumentException()
    }

inline fun <T> tryIgnoreCatch(body: () -> T?): T? =
    try {
        body()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

fun String.parseTheme() =
    when (this) {
        "blue" -> R.style.AppThemeBlue
        "gray" -> R.style.AppThemeGray
        else -> R.style.AppThemeBlack
    }

operator fun File.unaryMinus() =
    if(this.exists()) {
        if(this.isDirectory) {
            this.deleteRecursively()
        } else {
            this.delete()
        }
    } else {
        false
    }
