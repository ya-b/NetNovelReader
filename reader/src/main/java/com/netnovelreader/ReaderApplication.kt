package com.netnovelreader

import android.app.Application
import android.os.Environment
import com.squareup.leakcanary.LeakCanary
import kotlinx.coroutines.experimental.ThreadPoolDispatcher
import kotlinx.coroutines.experimental.newFixedThreadPoolContext

class ReaderApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = this
        LeakCanary.install(this)                  //内存泄漏检测框架LeakCanary全局初始化操作
        threadPool = newFixedThreadPoolContext(
                Runtime.getRuntime().availableProcessors() * 2 / 3, "appPoolContext"
        )

        dirPath = if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            Environment.getExternalStorageDirectory().path + "/netnovelreader"
        } else {
            filesDir.absolutePath
        }
    }

    companion object {
        lateinit var appContext: Application
        lateinit var threadPool: ThreadPoolDispatcher
        lateinit var dirPath: String
    }
}