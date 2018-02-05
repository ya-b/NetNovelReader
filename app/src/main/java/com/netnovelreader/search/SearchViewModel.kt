package com.netnovelreader.search

import android.util.Log
import com.netnovelreader.common.*
import com.netnovelreader.common.data.SQLHelper
import com.netnovelreader.common.data.SearchBook
import com.netnovelreader.common.download.CatalogCache
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URLEncoder
import java.util.concurrent.Executors

/**
 * Created by yangbo on 18-1-14.
 */
class SearchViewModel : ISearchContract.ISearchViewModel {
    @Volatile
    private var searchCode = 0
    var resultList: ObservableSyncArrayList<SearchBean>

    init {
        resultList = ObservableSyncArrayList()
    }

    /**
     * 添加书到数据库
     */
    override fun addBookToShelf(bookname: String, url: String): String {
        return id2TableName(SQLHelper.addBookToShelf(bookname, url))
    }

    override fun searchBook(bookname: String?) {
        bookname ?: return
        searchCode++
        resultList.clear()
        CatalogCache.clearCache()
        val executor = Executors.newFixedThreadPool(5)
        SQLHelper.queryAllSearchSite().forEach {
            executor.execute({
                try{
                    searchBookFromSite(bookname, it, searchCode)      //查询所有搜索站点设置，然后逐个搜索
                }catch (e: IOException){
                    e.printStackTrace()
                }
            })
        }
    }

    override fun saveBookImage(tableName: String, bookname: String) {
        val imageDir = File(getSavePath() + "/tmp")
        if (!imageDir.exists()) return
        Thread {
            imageDir.listFiles { dir, name -> name.startsWith(bookname) }.firstOrNull()
                    ?.copyTo(File(File(mkdirs(getSavePath() + "/$tableName")), IMAGENAME), true)
        }.start()
    }

    //删除目标及之后的章节,换源重新下载
    fun delChapterAfterSrc(tableName: String, chapterName: String) {
        val fileDir = File(getSavePath() + "/$tableName")
        if (!fileDir.exists()) return
        SQLHelper.delChapterAfterSrc(tableName, chapterName).map { File(fileDir, it) }.forEach { it.delete() }
    }

    //从具体网站搜索，并添加到resultList
    @Throws(IOException::class)
    private fun searchBookFromSite(bookname: String, siteinfo: Array<String?>, reqCode: Int) {
        val result: Array<String>
        val url = siteinfo[1]!!.replace(SQLHelper.SEARCH_NAME, URLEncoder.encode(bookname, siteinfo[7]))
        if (siteinfo[0].equals("0")) {
            result = SearchBook().search(
                    url,
                    siteinfo[4] ?: "",
                    siteinfo[6] ?: "",
                    siteinfo[9] ?: ""
            )
        } else {
            result = SearchBook().search(
                    url, siteinfo[2] ?: "", siteinfo[3] ?: "",
                    siteinfo[4] ?: "", siteinfo[5] ?: "",
                    siteinfo[6] ?: "", siteinfo[8] ?: "", siteinfo[9] ?: ""
            )
        }
        if (searchCode == reqCode && result[1].length > 0) { //result[1]==bookname,result[0]==catalogurl
            CatalogCache.addCatalog(result[1], result[0])
            val bean = CatalogCache.cache.get(result[0])
            if (bean != null && bean.url.get() != null) {
                resultList.add(bean)
            }
        }
        downloadImage(result[1], result[2])               //下载书籍封面图片
    }

    @Throws(IOException::class)
    private fun downloadImage(bookname: String, imageUrl: String) {
        if (imageUrl.equals("")) return
        val request = Request.Builder().url(imageUrl).build()
        val inputStream = OkHttpClient().newCall(request).execute().body()?.byteStream()
        val path = mkdirs(getSavePath() + "/tmp") + "/$bookname.${url2Hostname(imageUrl)}"
        val outputStream = FileOutputStream(path)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
    }
}