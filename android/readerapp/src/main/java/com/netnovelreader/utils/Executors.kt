package com.netnovelreader.utils

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors

private val UI_EXECUTOR = Handler(Looper.getMainLooper())
val IO_EXECUTOR = Executors.newFixedThreadPool(5)!!

fun ioThread(f: () -> Unit) {
    IO_EXECUTOR.execute(f)
}

fun uiThread(f: () -> Unit) {
    UI_EXECUTOR.post { f.invoke() }
}