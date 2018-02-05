package com.netnovelreader.common

import android.os.Environment
import java.io.File
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

fun getSavePath(): String {
    var path: String?
    if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
        path = Environment.getExternalStorageDirectory().path + "/netnovelreader"
    } else {
        path = "/data/data/com.mynovelreader"
    }
    return path
}

//例如: http://www.hello.com/world/fjwoj/foew.html  中截取 hello.com
fun url2Hostname(url: String): String {
    var hostname: String? = null
    val matcher = Pattern.compile(".*?//.*?\\.(.*?)/.*?").matcher(url)
    if (matcher.find())
        hostname = matcher.group(1)
    return hostname ?: "error"
}

//根据shelf书名对应的id作为这本书目录表的表名（表明不能用数字作为开头）
fun id2TableName(id: Any): String {
    return "BOOK" + id.toString()
}

fun tableName2Id(tableName: String): String {
    return tableName.replace("BOOK", "")
}

fun mkdirs(dir: String): String {
    val file = File(dir)
    if (!file.exists()) {
        file.mkdirs()
    }
    return file.toString()
}

fun getHeaders(url: String): HashMap<String, String> {
    val map = HashMap<String, String>()
    map.put("accept", "indicator/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    map.put("user-agent", UA)
    map.put("Upgrade-Insecure-Requests", "1")
    map.put("Referer", "http://www.${url2Hostname(url)}/")
    return map
}

fun fixUrl(referenceUrl: String, fixUrl: String): String {
    if (fixUrl.startsWith("http")) return fixUrl
    if (fixUrl.startsWith("//")) return "http:" + fixUrl
    val arr = fixUrl.split("/")
    if (arr.size < 2) return referenceUrl.substring(0, referenceUrl.lastIndexOf("/") + 1) + fixUrl
    if (referenceUrl.contains(arr[1])) {
        return referenceUrl.substring(0, referenceUrl.indexOf(arr[1]) - 1) + fixUrl
    } else {
        return referenceUrl.substring(0, referenceUrl.lastIndexOf("/")) + fixUrl
    }
}