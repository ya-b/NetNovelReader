package com.netnovelreader.utils

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

fun Context.sharedPreferences(name: String = this.applicationContext.packageName, type: Int = Context.MODE_PRIVATE) =
    this.getSharedPreferences(name, type)!!

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

fun Context.toast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun String.toMD5(): String =
    try {
        val  instance:MessageDigest = MessageDigest.getInstance("MD5")//获取md5加密对象
        val digest:ByteArray = instance.digest(this.toByteArray())//对字符串加密，返回字节数组
        val sb = StringBuffer()
        for (b in digest) {
            val i :Int = b.toInt() and 0xff//获取低八位有效值
            var hexString = Integer.toHexString(i)//将整数转化为16进制
            if (hexString.length < 2) {
                hexString = "0$hexString"//如果是一位的话，补0
            }
            sb.append(hexString)
        }
        sb.toString()
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
        ""
    }