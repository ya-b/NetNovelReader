package com.netnovelreader.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.netnovelreader.data.database.ChapterSQLManager
import com.netnovelreader.data.network.ParseHtml
import com.netnovelreader.utils.getSavePath
import com.netnovelreader.utils.mkdirs
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue

/**
 * Created by yangbo on 18-1-15.
 */
class DownloadService : Service() {
    var queue: LinkedBlockingQueue<DownloadTask>? = null

    var flag = true
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        queue = LinkedBlockingQueue<DownloadTask>()
        val threadPool = Executors.newFixedThreadPool(5)
        while (flag){
            queue!!.take().getRunnables().forEach{
                threadPool.execute(it)
            }
        }
        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("=====Service onstart", "==============")
        if(queue != null || intent != null){
            queue!!.offer(DownloadTask(intent!!.getStringExtra("localpath"),
                    intent.getStringExtra("catalogurl"),
                    intent.getBooleanExtra("iscatalog", false)))
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    class DownloadTask(val savename: String, val url: String, val isCatalog: Boolean){
        fun getRunnables(): ArrayList<Runnable> {
            val dir = mkdirs(getSavePath()+ "/$savename")
            var runnables: ArrayList<Runnable>? = null
            if(isCatalog){
                val map = ParseHtml().getCatalog(url)
                runnables = ArrayList<Runnable>(map.size)
                Thread{
                    ChapterSQLManager().createTable(savename).addAllChapter(map, savename)
                }.start()
                val iterator = map.iterator()
                while (iterator.hasNext()){
                    val entry = iterator.next()
                    runnables.add(ChapterRunnable(savename, dir, entry.key, entry.value))
                }
            }
            return runnables ?: ArrayList()
        }
    }

    class ChapterRunnable(val savename: String, val dir: String, val filename: String, val url:String) : Runnable{
        override fun run() {
            var fos: FileOutputStream? = null
            try{
                Log.d("=====Service ondownload", "$dir==$filename")
                fos = FileOutputStream(File(dir, filename))
                fos.write(ParseHtml().getChapter(url).toByteArray())
                ChapterSQLManager().finishChapter(savename, filename, true)
            }catch (e: Exception){
                ChapterSQLManager().finishChapter(savename, filename, false)
            }finally {
                fos?.close()
            }
        }
    }
}