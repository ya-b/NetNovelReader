package com.netnovelreader

import android.app.Application
import com.squareup.leakcanary.LeakCanary
import org.slf4j.LoggerFactory

class ReaderApp : Application() {

    override fun onCreate() {
        super.onCreate()
        LeakCanary.install(this)

        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            LoggerFactory.getLogger(this.javaClass).error(t.name, e)
        }
    }
}