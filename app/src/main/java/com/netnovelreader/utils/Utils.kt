package com.netnovelreader.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import android.support.v4.app.ActivityCompat
import java.util.jar.Manifest
import java.util.regex.Pattern

/**
 * Created by yangbo on 17-12-11.
 */

fun getSavePath(): String {
    var path: String?
    if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
        path = Environment.getExternalStorageDirectory().path.toString() + "/mynovelreader"
    } else {
        path = "/data/data/com.mynovelreader"
    }
    return path
}

fun url2Hostname(url: String) : String{
    var hostname: String? = null
    var matcher = Pattern.compile(".*?//.*?\\.(.*?)/.*?").matcher(url)
    if (matcher.find())
        hostname = matcher.group(1)
    return hostname ?: "error"
}
