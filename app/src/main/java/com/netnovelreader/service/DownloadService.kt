package com.netnovelreader.service

import android.app.IntentService
import android.app.NotificationManager
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.widget.Toast
import com.netnovelreader.R
import com.netnovelreader.common.THREAD_NUM
import com.netnovelreader.common.download.DownloadTask
import kotlinx.coroutines.experimental.ThreadPoolDispatcher
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newFixedThreadPoolContext
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by yangbo on 18-1-15.
 */
class DownloadService : IntentService {
    constructor() : super("DownloadService")
    constructor(name: String) : super(name)

    private var mNotificationManager: NotificationManager? = null
    private var builder: NotificationCompat.Builder? = null
    private val NOTIFYID = 1599407175
    lateinit var lock: LinkedBlockingQueue<Int>
    lateinit var poolContext: ThreadPoolDispatcher

    @Volatile
    private var max = 0                 //下载总数
    private var progress = AtomicInteger()     //下载完成数（包括失败的下载）
    private var failed = AtomicInteger()              //失败的下载数
    @Volatile
    private var remainder = 0            //待下载的书籍数

    override fun onCreate() {
        super.onCreate()
        openNotification()
        lock = LinkedBlockingQueue()
        poolContext = newFixedThreadPoolContext(THREAD_NUM, "DownloadService")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        remainder++
        return super.onStartCommand(intent, flags, startId)
    }

    @Synchronized
    override fun onHandleIntent(intent: Intent?) {
        remainder--
        val tableName = intent?.getStringExtra("tableName")
        val catalogUrl = intent?.getStringExtra("catalogurl")
        if (intent == null || tableName.isNullOrEmpty() || catalogUrl.isNullOrEmpty()) return
        launch {
            DownloadTask(tableName!!, catalogUrl!!).getList().apply { max = this.size }
                .forEach {
                    async(poolContext) { it.download(it.getChapterTxt()) }
                        .invokeOnCompletion {
                            synchronized(IntentService::class.java) {
                                if (it == null) {
                                    progress.incrementAndGet()
                                } else {
                                    failed.incrementAndGet()
                                }
                                updateNotification(progress.get(), max)
                                if (progress.get() + failed.get() == max) lock.offer(1)
                            }
                        }
                }
        }
        lock.take()
    }

    override fun onDestroy() {
        super.onDestroy()
        mNotificationManager?.cancel(NOTIFYID)
        if (failed.get() > 0) {
            Toast.makeText(
                this, getString(R.string.downloadfailed).replace("nn", "$failed"),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun openNotification() {
        builder = NotificationCompat.Builder(this, "reader")
            .setTicker(getString(R.string.app_name))
            .setContentTitle(getString(R.string.prepare_download))
            .setSmallIcon(R.drawable.ic_launcher_background)
        mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager?.notify(NOTIFYID, builder?.build())
    }

    fun updateNotification(progress: Int, max: Int) {
        val str = if (remainder != 0) ",${getString(R.string.wait4download)}"
            .replace("nn", "$remainder") else ""
        builder?.setProgress(max, progress, false)
            ?.setContentTitle("${getString(R.string.downloading)}:${progress}/$max$str")
        mNotificationManager?.notify(NOTIFYID, builder?.build())
    }
}