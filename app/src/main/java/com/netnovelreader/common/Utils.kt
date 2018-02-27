package com.netnovelreader.common

import android.content.Context
import android.os.Environment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.Toast
import com.netnovelreader.data.db.ShelfBean
import com.netnovelreader.data.db.ShelfDao
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.regex.Pattern

/**
 * Created by yangbo on 17-12-11.
 */

val IMAGENAME = "image" //书籍封面图片名
val TIMEOUT = 3000
val UA = "Mozilla/5.0 (X11; Linux x86_64; rv:58.0) Gecko/20100101 Firefox/58.0"
val PREFERENCE_NAME = "com.netnovelreader_preferences"
val UPDATEFLAG = "●"  //书籍有更新，显示该标志
val NotDeleteNum = 3 //自动删除已读章节，但保留最近3章
val THREAD_NUM = Runtime.getRuntime().availableProcessors() * 2 / 3 //线程数

fun getSavePath(): String =
    if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
        Environment.getExternalStorageDirectory().path + "/netnovelreader"
    } else {
        "/data/data/com.netnovelreader"
    }

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
fun Context.toast(message: String) = launch(UI) {
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
    decor: RecyclerView.ItemDecoration? = NovelItemDecoration(this.context),
    layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this.context),
    animator: RecyclerView.ItemAnimator = DefaultItemAnimator()
) {
    this.layoutManager = layoutManager
    this.adapter = adapter
    this.itemAnimator = animator
    if (decor != null) this.addItemDecoration(decor)
}


fun ShelfDao.replace(bean: ShelfBean) {
    val old = getBookInfo(bean.bookName!!)
    if (old == null) {
        insert(bean)
    } else {
        ShelfBean(
            bean._id ?: old._id,
            bean.bookName,
            bean.downloadUrl ?: old.downloadUrl,
            bean.readRecord ?: old.readRecord,
            bean.isUpdate ?: old.isUpdate,
            bean.latestChapter ?: old.latestChapter,
            bean.latestRead ?: old.latestRead
        ).apply { insert(this) }
    }
}