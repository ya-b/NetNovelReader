package com.netnovelreader

import android.app.Application
import android.content.Context

/**
 * Created by yangbo on 2018/1/11.
 */
class ReaderApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext ?: run { appContext = applicationContext }
    }

    companion object {
        var appContext: Context? = null
    }
}