package com.netnovelreader.service

import android.app.IntentService
import android.app.NotificationManager
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.widget.Toast
import com.netnovelreader.R
import com.netnovelreader.common.download.DownloadTask
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue

/**
 * Created by yangbo on 18-1-15.
 */
class DownloadService : IntentService {
    constructor() : super("DownloadService")
    constructor(name: String) : super(name)
    private var mNotificationManager: NotificationManager? = null
    private var builder: NotificationCompat.Builder? = null
    private val NOTIFYID = 1599407175
    var executors: ExecutorService? = null
    var lock: LinkedBlockingQueue<Int>? = null

    @Volatile
    var max = 0                 //下载总数
    @Volatile
    private var progress = 0     //下载完成数（包括失败的下载）
    @Volatile
    var failed = 0               //失败的下载数
    @Volatile
    var remainder = 0            //待下载的书籍数


    override fun onCreate() {
        super.onCreate()
        openNotification()
        lock = LinkedBlockingQueue()
        executors = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() / 2)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        remainder++
        return super.onStartCommand(intent, flags, startId)
    }

    @Synchronized
    override fun onHandleIntent(intent: Intent?) {
        remainder--
        intent ?: return
        val tableName = intent.getStringExtra("tableName")
        val catalogUrl = intent.getStringExtra("catalogurl")
        if (tableName.isNullOrEmpty() || catalogUrl.isNullOrEmpty()) return
        val downloadUnitList = DownloadTask(tableName, catalogUrl).downloadAll()
        if ({ max = downloadUnitList.size; max }() < 1) return
        Observable.fromIterable(downloadUnitList)
            .flatMap {
                Observable.create<Int> { emitter ->
                    try {
                        it.download(it.getChapterTxt())
                    } catch (e: IOException) {
                        failed++
                    } finally {
                        emitter.onNext(progressIncrement())
                    }
                }.subscribeOn(Schedulers.from(executors!!))
            }.observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                synchronized(IntentService::class.java) {
                    if (it >= progress) {
                        updateNotification(it, max)
                        if (progress == max) lock?.offer(1)
                    }
                }
            }
        lock?.take()
    }

    override fun onDestroy() {
        super.onDestroy()
        mNotificationManager?.cancel(NOTIFYID)
        mNotificationManager = null
        builder = null
        if (failed > 0) {
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
            ?.setContentTitle("${getString(R.string.downloading)}:${progress}/$max $str")
        mNotificationManager?.notify(NOTIFYID, builder?.build())
    }

    fun progressIncrement(): Int {
        synchronized(max) {
            return ++progress
        }
    }
}