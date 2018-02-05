package com.netnovelreader.search

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
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
import com.netnovelreader.common.*
import com.netnovelreader.common.base.IClickEvent
import com.netnovelreader.common.data.SQLHelper
import com.netnovelreader.common.download.CatalogCache
import com.netnovelreader.common.download.DownloadCatalog
import com.netnovelreader.common.download.DownloadChapter
import com.netnovelreader.databinding.ActivitySearchBinding
import com.netnovelreader.service.DownloadService
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.item_search.view.*
import java.io.IOException

class SearchActivity : AppCompatActivity(), ISearchContract.ISearchView {
    var searchViewModel: SearchViewModel? = null
    private lateinit var arrayListChangeListener: ArrayListChangeListener<SearchBean>

    override fun onCreate(savedInstanceState: Bundle?) {
        PreferenceManager.setTheme(this)
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
        if (searchloadingbar.isShown) return
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
            val catalogUrl = v.resultUrl.text.toString()
            val tableName = searchViewModel!!.addBookToShelf(v.resultName.text.toString(), catalogUrl)
            val isChangeSource = !intent.getStringExtra("bookname").isNullOrEmpty()
            val listener = DialogInterface.OnClickListener { dialog, which ->
                searchloadingbar.show()
                Observable.create<Boolean> {
                    try {
                        searchViewModel?.saveBookImage(tableName, v.resultName.text.toString())
                        DownloadCatalog(tableName, catalogUrl).download()
                        it.onNext(true)
                    } catch (e: IOException) {
                        it.onNext(false)
                    }
                }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe {
                    searchloadingbar.hide()
                    if (it) {
                        Toast.makeText(this@SearchActivity, R.string.catalog_finish, Toast.LENGTH_SHORT).show()
                        this@SearchActivity.finish()
                        if (which == Dialog.BUTTON_POSITIVE) downloadBook(v.context, tableName, catalogUrl, isChangeSource)
                        if (which == Dialog.BUTTON_NEGATIVE) downNowChapter(tableName, isChangeSource)
                    } else {
                        Toast.makeText(this@SearchActivity, R.string.downloadFailed, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            AlertDialog.Builder(this@SearchActivity).setTitle(getString(R.string.downloadAllBook))
                    .setPositiveButton(R.string.yes, listener)
                    .setNegativeButton(getString(R.string.no), listener)
                    .create().show()
        }

        //下载全书，若该书已存在，则下载所有未读章节
        private fun downloadBook(context: Context, tableName: String, catalogUrl: String, isChangeSource: Boolean) {
            val chapterName = intent.getStringExtra("chapterName")
            if (isChangeSource && !chapterName.isNullOrEmpty()) {
                searchViewModel?.delChapterAfterSrc(tableName, chapterName)
            }
            val intent = Intent(context, DownloadService::class.java)
            intent.putExtra("tableName", tableName)
            intent.putExtra("catalogurl", catalogUrl)
            startService(intent)
        }

        //换源下载，只下载当前章节
        private fun downNowChapter(tableName: String, isChangeSource: Boolean) {
            val chapterName = intent.getStringExtra("chapterName")
            if (isChangeSource && !chapterName.isNullOrEmpty()) {
                searchViewModel?.delChapterAfterSrc(tableName, chapterName)
                DownloadChapter(tableName, "${getSavePath()}/$tableName",
                        chapterName, SQLHelper.getChapterUrl(tableName, chapterName))
            }
        }
    }
}