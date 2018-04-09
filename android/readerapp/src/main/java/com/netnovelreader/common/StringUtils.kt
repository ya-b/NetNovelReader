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

fun String.replaceSlash(): String = this.replace("/", "SLASH")
