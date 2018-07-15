package com.netnovelreader

import android.app.Application
import com.squareup.leakcanary.LeakCanary


class ReaderApp : Application() {

    override fun onCreate() {
        super.onCreate()
        LeakCanary.install(this)
    }
}