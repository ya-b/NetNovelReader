package com.netnovelreader

import android.app.Application
import com.netnovelreader.repo.http.WebService
import okhttp3.logging.HttpLoggingInterceptor
//import leakcanary.LeakCanary
import org.slf4j.LoggerFactory

class ReaderApp : Application() {

    private val log = LoggerFactory.getLogger("NetworkLogger")

    override fun onCreate() {
        super.onCreate()
//        LeakCanary.install(this)

        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            LoggerFactory.getLogger(this.javaClass).error(t.name, e)
        }

        WebService.init(object: HttpLoggingInterceptor.Logger{
            override fun log(message: String) {
                log.debug(message)
            }

        })
    }
}