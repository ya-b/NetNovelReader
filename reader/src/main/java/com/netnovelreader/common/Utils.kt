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
import java.util.regex.Pattern

/**
 * Created by yangbo on 17-12-11.
 */

const val IMAGENAME = "image" //书籍封面图片名
const val TIMEOUT = 3000
const val UA = "Mozilla/5.0 (X11; Linux x86_64; rv:58.0) Gecko/20100101 Firefox/58.0"
const val PREFERENCE_NAME = "com.netnovelreader_preferences"
const val UPDATEFLAG = "●"  //书籍有更新，显示该标志
const val NotDeleteNum = 3 //自动删除已读章节，但保留最近3章
const val SLASH = "SLASH" //用“SLASH”替换"/"
val THREAD_NUM = Runtime.getRuntime().availableProcessors() * 2 / 3 //线程数

//例如: http://www.hello.com/world/fjwoj/foew.html  中截取 hello.com
fun url2Hostname(url: String): String {
    var hostname: String? = null
    val matcher = Pattern.compile(".*?//.*?\\.(.*?)/.*?").matcher(url)
    if (matcher.find())
        hostname = matcher.group(1)
    return hostname ?: "error"
}

fun getHeaders(url: String): HashMap<String, String> {
    val map = HashMap<String, String>()
    map["accept"] = "indicator/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
    map["user-agent"] = UA
    map["Upgrade-Insecure-Requests"] = "1"
    map["Referer"] = "http://www.${url2Hostname(url)}/"
    return map
}

//对不合法url修复
fun fixUrl(referenceUrl: String, fixUrl: String): String {
    if (fixUrl.isEmpty()) return ""
    if (fixUrl.startsWith("http")) return fixUrl
    if (fixUrl.startsWith("//")) return "http:" + fixUrl
    val str = if (fixUrl.startsWith("/")) fixUrl else "/" + fixUrl
    val arr = str.split("/")
    if (arr.size < 2) return referenceUrl.substring(0, referenceUrl.lastIndexOf("/")) + str
    if (referenceUrl.contains(arr[1]))
        return referenceUrl.substring(0, referenceUrl.indexOf(arr[1]) - 1) + str
    return referenceUrl.substring(0, referenceUrl.lastIndexOf("/")) + str
}

//简化书写
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
        ).apply { insert(this) }
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