package com.netnovelreader.utils

import java.util.regex.Pattern

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