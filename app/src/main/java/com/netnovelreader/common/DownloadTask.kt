package com.netnovelreader.common

import com.netnovelreader.data.ParseHtml
import com.netnovelreader.data.SQLHelper
import java.io.File
import java.io.FileWriter
import java.io.IOException

/**
 * Created by yangbo on 2018/1/16.
 */
class DownloadTask(val tableName: String, val url: String) {
    private var chapterName: String? = null

    @Throws(IOException::class)
    fun getUnitList(): ArrayList<DownloadChapterUnit> {
        val dir = mkdirs(getSavePath() + "/$tableName")
        var unitList: ArrayList<DownloadChapterUnit>? = null
        //TODO chapterName != null
        if (chapterName == null) {
            updateSql()
            unitList = getUnDownloadFromSql(dir, tableName)
        } else {

        }
        return unitList ?: ArrayList()
    }

    @Throws(IOException::class)
    fun updateSql() {
        val map = ParseHtml().getCatalog(url)
        SQLHelper.createTable(tableName)
        val chapterInSql = SQLHelper.getAllChapter(tableName)
        val iterator = map.iterator()
        var entry: MutableMap.MutableEntry<String, String>? = null
        while (iterator.hasNext()) {
            entry = iterator.next()
            if (!chapterInSql.contains(entry.key)) {
                SQLHelper.setChapterFinish(tableName, entry.key, entry.value, false)
            }
        }
        if (entry != null) {
            synchronized(SQLHelper) {
                SQLHelper.getDB().execSQL("update ${SQLHelper.TABLE_SHELF} set " +
                        "${SQLHelper.LATESTCHAPTER}='${entry.key}' where " +
                        "${SQLHelper.ID}=${tableName2Id(tableName)}")
            }
        }
    }

    @Throws(IOException::class)
    private fun getUnDownloadFromSql(saveDir: String, tableName: String): ArrayList<DownloadChapterUnit> {
        val map = SQLHelper.getDownloadedOrNot(tableName, 0)
        val runnables = ArrayList<DownloadChapterUnit>()
        val iterator = map.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            runnables.add(DownloadChapterUnit(tableName, saveDir, entry.key, entry.value))
        }
        return runnables
    }


    /**
     * 下载保存章节具体执行者
     */
    class DownloadChapterUnit(private val tablename: String, private val dir: String, private val chapterName: String,
                              private val chapterUrl: String) {

        @Throws(IOException::class)
        fun download(chapterText: String): Int {
            var fos: FileWriter? = null
            try {
                fos = FileWriter(File(dir, chapterName))
                fos.write(chapterText)
                fos.flush()
                SQLHelper.setChapterFinish(tablename, chapterName, chapterUrl, true)
            } catch (e: Exception) {
                SQLHelper.setChapterFinish(tablename, chapterName, chapterUrl, false)
            } finally {
                fos?.close()
                return 1
            }
        }

        @Throws(IOException::class)
        fun getChapterTxt(): String = ParseHtml().getChapter(chapterUrl)
    }
}