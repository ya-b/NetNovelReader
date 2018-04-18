package com.netnovelreader.common

import java.util.regex.Pattern

//例如: http://www.hello.com/world/fjwoj/foew.html  中截取 hello.com
fun url2Hostname(url: String): String {
    var hostname: String? = null
    val matcher = Pattern.compile(".*?//(.*?)/.*?").matcher(url)
    if (matcher.find()){
        hostname = matcher.group(1)
        val arr = hostname.split(".")
        if(arr.size > 1){
            hostname = "${arr[arr.size - 2]}.${arr[arr.size - 1]}"
        }
    }
    return hostname ?: "error"
}

fun getHeaders(url: String): HashMap<String, String> {
    val map = HashMap<String, String>()
    map["accept"] = "indicator/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
    map["user-agent"] = "Mozilla/5.0 (X11; Linux x86_64; rv:58.0) Gecko/20100101 Firefox/58.0"
    map["Upgrade-Insecure-Requests"] = "1"
    map["Referer"] = "http://www.${url2Hostname(url)}/"
    return map
}

//对不合法url修复
fun fixUrl(referenceUrl: String, fixUrl: String): String {
    val str = if (fixUrl.startsWith("/")) fixUrl else "/" + fixUrl
    val arr = str.split("/")
    return when {
        fixUrl.isEmpty() -> ""
        fixUrl.startsWith("http") -> fixUrl
        fixUrl.startsWith("//") -> "http:" + fixUrl
        arr.size < 2 -> referenceUrl.substring(0, referenceUrl.lastIndexOf("/")) + str
        referenceUrl.contains(arr[1]) -> referenceUrl.substring(0, referenceUrl.indexOf(arr[1]) - 1) + str
        else -> referenceUrl.substring(0, referenceUrl.lastIndexOf("/")) + str
    }
}

fun String.replaceSlash(): String = this.replace("/", "SLASH")
