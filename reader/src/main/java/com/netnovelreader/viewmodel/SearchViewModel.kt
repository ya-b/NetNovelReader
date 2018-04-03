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
import android.graphics.Color
import com.netnovelreader.R
import com.netnovelreader.ReaderApplication
import com.netnovelreader.ReaderApplication.Companion.threadPool
import com.netnovelreader.bean.NovelIntroduce
import com.netnovelreader.bean.SearchBookResult
import com.netnovelreader.bean.SearchHotWord
import com.netnovelreader.common.enqueueCall
import com.netnovelreader.common.replace
import com.netnovelreader.data.CatalogManager
import com.netnovelreader.data.local.ReaderDbManager
import com.netnovelreader.data.local.db.SitePreferenceBean
import com.netnovelreader.data.network.SearchBook
import com.netnovelreader.data.network.WebService
import kotlinx.coroutines.experimental.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class SearchViewModel(val context: Application) : AndroidViewModel(context) {
    val resultList by lazy { ObservableArrayList<SearchBookResult>() }            //搜索结果
    val isChangeSource by lazy { ObservableBoolean(false) }           //是否显示搜索建议
    val showHotWord by lazy { ObservableBoolean(false) }              //是否显示搜索热词
    val searchHotWords by lazy {
        //显示的搜索热词
        Array<ObservableField<String>>(10) { ObservableField("") }
    }
    val colors by lazy { Array(10) { ObservableInt(colorArray[0]) } }  //显示的搜索热词颜色
    var hotWordsTemp: List<SearchHotWord.SearchHotWordsBean>? = null        //搜索热词(从中选取)
    val isLoading by lazy { ObservableBoolean(false) }                //loadingbar是否显示
    val toastMessage by lazy { MutableLiveData<String>() }                   //toast要显示的信息
    val exitCommand by lazy { MutableLiveData<Void>() }                      //点击返回图标
    val selectHotWordEvent by lazy { MutableLiveData<String>() }             //选中的hotword的text
    val showBookDetailCommand by lazy { MutableLiveData<NovelIntroduce>() }  //点击的item的所需数据NovelIntroduce
    val showDialogCommand by lazy { MutableLiveData<SearchBookResult>() }          //点击的item的下载事件所需数据
    val downLoadChapterCommand by lazy { MutableLiveData<Array<String>>() }  //启动[DownloadService]的ExtraString
    private var queryTextTemp = ""
    private var queryTimeTemp = System.currentTimeMillis()
    private val colorArray by lazy {
        //搜索热词标签的背景颜色(从中选取)
        listOf(
                Color.parseColor("#627176"),
                Color.parseColor("#3fa5d9"),
                Color.parseColor("#E25754"),
                Color.parseColor("#ea6f5a"),
                Color.parseColor("#42c02e"),
                Color.parseColor("#2C2C2C"),
                Color.parseColor("#617c99"),
                Color.parseColor("#7E57C2"),
                Color.parseColor("#ff874d"),
                Color.parseColor("#f44336"),
                Color.parseColor("#18FFFF"),
                Color.parseColor("#0f0900"),
                Color.parseColor("#795548"),
                Color.parseColor("#FF6F00"),
                Color.parseColor("#69F0AE"),
                Color.parseColor("#651FFF"),
                Color.parseColor("#F06292")
        )
    }


    fun refreshHotWords() {
        showHotWord.set(false)
        hotWordsTemp ?: run {
            hotWordsTemp =
                    try {
                        WebService.zhuiShuShenQi.hotWords().execute().body()?.searchHotWords
                    } catch (e: IOException) {
                        null
                    }.let {
                        it?.filter { it.word != null && it.word!!.length > 1 }?.takeIf { it.size > 50 }
                                ?: it
                    }
        }
        hotWordsTemp = hotWordsTemp?.shuffled() ?: return
        val colorSource = colorArray.shuffled()
        for (i in 0 until hotWordsTemp!!.size) {
            if (i > searchHotWords.size - 1) break
            searchHotWords[i].set(hotWordsTemp?.get(i)?.word ?: "")
            colors[i].set(colorSource[i])
        }
        showHotWord.set(true)
    }

    fun onQueryTextChange(newText: String): Cursor? {
        resultList.clear()
        return if (newText.isEmpty()) {
            showHotWord.set(hotWordsTemp != null)
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
        resultList.clear()
        CatalogManager.clearCache()
        val list = ReaderDbManager.sitePreferenceDao().getAll().apply { isLoading.set(!isEmpty()) }
        list.forEach {
            launch(threadPool) {
                // Logger.i("步骤1.正准备从网站【${it[1]}】搜索图书【${bookname}】")
                try {
                    //查询所有搜索站点设置，然后逐个搜索
                    searchBookFromSite(bookname!!, it, chapterName)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                if (isLoading.get() && (resultList.isNotEmpty() || list.last() == it)) isLoading.set(
                        false
                )
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
    fun downloadBook(bookname: String, catalogUrl: String, chapterName: String?, which: Int) {
        launch { ReaderDbManager.shelfDao().replace(bookName = bookname, downloadUrl = catalogUrl) }
        saveBookImage(bookname)
        if (isChangeSource.get()) {
            delChapterAfterSrc(bookname, chapterName!!)
        }
        try {
            CatalogManager.download(bookname, catalogUrl)
            toastMessage.postValue(this@SearchViewModel.context.getString(R.string.catalog_finish))
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (which == Dialog.BUTTON_POSITIVE) {
            downLoadChapterCommand.postValue(arrayOf(bookname, catalogUrl))
        }
    }

    fun showDialog(itemDetail: SearchBookResult) {
        showDialogCommand.value = itemDetail
    }

    fun detailClick(itemText: String) {
        launch {
            val novelIntroduce = try {
                WebService.zhuiShuShenQi.searchBook(itemText).execute().body()
                        ?.books
                        ?.firstOrNull { it.title == itemText }
                        ?._id
                        ?.let { WebService.zhuiShuShenQi.getNovelIntroduce(it).execute().body() }
            } catch (e: IOException) {
                null
            }
            if (novelIntroduce == null) {
                toastMessage.postValue("没有搜索到相关小说的介绍")
            } else {
                showBookDetailCommand.postValue(novelIntroduce)
            }
        }
    }

    fun exit() {
        exitCommand.value = null
    }

    //将搜索热词填充到searchView上但是不触发网络请求
    fun selectHotWord(word: String) {
        selectHotWordEvent.value = word
    }

    /**
     * 在搜索框输入过程中匹配一些输入项并提示
     */
    private fun searchBookSuggest(queryText: String): Cursor? {
        val suggestCursor = MatrixCursor(arrayOf("text", "_id"))
        try {
            WebService.zhuiShuShenQi.searchSuggest(queryText, "com.ushaqi.zhuishushenqi")
                    .execute().body()
        } catch (e: IOException) {
            null
        }?.keywords?.filter { it.tag == "bookname" }
                ?.map { it.text }?.toHashSet()
                ?.forEachIndexed { index, s -> suggestCursor.addRow(arrayOf(s, index)) }
        return suggestCursor
    }

    /**
     * 从具体网站搜索，并添加到resultList
     * @chapterName 正在阅读的章节（换源下载）
     */
    @Throws(IOException::class)
    private fun searchBookFromSite(
            bookname: String,
            siteinfo: SitePreferenceBean,
            chapterName: String?
    ) {
        //result[1]==bookname,result[0]==catalogurl
        val result = SearchBook().search(bookname, siteinfo).takeIf { it[1].isNotEmpty() } ?: return
        CatalogManager.addToCache(result[1], result[0])
        val bean = CatalogManager.getFromCache(result[0])
                ?.takeIf { !it.url.get().isNullOrEmpty() }
                ?.takeIf { !it.latestChapter.get().isNullOrEmpty() }
                ?: return
        if (chapterName.isNullOrEmpty() || bean.catalogMap.containsKey(chapterName)) {
            resultList.add(bean)
        }
        launch { downloadImage(result[1], result[2]) }           //下载书籍封面图片
    }

    //删除目标及之后的章节,换源重新下载
    private fun delChapterAfterSrc(tableName: String, chapterName: String) {
        val list = ReaderDbManager.delChapterAfterSrc(tableName, chapterName)
        File(ReaderApplication.dirPath + "/$tableName") //目录
                .takeIf { it.exists() }             //是否存在
                ?.let { list.map { item -> File(it, item) }.forEach { it.delete() } }
    }

    //下载书籍图片，搜索时调用(搜索时顺便获取图片链接)
    private fun downloadImage(bookname: String, imageUrl: String) {
        val path =
                "${ReaderApplication.dirPath}/tmp".apply { File(this).mkdirs() } + "/$bookname.png"
        if (imageUrl != "" && !File(path).exists()) {
            //  Logger.i("步骤2.从网站下载图书【$bookname】的图片,URL为【$imageUrl】")
            WebService.novelReader.getPicture(imageUrl).enqueueCall {
                try {
                    it?.byteStream().use { ins ->
                        FileOutputStream(path).use { os ->
                            BitmapFactory.decodeStream(ins).compress(Bitmap.CompressFormat.PNG, 100, os)
                            os.flush()
                        }
                    }
                }catch (e: IOException){
                    e.printStackTrace()
                }
            }
        }
    }

    private fun saveBookImage(bookname: String) {
        val destFile = File(
                "${ReaderApplication.dirPath}/$bookname".apply { File(this).mkdirs() },
                "$bookname.png"
        )
        File(ReaderApplication.dirPath + "/tmp")
                .takeIf { it.exists() }
                ?.listFiles { _, name -> name.startsWith(bookname) }
                ?.firstOrNull()
                ?.copyTo(destFile,true)
    }
}