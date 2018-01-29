package com.netnovelreader

import android.app.Application
import android.content.Context

/**
 * Created by yangbo on 2018/1/11.
 */
class ReaderApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }

    companion object {
        lateinit var appContext: Context
    }
}