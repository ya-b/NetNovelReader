package com.netnovelreader.viewmodel

import android.app.Application
import android.app.Dialog
import android.arch.lifecycle.AndroidViewModel
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
import com.netnovelreader.bean.SearchHotWord
import com.netnovelreader.common.*
import com.netnovelreader.data.db.ReaderDatabase
import com.netnovelreader.data.db.ReaderDbManager
import com.netnovelreader.data.db.SitePreferenceBean
import com.netnovelreader.data.network.ApiManager
import com.netnovelreader.data.network.CatalogCache
import com.netnovelreader.data.network.DownloadCatalog
import com.netnovelreader.data.network.SearchBook
import kotlinx.coroutines.experimental.launch
import java.io.*
import java.net.URLEncoder

/**
 * Created by yangbo on 18-1-14.
 */
class SearchViewModel(val context: Application) : AndroidViewModel(context) {
    @Volatile
    private var searchCode = 0
    val resultList = ObservableArrayList<SearchBean>()             //搜索结果
    val isChangeSource = ObservableBoolean(false)            //是否显示搜索建议
    val showHotWord = ObservableBoolean(false)               //是否显示搜索热词
    val searchHotWords = Array<ObservableField<String>>(10) { ObservableField("") }  //显示的搜索热词
    val colors = Array(10) { ObservableInt(R.color.hot_label_bg1) }  //显示的搜索热词颜色
    var hotWordsTemp: List<SearchHotWord.SearchHotWordsBean>? = null      //搜索热词(从中选取)
    val isLoading = ObservableBoolean(false)                        //loadingbar是否显示
    val toastMessage = ReaderLiveData<String>()                       //toast要显示的信息
    val exitCommand = ReaderLiveData<Void>()                          //点击返回图标
    val selectHotWordEvent = ReaderLiveData<String>()               //选中的hotword的text
    val showBookDetailCommand = ReaderLiveData<NovelIntroduce>()      //点击的item的所需数据NovelIntroduce
    val showDialogCommand = ReaderLiveData<SearchBean>()              //点击的item的下载事件所需数据
    val downLoadChapterCommand =
        ReaderLiveData<Array<String>>()      //启动[DownloadService]的ExtraString
    private var queryTextTemp = ""
    private var queryTimeTemp = System.currentTimeMillis()

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

    fun refreshHotWords() = launch {
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

    fun onQueryTextChange(newText: String?): Cursor? {
        resultList.clear()
        return if (newText!!.isEmpty()) {
            if (hotWordsTemp != null) {
                showHotWord.set(true)
            }
            null
        } else {
            showHotWord.set(false)
            searchBookSuggest(newText)
        }
    }

    fun searchBook(bookname: String?, chapterName: String?) {
        showHotWord.set(false)
        if (queryTextTemp == bookname && System.currentTimeMillis() - queryTimeTemp < 1000) return
        if (bookname.isNullOrEmpty()) return
        searchCode++
        resultList.clear()
        CatalogCache.clearCache()
        ReaderDbManager.getRoomDB().sitePreferenceDao().getAll().apply { isLoading.set(!isEmpty()) }
            .forEach {
                launch(threadPool) {
                    // Logger.i("步骤1.正准备从网站【${it[1]}】搜索图书【${bookname}】")
                    try {
                        //查询所有搜索站点设置，然后逐个搜索
                        searchBookFromSite(bookname!!, it, searchCode, chapterName)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    isLoading.set(false)
                }
            }
        queryTextTemp = bookname!!
        queryTimeTemp = System.currentTimeMillis()
    }


    /**
     * @param bookname 书名
     * @param catalogUrl 目录页地址
     * @param chapterName 章节名称
     * @param which dialog按键
     * @return "1"表示只下载目录页， "tableName"表示下载全书， "0"表示下载目录失败
     */
    fun downloadBook(bookname: String, catalogUrl: String, chapterName: String?, which: Int) =
        launch {
            ReaderDbManager.getRoomDB().shelfDao()
                .replace(bookName = bookname, downloadUrl = catalogUrl)
            saveBookImage(bookname)
            if (isChangeSource.get()) {
                delChapterAfterSrc(bookname, chapterName!!)
            }
            try {
                DownloadCatalog(bookname, catalogUrl).download()
                toastMessage.value = this@SearchViewModel.context.getString(R.string.catalog_finish)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (which == Dialog.BUTTON_POSITIVE) {
                downLoadChapterCommand.value = arrayOf(bookname, catalogUrl)
            }
        }

    fun showDialogTask(itemDetail: SearchBean) {
        showDialogCommand.value = itemDetail
    }

    fun detailClickTask(itemText: String) {
        ApiManager.mAPI.searchBook(itemText).enqueueCall {
            it?.books?.firstOrNull { it.title == itemText }?._id?.let {
                ApiManager.mAPI.getNovelIntroduce(it).enqueueCall {
                    if (it == null) {
                        toastMessage.value = "没有搜索到相关小说的介绍"
                    } else {
                        showBookDetailCommand.value = it
                    }
                }
            }
        }
    }

    fun activityExitTask() {
        exitCommand.call()
    }

    //将搜索热词填充到searchView上但是不触发网络请求
    fun selectHotWordTask(word: String) {
        selectHotWordEvent.value = word
    }

    /**
     * 在搜索框输入过程中匹配一些输入项并提示
     */
    private fun searchBookSuggest(queryText: String): Cursor? = try {
        val suggestCursor = MatrixCursor(arrayOf("text", "_id"))
        ApiManager.mAPI.searchSuggest(queryText, "com.ushaqi.zhuishushenqi")
            .execute().body()?.keywords?.filter { it.tag == "bookname" }
            ?.map { it.text }?.toHashSet()
            ?.forEachIndexed { index, s -> suggestCursor.addRow(arrayOf(s, index)) }
        suggestCursor
    } catch (e: IOException) {
        null
    }

    //从具体网站搜索，并添加到resultList
    @Throws(IOException::class)
    private fun searchBookFromSite(
        bookname: String,
        siteinfo: SitePreferenceBean,
        reqCode: Int,
        chapterName: String?
    ) {
        val result = SearchBook().search(
            siteinfo.searchUrl.replace(
                ReaderDatabase.SEARCH_NAME,
                URLEncoder.encode(bookname, siteinfo.charset)
            ),
            siteinfo.redirectFileld,
            siteinfo.redirectUrl,
            siteinfo.noRedirectUrl,
            siteinfo.redirectName,
            siteinfo.noRedirectName,
            siteinfo.redirectImage,
            siteinfo.noRedirectImage
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
                    e.printStackTrace()
                } finally {
                    inputStream?.close()
                    outputStream?.close()
                }
            }
        }
    }

    private fun saveBookImage(bookname: String) {
        File(getSavePath() + "/tmp")
            .takeIf { it.exists() }
            ?.listFiles { _, name -> name.startsWith(bookname) }
            ?.firstOrNull()
            ?.copyTo(
                File("${getSavePath()}/$bookname".apply { File(this).mkdirs() }, IMAGENAME),
                true
            )
    }
}