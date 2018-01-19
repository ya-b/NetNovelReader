package com.netnovelreader.service

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import com.netnovelreader.R
import com.netnovelreader.common.DownloadTask
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
    var mNotificationManager: NotificationManager? = null
    var builder: NotificationCompat.Builder? = null
    var queue: LinkedBlockingQueue<DownloadTask>? = null
    var tmpQueue: LinkedList<DownloadTask>? = null
    val NOTIFYID = 1599407175
    var executors: ExecutorService? = null
    var executeDownload: Thread? = null
    @Volatile
    var max = -1
    @Volatile
    var progress = 0

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        openNotification()
        queue = LinkedBlockingQueue()
        tmpQueue = LinkedList()
        executors = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors() + 1)
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
                val t = DownloadTask(intent!!.getStringExtra("tableName"), intent.getStringExtra("catalogurl"))
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
    }

    fun openNotification() {
        builder = NotificationCompat.Builder(this, "reader")
                .setTicker(getString(R.string.app_name))
                .setContentTitle(getString(R.string.prepare_download))
                .setSmallIcon(R.drawable.ic_launcher_background)
        mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager?.notify(NOTIFYID, builder?.build())
    }

    fun updateNotification(max: Int, progress: Int) {
        var str: String?
        if (tmpQueue!!.isEmpty()) {
            str = ""
        } else {
            str = ",${getString(R.string.wait2download)}".replace("n", tmpQueue!!.size.toString())
        }
        builder?.setProgress(max, progress, false)?.setContentTitle("${getString(R.string.downloading)}:$progress/$max$str")
        mNotificationManager?.notify(NOTIFYID, builder?.build())
    }

    fun stopOrContinue() {
        if (progress == max) {  //判断一个task是否执行完
            if (tmpQueue!!.size == 0) { //是否还有任务待执行
                queue!!.offer(DownloadTask("", ""))
                stopSelf()
            } else {
                queue!!.offer(tmpQueue!!.removeFirst())
            }
        }
    }

    inner class ExecuteDownload : Runnable {

        override fun run() {
            while (true) {
                val downloadTask = queue!!.take()
                if (downloadTask.tableName.equals("")) { //线程结束
                    executors?.shutdown()
                    break
                }
                var downloadUnitList: ArrayList<DownloadTask.DownloadChapterUnit>? = null
                try {
                    downloadUnitList = downloadTask.getUnitList()
                } catch (e: IOException) {
                    stopOrContinue()
                }
                max += downloadUnitList?.size ?: 0
                if (downloadUnitList?.isEmpty() ?: true) {
                    stopOrContinue()
                } else {
                    Observable.fromIterable(downloadUnitList)
                            .flatMap {
                                Observable.create<Int> { emitter -> emitter.onNext(it.download(it.getChapterTxt())) }
                                        .subscribeOn(Schedulers.from(executors!!))
                            }
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                updateNotification(max, ++progress)
                                stopOrContinue()
                            }, {
                                updateNotification(max, ++progress)
                                stopOrContinue()
                            })
                }
            }
        }
    }
}