package com.netnovelreader.service

import com.netnovelreader.data.database.ChapterSQLManager
import com.netnovelreader.data.network.ParseHtml
import com.netnovelreader.utils.getSavePath
import com.netnovelreader.utils.mkdirs
import java.io.File
import java.io.FileWriter
import java.net.SocketTimeoutException
import kotlin.collections.ArrayList

/**
 * Created by yangbo on 2018/1/16.
 */
class DownloadTask(val tableName: String, val url: String){
    var chapterName: String? = null

    @Throws(SocketTimeoutException::class)
    fun getRunnables(): ArrayList<ChapterRunnable> {
        val dir = mkdirs(getSavePath() + "/$tableName")
        var runnables: ArrayList<ChapterRunnable>? = null
        //TODO chapterName != null
        if(chapterName == null){
            runnables = formCatalog(dir)
        }
        return runnables ?: ArrayList()
    }

    @Throws(SocketTimeoutException::class)
    fun formCatalog(dir: String): ArrayList<ChapterRunnable>{
        val sqlManager = ChapterSQLManager()
        val map = ParseHtml().getCatalog(url!!)
        var alreadyExists: ArrayList<String>? = null
        if(sqlManager.isTableExists(tableName!!)){
            alreadyExists = sqlManager.getDownloaded(tableName!!)
        }else{
            sqlManager.createTable(tableName!!).addAllChapter(map, tableName!!)
        }
        var runnables = ArrayList<ChapterRunnable>()
        val iterator = map.iterator()
        while (iterator.hasNext()){
            val entry = iterator.next()
            if(alreadyExists == null || !alreadyExists.contains(entry.key)){
                runnables.add(ChapterRunnable(tableName!!, dir, entry.key, entry.value))
            }
        }
        sqlManager.closeDB()
        return runnables
    }


    /**
     * 下载保存章节具体执行者，实现runnable接口，线程池执行
     */
    class ChapterRunnable(val tablename: String, val dir: String, val chapterName: String, val chapterUrl:String) : Runnable{
        lateinit var eON: () -> Unit ?

        override fun run() {
            var fos: FileWriter? = null
            var dbm = ChapterSQLManager()
            try{
                fos = FileWriter(File(dir, chapterName))
                fos.write(ParseHtml().getChapter(chapterUrl))
                fos.flush()
                dbm.setChapterFinish(tablename, chapterName, true, chapterUrl)
            }catch (e: Exception){
                dbm.setChapterFinish(tablename, chapterName, false, chapterUrl)
            }finally {
                fos?.close()
                dbm.closeDB()
                eON()
            }
        }

        fun setFun(eON: () -> Unit): Runnable{
            this.eON = eON
            return this
        }
    }
}