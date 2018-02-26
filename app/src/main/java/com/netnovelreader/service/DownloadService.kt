package com.netnovelreader.service

import android.app.IntentService
import android.app.NotificationManager
import android.content.Intent
import android.support.v4.app.NotificationCompat
import com.netnovelreader.R
import com.netnovelreader.ReaderApplication.Companion.threadPool
import com.netnovelreader.common.getSavePath
import com.netnovelreader.common.toast
import com.netnovelreader.data.db.ReaderDbManager
import com.netnovelreader.data.network.DownloadCatalog
import com.netnovelreader.data.network.DownloadChapter
import kotlinx.coroutines.experimental.launch
import java.io.File
import java.io.IOException
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
        try {
            getList(tableName!!, catalogUrl!!).apply { max = this.size }.forEach {
                //获取要下载的章节列表
                launch(threadPool) {
                    it.download(it.getChapterTxt())     //下载每一章
                }.invokeOnCompletion {
                    synchronized(IntentService::class.java) {
                        it?.apply { failed.incrementAndGet() } ?: progress.incrementAndGet()
                        updateNotification(progress.get(), max)
                        if (progress.get() + failed.get() == max) lock.offer(1)   //下载完成，取消阻塞
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        lock.take()  //用阻塞队列阻塞住线程，一次只下载一本书
    }

    override fun onDestroy() {
        super.onDestroy()
        mNotificationManager?.cancel(NOTIFYID)
        if (failed.get() > 0) {
            toast(getString(R.string.downloadfailed).replace("nn", "$failed"))
        }
        ReaderDbManager.closeDB()
    }

    private fun openNotification() {
        builder = NotificationCompat.Builder(this, "reader")
            .setTicker(getString(R.string.app_name))
            .setContentTitle(getString(R.string.prepare_download))
            .setSmallIcon(R.drawable.notification_icon)
        mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager?.notify(NOTIFYID, builder?.build())
    }

    fun updateNotification(progress: Int, max: Int) {
        val str = if (remainder != 0) ",${getString(R.string.wait4download)}"
            .replace("nn", "$remainder") else ""
        builder?.setProgress(max, progress, false)
            ?.setContentTitle("${getString(R.string.downloading)}:$progress/$max$str")
        mNotificationManager?.notify(NOTIFYID, builder?.build())
    }

    @Throws(IOException::class)
    fun getList(tableName: String, url: String): ArrayList<DownloadChapter> {
        DownloadCatalog(tableName, url).download()
        val runnables = ArrayList<DownloadChapter>()
        ReaderDbManager.getChapterNameAndUrl(tableName, 0).forEach {
            runnables.add(
                DownloadChapter(
                    tableName,
                    "${getSavePath()}/$tableName".apply { File(this).mkdirs() }, it.key, it.value
                )
            )
        }
        return runnables
    }
}