package com.netnovelreader.search

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import com.netnovelreader.BR
import com.netnovelreader.R
import com.netnovelreader.api.ApiManager
import com.netnovelreader.api.bean.KeywordsBean
import com.netnovelreader.api.bean.SearchHotWord
import com.netnovelreader.api.bean.SearchHotWordsBean
import com.netnovelreader.common.*
import com.netnovelreader.common.base.IClickEvent
import com.netnovelreader.common.data.SQLHelper
import com.netnovelreader.common.download.CatalogCache
import com.netnovelreader.common.download.DownloadCatalog
import com.netnovelreader.common.download.DownloadChapter
import com.netnovelreader.databinding.ActivitySearchBinding
import com.netnovelreader.noveldetail.NovelDetailActivity
import com.netnovelreader.service.DownloadService
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.item_search.view.*
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import java.util.*

class SearchActivity : AppCompatActivity(), ISearchContract.ISearchView {
    var searchViewModel: SearchViewModel? = null
    private lateinit var arrayListChangeListener: ArrayListChangeListener<SearchBean>
    private lateinit var suggestArrayListChangeListener: ArrayListChangeListener<KeywordsBean>
    private var job: Job? = null
    private var mSearchHotWord: SearchHotWord? = null              //搜索热词数组
    private val colorArray = arrayOf(                              //搜索热词标签的背景颜色列表
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
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        PreferenceManager.setTheme(this)
        super.onCreate(savedInstanceState)
        setViewModel(SearchViewModel())
        requestHotWords()
        init()
        changeSource()
    }

    /**
     * 请求搜索热词数据
     */
    private fun requestHotWords() {
        ApiManager.mAPI!!.hotWords().enqueueCall {
            it?.also { mSearchHotWord = it }?.searchHotWords?.apply { refreshHotWords(this) }
        }
    }

    override fun setViewModel(vm: SearchViewModel) {
        searchViewModel = vm
        val binding =
            DataBindingUtil.setContentView<ActivitySearchBinding>(this, R.layout.activity_search)
        binding.setVariable(BR.clickEvent, BackClickEvent())
    }

    override fun init() {
        searchRecycler.layoutManager = LinearLayoutManager(this)
        val mAdapter = BindingAdapter(
            searchViewModel?.resultList,
            R.layout.item_search,
            SearchItemClickEvent()
        )
        searchRecycler.adapter = mAdapter
        searchRecycler.itemAnimator = DefaultItemAnimator()
        searchRecycler.addItemDecoration(NovelItemDecoration(this))
        arrayListChangeListener = ArrayListChangeListener(mAdapter)
        searchViewModel?.resultList?.addOnListChangedCallback(arrayListChangeListener)


        searchViewBar.setOnQueryTextListener(QueryListener())
        searchViewBar.onActionViewExpanded()


        searchSuggestRecycler.layoutManager = LinearLayoutManager(this)
        val adapter = BindingAdapter(
            searchViewModel?.suggestList,
            R.layout.item_search_suggest,
            SuggestSearchItemClickEvent()
        )

        searchSuggestRecycler.adapter = adapter
        searchSuggestRecycler.itemAnimator = DefaultItemAnimator()
        searchSuggestRecycler.addItemDecoration(NovelItemDecoration(this))
        suggestArrayListChangeListener = ArrayListChangeListener(adapter)
        searchViewModel?.suggestList?.addOnListChangedCallback(
            suggestArrayListChangeListener
        )


    }

    override fun onDestroy() {
        super.onDestroy()
        searchViewModel?.resultList?.removeOnListChangedCallback(arrayListChangeListener)
        searchViewModel?.suggestList?.removeOnListChangedCallback(suggestArrayListChangeListener)
        searchViewModel = null
        CatalogCache.clearCache()
        job?.cancel()
    }

    override fun onBackPressed() {
        if (searchloadingbar.isShown) return
        super.onBackPressed()
    }

    private fun changeSource() {
        val bookname = intent.getStringExtra("bookname")
        if (bookname.isNullOrEmpty()) return
        searchViewBar.visibility = View.INVISIBLE
        searchViewText.visibility = View.VISIBLE
        searchViewText.text = bookname
        launch {
            searchViewModel?.searchBook(bookname)
        }
    }

    inner class QueryListener : android.support.v7.widget.SearchView.OnQueryTextListener {
        private var tmp = ""
        private var tmpTime = System.currentTimeMillis()

        override fun onQueryTextSubmit(query: String): Boolean {
            searchloadingbar.hide()
            if (tmp == query && System.currentTimeMillis() - tmpTime < 1000) return true  //点击间隔小于1秒，并且搜索书名相同不再搜索
            if (query.isNotEmpty()) {
                launch {
                    job?.cancel()
                    job = searchViewModel?.searchBook(query)
                }
                tmp = query
                tmpTime = System.currentTimeMillis()
                searchViewBar.clearFocus()                    //提交搜索commit后收起键盘
                searchSuggestRecycler.visibility = View.GONE
            }
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            if (newText!!.isEmpty() && searchViewModel?.resultList!!.isEmpty()) {
                showHotWords()   //搜索框里文字为空并且仍未搜索过  -->显示热门搜索标签
                searchSuggestRecycler.visibility = View.GONE
            } else {
                hideHotWords()   //搜索框里文字不为空时  -->隐藏热门搜索标签
                searchSuggestRecycler.visibility = View.VISIBLE
                searchViewModel?.searchBookSuggest(newText)
            }
            return true
        }
    }

    /**
     * 显示搜索热词的相关UI界面
     */
    private fun showHotWords() {
        tvSearchLabel.visibility = View.VISIBLE
        tvRefreshHotWord.visibility = View.VISIBLE
        linearLayout.visibility = View.VISIBLE
    }

    /**
     * 隐藏搜索热词的相关UI界面
     */
    private fun hideHotWords() {
        tvSearchLabel.visibility = View.GONE
        tvRefreshHotWord.visibility = View.GONE
        linearLayout.visibility = View.GONE
    }

    fun refreshHotWords(list: List<SearchHotWordsBean>?) {
        val li = list?.shuffled() ?: run { hideHotWords(); return }
        for (i in 0 until linearLayout.childCount)
            with(linearLayout.getChildAt(i) as TextView) {
                //设置搜索热词文本，该10个热词是从100个关键个搜索热词中随机抽取的
                text = li[i].word
                (background as GradientDrawable).setColor(ContextCompat.getColor(context, colorArray[Random().nextInt(17)]))
            }
    }

    //backbutton点击事件
    inner class BackClickEvent : IClickEvent {
        fun onClick(v: View) {
            finish()
        }

        //换一批热门搜索词
        fun refreshHotWords(v: View) {
            refreshHotWords(mSearchHotWord?.searchHotWords)
        }

        //将搜索热词填充到searchView上但是不触发网络请求
        fun submitHotWord(v: View) {
            v as TextView
            searchViewBar.setQuery(v.text, false)
        }
    }

    //建议搜索列表item点击事件
    inner class SuggestSearchItemClickEvent : IClickEvent {
        fun onClick(v: View) {
            val textView = v.findViewById<TextView>(R.id.tvSearchSuggest)
            searchViewBar.setQuery(textView.text, true)
            searchSuggestRecycler.visibility = View.GONE
        }
    }


    //搜索列表item点击事件
    inner class SearchItemClickEvent : IClickEvent {

        fun onClickDetail(v: View) {

            val itemText = v.findViewById<TextView>(R.id.resultName).text.toString()
            ApiManager.mAPI!!.searchBook(itemText).enqueueCall {
                val id = it?.books?.firstOrNull { it.title == itemText }?._id
                ApiManager.mAPI?.getNovelIntroduce(id ?: "")?.enqueueCall {
                    when (it?._id) {
                        null ->   launch(UI) { Snackbar.make(searchRoot, "没有搜索到相关小说的介绍", Snackbar.LENGTH_SHORT).show() }
                        else -> {
                            val intent = Intent(this@SearchActivity, NovelDetailActivity::class.java)
                            intent.putExtra("data", it)
                            this@SearchActivity.startActivity(intent)
                        }
                    }
                }
            }
        }

        //搜索列表item下载事件
        fun onClickDownload(v: View) {

            val container = v.parent as View      //获取下载按钮的父元素 即ItemView
            if (searchloadingbar.isShown) return     //如果正在下载中，return

            val listener = DialogInterface.OnClickListener { _, which ->
                searchloadingbar.show()
                launch(UI) {                                                                         //在UI线程中执行一些操作
                    val catalogUrl = container.resultUrl.text.toString()
                    val bookname = container.resultName.text.toString()
                    val tableName = searchViewModel!!.addBookToShelf(bookname, catalogUrl)           //addBookToShelf方法需要被suspend修饰？
                    val isChangeSource = !intent.getStringExtra("bookname").isNullOrEmpty()
                    when (which) {
                        Dialog.BUTTON_POSITIVE -> download(bookname, catalogUrl) {
                            downloadBook(v.context, tableName, catalogUrl, isChangeSource)
                        }
                        Dialog.BUTTON_NEGATIVE -> download(bookname, catalogUrl) {
                            launch { downNowChapter(tableName, isChangeSource) }                     //新启动一个线程执行下载任务
                        }
                    }
                }
            }
            AlertDialog.Builder(this@SearchActivity).setTitle(getString(R.string.downloadAllBook))
                .setPositiveButton(R.string.yes, listener)
                .setNegativeButton(getString(R.string.no), listener)
                .setNeutralButton(getString(R.string.cancel), null)
                .create().show()
        }

        /**
         * 下载，调用[downloadBook]或[downNowChapter]
         */
        private fun download(bookname: String, catalogUrl: String, method: () -> Unit) {         //函数A作为函数B的参数传递进来，然后在函数B里执行函数A
            async {
                searchViewModel!!.addBookToShelf(bookname, catalogUrl).apply {
                    searchViewModel?.saveBookImage(this, bookname)
                    DownloadCatalog(this, catalogUrl).download()
                }
            }.invokeOnCompletion {
                launch(UI) {
                    it?.apply { toast(getString(R.string.downloadFailed)) } ?: kotlin.run { toast(getString(R.string.catalog_finish));this@SearchActivity.finish();method() }
                    searchloadingbar.hide()
                }
            }
        }

        //下载全书，若该书已存在，则下载所有未读章节
        private fun downloadBook(
            context: Context,
            tableName: String,
            catalogUrl: String,
            isChangeSource: Boolean
        ) {
            val chapterName = intent.getStringExtra("chapterName")
            if (isChangeSource && !chapterName.isNullOrEmpty()) {
                launch { searchViewModel?.delChapterAfterSrc(tableName, chapterName) }
            }
            val intent = Intent(context, DownloadService::class.java)
            intent.putExtra("tableName", tableName)
            intent.putExtra("catalogurl", catalogUrl)
            startService(intent)
        }

        //换源下载，只下载当前章节
        private suspend fun downNowChapter(tableName: String, isChangeSource: Boolean) {
            val chapterName = intent.getStringExtra("chapterName")
            if (isChangeSource && !chapterName.isNullOrEmpty()) {
                launch { searchViewModel?.delChapterAfterSrc(tableName, chapterName) }
                DownloadChapter(
                    tableName, "${getSavePath()}/$tableName",
                    chapterName, SQLHelper.getChapterUrl(tableName, chapterName)
                )
            }
        }
    }
}