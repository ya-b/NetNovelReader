package com.netnovelreader.data.network

import com.netnovelreader.common.getSavePath
import com.netnovelreader.data.db.ReaderDbManager
import java.io.File
import java.io.IOException

/**
 * Created by yangbo on 2018/1/16.
 */
class DownloadTask(val tableName: String, val url: String) {

    @Throws(IOException::class)
    fun getList(): ArrayList<DownloadChapter> {
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