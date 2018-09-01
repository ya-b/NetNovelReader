package com.netnovelreader.utils

import java.lang.IllegalStateException

fun checkNotNullOrEmpty(vararg ss: String?) {
    ss.forEach {
        if (it.isNullOrEmpty()) {
            throw IllegalStateException("arg must not be null")
        }
    }
}