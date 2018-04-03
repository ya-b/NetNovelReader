package com.netnovelreader.utils

import java.util.regex.Pattern

const val TIMEOUT = 3000
const val SEARCH_NAME = "searchname"
const val UA = "Mozilla/5.0 (X11; Linux x86_64; rv:58.0) Gecko/20100101 Firefox/58.0"

fun getHeaders(url: String): HashMap<String, String> {
    val map = HashMap<String, String>()
    map["accept"] = "indicator/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
    map["user-agent"] = UA
    map["Upgrade-Insecure-Requests"] = "1"
    map["Referer"] = "http://www.${url2Hostname(url)}/"
    return map
}

fun url2Hostname(url: String): String {
    var hostname: String? = null
    val matcher = Pattern.compile(".*?//.*?\\.(.*?)/.*?").matcher(url)
    if (matcher.find())
        hostname = matcher.group(1)
    return hostname ?: "error"
}