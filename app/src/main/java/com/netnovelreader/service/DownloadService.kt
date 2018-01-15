package com.netnovelreader.service

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import com.netnovelreader.R
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.locks.ReentrantLock

/**
 * Created by yangbo on 18-1-15.
 */
class DownloadService : Service() {
    var mNotificationManager: NotificationManager? = null
    var builder: NotificationCompat.Builder?  = null
    var queue: LinkedBlockingQueue<DownloadTask>? = null
    val NOTIFYID = 1599407175
    @Volatile
    var max = 0
    @Volatile
    var progress = 0
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        openNotification()
        queue = LinkedBlockingQueue<DownloadTask>()
        downloadThreadStart()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(queue != null || intent != null){
            queue!!.offer(DownloadTask(intent!!.getStringExtra("localpath"),
                    intent.getStringExtra("catalogurl")))
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mNotificationManager?.cancel(NOTIFYID)
    }

    //TODO 优化
    fun downloadThreadStart(){
        Thread{
            Thread.sleep(100)
            val threadPool = Executors.newFixedThreadPool(5)
            var flag = true
            while (flag){
                var taskList: ArrayList<DownloadTask.ChapterRunnable>? = null
                try {
                    taskList = queue!!.take().getRunnables()
                }catch (e: Exception){
                    stopSelf()
                }
                flag = {max += taskList!!.size; max}() != 0
                Flowable.fromIterable(taskList)
                        .flatMap { it ->
                            Flowable.create<Int>({ e -> threadPool?.execute(it.setFun { e.onNext(1) }) },
                                    BackpressureStrategy.BUFFER)
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            updateNotification(max, ++progress)
                            if(progress == max){
                                stopSelf()
                                flag = false
                            }
                        }
            }
        }.start()
    }
//    fun downloadExecutor(){
//        val threadPool = Executors.newFixedThreadPool(5)
//        Thread{
//            while (flag || threadPool.isTerminated){
//                var taskList = queue!!.take().getRunnables()
//                max += taskList.size
//                taskList.forEach{
//                    threadPool.execute(it)
//                }
//            }
//        }.start()
//    }

    fun openNotification(){
        builder = NotificationCompat.Builder(this, "reader")
                .setTicker(getString(R.string.app_name))
                .setContentTitle(getString(R.string.prepare_download))
                .setProgress(100, 0, false)
                .setSmallIcon(R.drawable.ic_launcher_background)
        mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager?.notify(NOTIFYID, builder?.build())
    }

    fun updateNotification(max: Int, progress: Int){
        var str: String?
        if(queue!!.isEmpty()){
            str = ""
        }else{
            str = ",${getString(R.string.wait2download)}".replace("n", queue!!.size.toString())
        }
        builder?.setProgress(max, progress, false)?.setContentTitle("${getString(R.string.downloading)}:$progress/$max$str")
        mNotificationManager?.notify(NOTIFYID, builder?.build())
    }
}