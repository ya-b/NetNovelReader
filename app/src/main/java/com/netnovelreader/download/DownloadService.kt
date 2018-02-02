package com.netnovelreader.download

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.widget.Toast
import com.netnovelreader.R
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue

/**
 * Created by yangbo on 18-1-15.
 */
class DownloadService : Service() {
    private var mNotificationManager: NotificationManager? = null
    private var builder: NotificationCompat.Builder? = null
    var queue: LinkedBlockingQueue<DownloadTask>? = null
    private var tmpQueue: LinkedList<DownloadTask>? = null
    private val NOTIFYID = 1599407175
    var executors: ExecutorService? = null
    private var executeDownload: Thread? = null
    /**
     * 下载总数
     */
    @Volatile
    var max = -1
    /**
     * 下载完成数（包括失败的下载）
     */
    @Volatile
    private var progress = 0
    /**
     * 失败的下载数
     */
    @Volatile
    var failed = 0

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        openNotification()
        queue = LinkedBlockingQueue()
        tmpQueue = LinkedList()
        executors = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() / 3 * 2)
        executeDownload = Thread(ExecuteDownload())
        try {
            executeDownload?.start()
        } catch (e: Exception) {
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        synchronized(this) {
            if (queue != null || intent != null) {
                val t = DownloadTask(
                    intent!!.getStringExtra("tableName"),
                    intent.getStringExtra("catalogurl")
                )
                if (max == -1) {
                    queue!!.offer(t)
                    max = 0 //从网上解析目录需要时间，max不会马上赋值，所有在这里改变
                } else {
                    tmpQueue!!.add(t)
                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mNotificationManager?.cancel(NOTIFYID)
        mNotificationManager = null
        builder = null
        if (failed != 0) {
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
        if (max == -1) return
        val str: String
        if (tmpQueue!!.isEmpty()) {
            str = ""
        } else {
            str = ",${getString(R.string.wait4download)}".replace("nn", tmpQueue!!.size.toString())
        }
        builder?.setProgress(max, progress, false)
            ?.setContentTitle("${getString(R.string.downloading)}:${progress}/$max$str")
        mNotificationManager?.notify(NOTIFYID, builder?.build())
    }

    fun stopOrContinue(progress: Int, max: Int) {
        if (progress != max) return  //判断一个task是否执行完
        if (tmpQueue!!.size == 0) { //是否还有任务待执行
            queue!!.offer(DownloadTask("", ""))//ExecuteDownload线程结束信号
            stopSelf()
        } else {
            queue!!.offer(tmpQueue!!.removeFirst())
        }
    }

    @Synchronized
    fun progressIncrement(): Int {
        return ++progress
    }

    inner class ExecuteDownload : Runnable {
        var tmp = 0

        override fun run() {
            while (true) {
                val downloadTask = queue!!.take()
                if (downloadTask.tableName.equals("")) { //线程结束
                    executors?.shutdown()
                    break
                }
                var downloadUnitList: ArrayList<DownloadChapter>
                try {
                    downloadUnitList = downloadTask.downloadAll()
                } catch (e: IOException) {
                    stopOrContinue(progress, max)
                    continue
                }
                max += downloadUnitList.size
                if (downloadUnitList.isEmpty()) {
                    stopOrContinue(progress, max)
                } else {
                    downEveryItem(downloadUnitList)
                }
            }
        }

        private fun downEveryItem(downloadUnitList: ArrayList<DownloadChapter>) {
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
                    synchronized(this) {
                        if (it > tmp) {
                            updateNotification(it, max)
                            stopOrContinue(it, max)
                            tmp = it
                        }
                    }
                }
        }
    }
}