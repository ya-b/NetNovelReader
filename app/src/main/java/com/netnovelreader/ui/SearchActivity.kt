package com.netnovelreader.ui

import android.app.AlertDialog
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.widget.CursorAdapter
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.netnovelreader.R
import com.netnovelreader.bean.SearchBean
import com.netnovelreader.common.PreferenceManager
import com.netnovelreader.common.RecyclerAdapter
import com.netnovelreader.common.init
import com.netnovelreader.common.toast
import com.netnovelreader.data.network.CatalogCache
import com.netnovelreader.databinding.ActivitySearchBinding
import com.netnovelreader.interfaces.IClickEvent
import com.netnovelreader.interfaces.ISearchContract
import com.netnovelreader.service.DownloadService
import com.netnovelreader.viewmodel.SearchViewModel
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch

class SearchActivity : AppCompatActivity(), ISearchContract.ISearchView {
    var searchViewModel: SearchViewModel? = null
    private var job: Job? = null
    private var suggestCursor: Cursor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        PreferenceManager.getThemeId(this).also { setTheme(it) }
        super.onCreate(savedInstanceState)
        initViewModel()
        initView()

        val isChangeSource = !intent.getStringExtra("bookname").isNullOrEmpty()
        searchViewModel?.isChangeSource?.set(isChangeSource)
        if (isChangeSource) {
            changeSource()
        } else {
            launch { searchViewModel?.refreshHotWords() }
        }
    }

    override fun initViewModel() {
        val factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        searchViewModel = ViewModelProviders.of(this, factory).get(SearchViewModel::class.java)
        val binding =
                DataBindingUtil.setContentView<ActivitySearchBinding>(this, R.layout.activity_search)
        binding.clickEvent = ActivityClickEvent()
        binding.viewModel = searchViewModel
    }

    override fun initView() {
        searchRecycler.init(
                RecyclerAdapter(
                        searchViewModel?.resultList,
                        R.layout.item_search,
                        SearchItemClickEvent()
                )
        )

        searchViewBar.setOnQueryTextListener(QueryListener())
        searchViewBar.onActionViewExpanded()
        searchViewBar.suggestionsAdapter = SearchViewAdapter(this, null)
        searchViewBar.setOnSuggestionListener(SuggestionListener())
    }

    @Suppress("UNCHECKED_CAST")
    override fun onDestroy() {
        super.onDestroy()
        (searchRecycler.adapter as RecyclerAdapter<Any>).removeDataChangeListener()
        CatalogCache.clearCache()
        job?.cancel()
    }

    private fun changeSource() {
        val bookname = intent.getStringExtra("bookname")
        searchViewText.text = bookname
        launch {
            searchViewModel?.searchBook(bookname)
        }
    }

    inner class QueryListener : android.support.v7.widget.SearchView.OnQueryTextListener {
        var deffered: Deferred<Cursor?>? = null

        override fun onQueryTextSubmit(query: String): Boolean {
            launch {
                job?.cancel()
                job = launch { searchViewModel?.searchBook(query) }
            }
            searchViewBar.clearFocus()                    //提交搜索commit后收起键盘
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            launch(UI) {
                deffered?.cancel()
                deffered = async { searchViewModel?.onQueryTextChange(newText) }
                suggestCursor = deffered?.await()
                searchViewBar.suggestionsAdapter.changeCursor(suggestCursor)
            }
            return true
        }
    }

    class SearchViewAdapter(context: Context, cursor: Cursor?) : CursorAdapter(context, cursor,
            true) {
        override fun newView(context: Context, cursor: Cursor?, parent: ViewGroup?): View {
            return LayoutInflater.from(context).inflate(R.layout.item_search_suggest, parent, false)
        }

        override fun bindView(view: View, context: Context?, cursor: Cursor?) {
            (view as TextView).text = cursor?.getString(0)
        }
    }

    inner class SuggestionListener : SearchView.OnSuggestionListener {
        override fun onSuggestionClick(position: Int): Boolean {
            if (suggestCursor?.moveToPosition(position) == true) {
                searchViewBar.setQuery(suggestCursor?.getString(0), true)
                job = launch { searchViewModel?.searchBook(suggestCursor?.getString(0)) }
            }
            return true
        }

        override fun onSuggestionSelect(position: Int): Boolean {
            return true
        }
    }

    //backbutton点击事件
    inner class ActivityClickEvent : IClickEvent {
        fun onBackClick() {
            finish()
        }

        //将搜索热词填充到searchView上但是不触发网络请求
        fun submitHotWord(word: String) {
            searchViewBar.setQuery(word, false)
        }
    }


    //搜索列表item点击事件
    inner class SearchItemClickEvent : IClickEvent {

        fun onClickDetail(itemText: String) {
            launch(UI) {
                val novelIntroduce = async { searchViewModel?.detailClick(itemText) }.await()
                if (novelIntroduce == null) {
                    toast("没有搜索到相关小说的介绍")
                } else {
                    val intent = Intent(this@SearchActivity, NovelDetailActivity::class.java)
                    intent.putExtra("data", novelIntroduce)
                    this@SearchActivity.startActivity(intent)
                }
            }
        }

        //搜索列表item下载事件
        fun onClickDownload(itemDetail: SearchBean) {
            val listener = DialogInterface.OnClickListener { _, which ->
                launch(UI) {
                    val str = async {
                        searchViewModel!!.downloadCatalog(
                                itemDetail.bookname.get() ?: "",
                                itemDetail.url.get() ?: "",
                                intent.getStringExtra("chapterName"), which
                        )
                    }.await()
                    if (str == "0") {
                        toast(getString(R.string.downloadFailed))
                        return@launch
                    }
                    toast(getString(R.string.catalog_finish))
                    if (str != "1") {
                        val intent = Intent(this@SearchActivity, DownloadService::class.java)
                        intent.putExtra("tableName", str)
                        intent.putExtra("catalogurl", itemDetail.url.get() ?: "")
                        this@SearchActivity.startService(intent)
                    }
                    this@SearchActivity.finish()
                }
            }
            AlertDialog.Builder(this@SearchActivity).setTitle(getString(R.string.downloadAllBook))
                    .setPositiveButton(R.string.yes, listener)
                    .setNegativeButton(getString(R.string.no), listener)
                    .setNeutralButton(getString(R.string.cancel), null)
                    .create().show()
        }
    }
}