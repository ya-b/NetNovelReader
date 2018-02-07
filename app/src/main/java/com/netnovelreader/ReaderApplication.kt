package com.netnovelreader

import android.app.Application
import android.content.Context
import com.netnovelreader.common.THREAD_NUM
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
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
        Logger.addLogAdapter(AndroidLogAdapter()) //日志调试工具全局初始化操作
        LeakCanary.install(this)                  //内存泄漏检测框架LeakCanary全局初始化操作
        threadPool = newFixedThreadPoolContext(THREAD_NUM, "appPoolContext")
    }

    companion object {
        lateinit var appContext: Context
        lateinit var threadPool: ThreadPoolDispatcher
    }
}