package com.netnovelreader.search

import com.netnovelreader.api.ApiManager
import com.netnovelreader.api.bean.KeywordsBean
import com.netnovelreader.common.*
import com.netnovelreader.common.data.SQLHelper
import com.netnovelreader.common.data.SearchBook
import com.netnovelreader.common.download.CatalogCache
import com.orhanobut.logger.Logger
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newFixedThreadPoolContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URLEncoder

/**
 * Created by yangbo on 18-1-14.
 */
class SearchViewModel : ISearchContract.ISearchViewModel {
    @Volatile
    private var searchCode = 0
    var resultList: ObservableSyncArrayList<SearchBean> = ObservableSyncArrayList()
    var searchSuggestResultList: ObservableSyncArrayList<KeywordsBean> = ObservableSyncArrayList()   //输入部分书名自动补全提示
    private val poolContext = newFixedThreadPoolContext(6, "DownloadService")         //新建一个名为“DownloadService”线程池调度器，线程数目为2/3cpu核心数
    /**
     * 在搜索框输入过程中匹配一些输入项并提示
     */
    fun searchBookSuggest(queryText: String) {
        ApiManager.mAPI!!.searchSuggest(queryText, "com.ushaqi.zhuishushenqi")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe {
                    searchSuggestResultList.clear()
                    searchSuggestResultList.addAll(it.keywords!!)
                }
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
        launch {
            resultList.clear()
            CatalogCache.clearCache()

            SQLHelper.queryAllSearchSite().forEach {
                launch(poolContext) {
                    //创建并马上启动一个协程执行任务
                  //  Logger.i("步骤1.正准备从网站【${it[1]}】搜索图书【${bookname}】")
                    searchBookFromSite(bookname, it, searchCode)      //查询所有搜索站点设置，然后逐个搜索
                }
            }
        }
    }

    override fun saveBookImage(tableName: String, bookname: String) {
        launch {
            File(getSavePath() + "/tmp")
                    .takeIf { it.exists() }
                    ?.listFiles { dir, name -> name.startsWith(bookname) }
                    ?.firstOrNull()
                    ?.copyTo(File("${getSavePath()}/$tableName".apply { File(this).mkdirs() }, IMAGENAME), true)
        }
    }

    //删除目标及之后的章节,换源重新下载
    fun delChapterAfterSrc(tableName: String, chapterName: String) {
        launch {
            val list = SQLHelper.delChapterAfterSrc(tableName, chapterName)
            File(getSavePath() + "/$tableName")
                    .takeIf { it.exists() }
                    ?.let { list.map { item -> File(it, item) }.forEach { it.delete() } }
        }
    }

    //从具体网站搜索，并添加到resultList
    @Throws(IOException::class)
    private fun searchBookFromSite(bookname: String, siteinfo: Array<String?>, reqCode: Int) {
        val url = siteinfo[1]!!.replace(SQLHelper.SEARCH_NAME, URLEncoder.encode(bookname, siteinfo[7]))
        val result = if (siteinfo[0].equals("0")) {    //是否重定向
            SearchBook().search(
                    url,
                    siteinfo[4] ?: "",
                    siteinfo[6] ?: "",
                    siteinfo[9] ?: ""
            )
        } else {
            SearchBook().search(
                    url, siteinfo[2] ?: "", siteinfo[3] ?: "",
                    siteinfo[4] ?: "", siteinfo[5] ?: "",
                    siteinfo[6] ?: "", siteinfo[8] ?: "", siteinfo[9] ?: ""
            )
        }
        if (searchCode == reqCode && result[1].isNotEmpty()) { //result[1]==bookname,result[0]==catalogurl

            CatalogCache.addCatalog(result[1], result[0])
            val bean = CatalogCache.cache[result[0]]
            if (bean != null && !bean.url.get().isNullOrEmpty()) {
                resultList.add(bean)
            }
        }
        downloadImage(result[1], result[2])               //下载书籍封面图片

    }

    @Throws(IOException::class)
    private fun downloadImage(bookname: String, imageUrl: String) {
        launch {
            if (imageUrl != "") {
              //  Logger.i("步骤2.从网站下载图书【$bookname】的图片,URL为【$imageUrl】")
                val request = Request.Builder().url(imageUrl).build()
                val inputStream = OkHttpClient().newCall(request).execute().body()?.byteStream()
                val path = "${getSavePath()}/tmp".apply { File(this).mkdirs() } + "/$bookname.${url2Hostname(imageUrl)}"
                val outputStream = FileOutputStream(path)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()
            }
        }
    }
}