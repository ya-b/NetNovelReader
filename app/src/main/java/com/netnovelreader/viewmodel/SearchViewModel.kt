package com.netnovelreader.viewmodel

import android.app.Application
import android.app.Dialog
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.database.Cursor
import android.database.MatrixCursor
import android.databinding.ObservableArrayList
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v4.content.ContextCompat
import com.netnovelreader.R
import com.netnovelreader.ReaderApplication.Companion.threadPool
import com.netnovelreader.bean.NovelIntroduce
import com.netnovelreader.bean.SearchBean
import com.netnovelreader.bean.SearchHotWordsBean
import com.netnovelreader.common.IMAGENAME
import com.netnovelreader.common.enqueueCall
import com.netnovelreader.common.getSavePath
import com.netnovelreader.common.id2TableName
import com.netnovelreader.data.db.ReaderDbManager
import com.netnovelreader.data.db.ReaderSQLHelper
import com.netnovelreader.data.network.*
import com.netnovelreader.interfaces.ISearchContract
import kotlinx.coroutines.experimental.launch
import java.io.*
import java.net.URLEncoder

/**
 * Created by yangbo on 18-1-14.
 */
class SearchViewModel(val context: Application) : AndroidViewModel(context),
    ISearchContract.ISearchViewModel {
    @Volatile
    private var searchCode = 0
    val resultList by lazy {
        MutableLiveData<ObservableArrayList<SearchBean>>().run {
            value = ObservableArrayList(); value!!
        }
    }
    val isChangeSource =
        ObservableBoolean(false)                                          //是否显示搜索建议
    val showHotWord = ObservableBoolean(false)                                            //是否显示搜索热词
    val searchHotWords = Array<ObservableField<String>>(10) { ObservableField("") }  //显示的搜索热词
    val colors = Array(10) { ObservableInt(R.color.hot_label_bg1) }        //显示的搜索热词颜色
    var hotWordsTemp: List<SearchHotWordsBean>? = null                          //搜索热词(从中选取)

    private var queryText = ""
    private var queryTime = System.currentTimeMillis()

    private val colorArray by lazy {
        //搜索热词标签的背景颜色(从中选取)
        listOf(
            R.color.hot_label_bg1,
            R.color.hot_label_bg2,
            R.color.hot_label_bg3,
            R.color.hot_label_bg4,
            R.color.hot_label_bg5,
            R.color.hot_label_bg6,
            R.color.hot_label_bg7,
            R.color.hot_label_bg8,
            R.color.hot_label_bg9,
            R.color.hot_label_bg10,
            R.color.hot_label_bg11,
            R.color.hot_label_bg12,
            R.color.hot_label_bg13,
            R.color.hot_label_bg14,
            R.color.hot_label_bg15,
            R.color.hot_label_bg16,
            R.color.hot_label_bg17
        ).map { ContextCompat.getColor(context, it) }
    }

    override fun refreshHotWords() = launch {
        showHotWord.set(false)
        if (hotWordsTemp == null) {
            hotWordsTemp = try {
                ApiManager.mAPI.hotWords().execute().body()?.searchHotWords
            } catch (e: IOException) {
                null
            }
            hotWordsTemp?.filter { it.word != null && it.word!!.length > 1 }
                ?.takeIf { it.size > 50 }?.run { hotWordsTemp = this }
        }
        hotWordsTemp = hotWordsTemp?.shuffled() ?: return@launch
        val colorSource = colorArray.shuffled()
        for (i in 0 until (hotWordsTemp?.size ?: -1)) {
            if (i > searchHotWords.size - 1) break
            searchHotWords[i].set(hotWordsTemp?.get(i)?.word ?: "")
            colors[i].set(colorSource[i])
        }
        showHotWord.set(true)
    }

    override fun onQueryTextChange(newText: String?): Cursor? {
        resultList.clear()
        return if (newText!!.isEmpty()) {
            showHotWord.set(true)
            null
        } else {
            showHotWord.set(false)
            searchBookSuggest(newText)
        }
    }

    override suspend fun searchBook(bookname: String?, chapterName: String?) {
        showHotWord.set(false)
        if (queryText == bookname && System.currentTimeMillis() - queryTime < 1000) return
        bookname ?: return
        searchCode++
        resultList.clear()
        CatalogCache.clearCache()
        ReaderDbManager.queryAllSearchSite().forEach {
            launch(threadPool) {
                // Logger.i("步骤1.正准备从网站【${it[1]}】搜索图书【${bookname}】")
                try {
                    searchBookFromSite(
                        bookname,
                        it,
                        searchCode,
                        chapterName
                    )      //查询所有搜索站点设置，然后逐个搜索
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        queryText = bookname
        queryTime = System.currentTimeMillis()
    }


    /**
     * @param bookname 书名
     * @param catalogUrl 目录页地址
     * @param chapterName 章节名称
     * @param which dialog按键
     * @return "1"表示只下载目录页， "tableName"表示下载全书， "0"表示下载目录失败
     */
    override suspend fun downloadCatalog(
        bookname: String, catalogUrl: String, chapterName: String?, which: Int
    ): String {
        val tableName = id2TableName(ReaderDbManager.addBookToShelf(bookname, catalogUrl))
        saveBookImage(tableName, bookname)
        return try {
            DownloadCatalog(tableName, catalogUrl).download()
            if (which == Dialog.BUTTON_POSITIVE) {
                downloadBook(tableName, catalogUrl, chapterName)
                tableName
            } else {
                downCurrentChapter(tableName, chapterName)
                "1"
            }
        } catch (e: IOException) {
            "0"
        }
    }

    override suspend fun detailClick(itemText: String): NovelIntroduce? = try {
        ApiManager.mAPI.searchBook(itemText).execute().body()
            ?.books
            ?.firstOrNull { it.title == itemText }
            ?._id
            ?.let { ApiManager.mAPI.getNovelIntroduce(it).execute().body() }
    } catch (e: IOException) {
        null
    }

    /**
     * 在搜索框输入过程中匹配一些输入项并提示
     */
    private fun searchBookSuggest(queryText: String): Cursor? {
        return try {
            val suggestCursor = MatrixCursor(arrayOf("text", "_id"))
            ApiManager.mAPI.searchSuggest(queryText, "com.ushaqi.zhuishushenqi")
                .execute().body()?.keywords?.filter { it.tag == "bookname" }
                ?.map { it.text }?.toHashSet()
                ?.forEachIndexed { index, s -> suggestCursor.addRow(arrayOf(s, index)) }
            suggestCursor
        } catch (e: IOException) {
            null
        }
    }

    //从具体网站搜索，并添加到resultList
    @Throws(IOException::class)
    private fun searchBookFromSite(
        bookname: String,
        siteinfo: Array<String?>,
        reqCode: Int,
        chapterName: String?
    ) {
        val result = SearchBook().search(
            siteinfo[1]!!.replace(
                ReaderSQLHelper.SEARCH_NAME,
                URLEncoder.encode(bookname, siteinfo[7])
            ),
            siteinfo[2] ?: "",
            siteinfo[3] ?: "",
            siteinfo[4] ?: "",
            siteinfo[5] ?: "",
            siteinfo[6] ?: "",
            siteinfo[8] ?: "",
            siteinfo[9] ?: ""
        )
        if (searchCode == reqCode && result[1].isNotEmpty()) { //result[1]==bookname,result[0]==catalogurl
            CatalogCache.addCatalog(result[1], result[0])
            val bean = CatalogCache.cache[result[0]]
            if (bean != null && !bean.url.get().isNullOrEmpty() && !bean.latestChapter.get().isNullOrEmpty()) {
                if (chapterName.isNullOrEmpty() || bean.catalogMap.containsKey(chapterName)) {
                    resultList.add(bean)
                }
            }
            launch { downloadImage(result[1], result[2]) }           //下载书籍封面图片
        }
    }

    //换源下载，只下载当前章节
    @Throws(IOException::class)
    private fun downCurrentChapter(tableName: String, chapterName: String?) {
        if (!chapterName.isNullOrEmpty()) {
            delChapterAfterSrc(tableName, chapterName!!)
            DownloadChapter(
                tableName, "${getSavePath()}/$tableName",
                chapterName, ReaderDbManager.getChapterUrl(tableName, chapterName)
            ).apply { download(getChapterTxt()) }
        }
    }

    //下载全书，若该书已存在，则下载所有未读章节
    private fun downloadBook(tableName: String, catalogUrl: String, chapterName: String?) {
        if (!chapterName.isNullOrEmpty()) {
            delChapterAfterSrc(tableName, chapterName!!)
            DownloadCatalog(tableName, catalogUrl).download()
        }
    }

    //删除目标及之后的章节,换源重新下载
    private fun delChapterAfterSrc(tableName: String, chapterName: String) {
        val list = ReaderDbManager.delChapterAfterSrc(tableName, chapterName)
        File(getSavePath() + "/$tableName") //目录
            .takeIf { it.exists() }             //是否存在
            ?.let { list.map { item -> File(it, item) }.forEach { it.delete() } }
    }

    //下载书籍图片，搜索时调用(搜索时顺便获取图片链接)
    private fun downloadImage(bookname: String, imageUrl: String) {
        val path = "${getSavePath()}/tmp".apply { File(this).mkdirs() } + "/$bookname.png"
        if (imageUrl != "" && !File(path).exists()) {
            //  Logger.i("步骤2.从网站下载图书【$bookname】的图片,URL为【$imageUrl】")
            ApiManager.mAPI.getPicture(imageUrl).enqueueCall {
                var inputStream: InputStream? = null
                var outputStream: OutputStream? = null
                try {
                    inputStream = it?.byteStream()
                    outputStream = FileOutputStream(path)
                    BitmapFactory.decodeStream(inputStream)
                        .compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.flush()
                } catch (e: IOException) {

                } finally {
                    inputStream?.close()
                    outputStream?.close()
                }
            }
        }
    }

    private fun saveBookImage(tableName: String, bookname: String) {
        File(getSavePath() + "/tmp")
            .takeIf { it.exists() }
            ?.listFiles { _, name -> name.startsWith(bookname) }
            ?.firstOrNull()
            ?.copyTo(
                File(
                    "${getSavePath()}/$tableName"
                        .apply { File(this).mkdirs() }, IMAGENAME
                ), true
            )
    }
}