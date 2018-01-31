package com.netnovelreader.search

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import com.netnovelreader.BR
import com.netnovelreader.R
import com.netnovelreader.base.IClickEvent
import com.netnovelreader.common.ApplyPreference
import com.netnovelreader.common.ArrayListChangeListener
import com.netnovelreader.common.BindingAdapter
import com.netnovelreader.common.NovelItemDecoration
import com.netnovelreader.databinding.ActivitySearchBinding
import com.netnovelreader.download.CatalogCache
import com.netnovelreader.download.DownloadService
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.item_search.view.*

class SearchActivity : AppCompatActivity(), ISearchContract.ISearchView {
    var searchViewModel: SearchViewModel? = null
    private lateinit var arrayListChangeListener: ArrayListChangeListener<SearchBean>

    override fun onCreate(savedInstanceState: Bundle?) {
        ApplyPreference.setTheme(this)
        super.onCreate(savedInstanceState)
        setViewModel(SearchViewModel())
        init()
        changeSource()
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
        searchViewBar.isIconified = false
        searchViewBar.onActionViewExpanded()
    }

    override fun onDestroy() {
        super.onDestroy()
        searchViewModel?.resultList?.removeOnListChangedCallback(arrayListChangeListener)
        searchViewModel = null
    }

    override fun onBackPressed() {
        CatalogCache.clearCache()
        super.onBackPressed()
    }

    fun changeSource() {
        val bookname = intent.getStringExtra("bookname")
        if (bookname.isNullOrEmpty()) return
        searchViewBar.visibility = View.INVISIBLE
        searchViewText.visibility = View.VISIBLE
        searchViewText.text = bookname
        searchViewModel?.searchBook(bookname)
    }

    inner class QueryListener : android.support.v7.widget.SearchView.OnQueryTextListener {
        private var tmp = ""
        private var tmpTime = System.currentTimeMillis()

        override fun onQueryTextSubmit(query: String): Boolean {
            if (tmp == query && System.currentTimeMillis() - tmpTime < 1000) return true  //点击间隔小于1秒，并且搜索书名相同不再搜索
            if (query.length > 0) {
                searchViewModel?.searchBook(query)
                tmp = query
                tmpTime = System.currentTimeMillis()
            }
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            return true
        }
    }

    //backbutton点击事件
    inner class BackClickEvent : IClickEvent {
        fun onClick(v: View) {
            finish()
        }
    }

    //搜索列表item点击事件
    inner class SearchItemClickEvent : IClickEvent {
        fun onClick(v: View) {
            val chapterName = intent.getStringExtra("chapterName")
            val catalogUrl = v.resultUrl.text.toString()
            val tableName =
                    searchViewModel!!.addBookToShelf(v.resultName.text.toString(), catalogUrl)
            if (!intent.getStringExtra("bookname").isNullOrEmpty() && !chapterName.isNullOrEmpty()) {
                searchViewModel?.delChapterAfterSrc(tableName, chapterName)
            }
            Toast.makeText(this@SearchActivity, R.string.start_download, Toast.LENGTH_SHORT)
                    .show()
            val intent = Intent(v.context, DownloadService::class.java)
            intent.putExtra("tableName", tableName)
            intent.putExtra("catalogurl", v.resultUrl.text.toString())
            startService(intent)
            searchViewModel?.saveBookImage(tableName, v.resultName.text.toString())
            this@SearchActivity.finish()
        }
    }
}