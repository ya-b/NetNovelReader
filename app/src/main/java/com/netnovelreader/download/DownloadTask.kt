package com.netnovelreader.download

import com.netnovelreader.common.getSavePath
import com.netnovelreader.common.mkdirs
import com.netnovelreader.data.SQLHelper
import java.io.IOException

/**
 * Created by yangbo on 2018/1/16.
 */
class DownloadTask(val tableName: String, val url: String) {

    @Throws(IOException::class)
    fun downloadAll(): ArrayList<DownloadChapter> {
        DownloadCatalog(tableName, url).download()
        val dir = mkdirs(getSavePath() + "/$tableName")
        val runnables = ArrayList<DownloadChapter>()
        SQLHelper.getChapterList(tableName, 0).forEach {
            runnables.add(DownloadChapter(tableName, dir, it.key, it.value))
        }
        return runnables
    }
}