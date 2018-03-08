package com.netnovelreader

import android.app.Application
import android.content.Context
import android.os.Environment
import com.netnovelreader.common.THREAD_NUM
import com.squareup.leakcanary.LeakCanary
import kotlinx.coroutines.experimental.ThreadPoolDispatcher
import kotlinx.coroutines.experimental.newFixedThreadPoolContext


/**
 * Created by yangbo on 2018/1/11.
 */
class ReaderApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        LeakCanary.install(this)                  //内存泄漏检测框架LeakCanary全局初始化操作
        threadPool = newFixedThreadPoolContext(THREAD_NUM, "appPoolContext")

        dirPath = if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            Environment.getExternalStorageDirectory().path + "/netnovelreader"
        } else {
            filesDir.absolutePath
        }
    }

    companion object {
        lateinit var appContext: Context
        lateinit var threadPool: ThreadPoolDispatcher
        lateinit var dirPath: String
    }
}