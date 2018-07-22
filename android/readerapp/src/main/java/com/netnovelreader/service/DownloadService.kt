package com.netnovelreader.service

import android.app.IntentService
import android.app.NotificationManager
import android.content.Intent
import android.support.v4.app.NotificationCompat
import com.netnovelreader.R
import com.netnovelreader.repo.SearchRepo
import com.netnovelreader.repo.http.resp.ChapterInfoResp
import com.netnovelreader.repo.http.resp.SearchBookResp
import com.netnovelreader.utils.IO_EXECUTOR
import com.netnovelreader.utils.mkBookDir
import com.netnovelreader.utils.toast
import com.netnovelreader.utils.uiThread
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class DownloadService : IntentService {
    constructor() : super("DownloadService")
    constructor(name: String) : super(name)

    companion object {
        const val INFO = "info"
    }

    private lateinit var repo: SearchRepo
    private var mNotificationManager: NotificationManager? = null
    private var builder: NotificationCompat.Builder? = null
    private val notyfyId = 1599407175

    private var max = AtomicInteger()          //下载总数
    private var progress = AtomicInteger()     //下载完成数（包括失败的下载）
    private var failed = AtomicInteger()       //失败下载数
    private val taskList by lazy { LinkedList<SearchBookResp>() }

    override fun onCreate() {
        super.onCreate()
        repo = SearchRepo(application)
        openNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getParcelableExtra<SearchBookResp>(INFO)?.also {
            if(taskList.contains(it)) {
                toast(getString(R.string.already_in_task))
            } else {
                taskList.add(it)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onHandleIntent(intent: Intent?) {
        download()
    }

    override fun onDestroy() {
        super.onDestroy()
        mNotificationManager?.cancel(notyfyId)

    }

    private fun openNotification() {
        builder = NotificationCompat.Builder(this, "reader")
            .setTicker(getString(R.string.app_name))
            .setContentTitle(getString(R.string.prepare_download))
            .setSmallIcon(R.mipmap.ic_launcher)
        mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager?.notify(notyfyId, builder?.build())
    }

    private fun updateNotification(progress: Int, max: Int, failed: Int, remain: Int) {
        val str = if(progress == max) {
            String.format(getString(R.string.download_finish), failed)
        } else {
            String.format(
                getString(R.string.downloading),
                progress,
                max,
                failed,
                remain
            )
        }
        builder?.setProgress(max, progress, false)
            ?.setContentTitle(str)
        uiThread { mNotificationManager?.notify(notyfyId, builder?.build()) }
    }

    private fun download() {
        if (taskList.size == 0) return
        repo.getCatalog(taskList[0]) { _, chapterList ->
            chapterList ?: return@getCatalog
            max.set(chapterList.size)
            if(!mkBookDir(taskList[0].bookname)) {
                //todo 创建文件夹出错
                return@getCatalog
            }
            Observable.fromIterable(chapterList)
                .flatMap { info ->
                    Observable.create<Pair<ChapterInfoResp, Boolean>> {
                        repo.downloadChapter(taskList[0].bookname, info)
                        it.onNext(Pair(info, true))
                        it.onComplete()
                    }.subscribeOn(Schedulers.from(IO_EXECUTOR))
                }.subscribe(
                    {
                        updateNotification(
                            progress.incrementAndGet(),
                            max.get(),
                            failed.get(),
                            taskList.size - 1
                        )
                    },
                    {
                        updateNotification(
                            progress.get(),
                            max.get(),
                            failed.incrementAndGet(),
                            taskList.size - 1
                        )
                    },
                    {
                        updateNotification(max.get(), max.get(), failed.get(), taskList.size - 1)
                        taskList.removeFirst()
                        download()
                    }
                )
        }
    }
}