package com.netnovelreader

import android.app.Application
import android.content.Context

/**
 * Created by yangbo on 2018/1/11.
 */
class NetNovelReaderApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        context ?: run{ context = applicationContext }
    }
    companion object {
        var context: Context? = null
    }
}