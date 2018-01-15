package com.netnovelreader.search

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import com.netnovelreader.NetNovelReaderApplication
import com.netnovelreader.R
import com.netnovelreader.base.BindingAdapter
import com.netnovelreader.base.IClickEvent
import com.netnovelreader.databinding.ActivitySearchBinding
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.item_search_recycler_view.view.*

class SearchActivity : AppCompatActivity(), ISearchContract.ISearchView {
    var mViewModel: SearchViewModel? = null
    var searchCode = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setViewModel(SearchViewModel())
        initView()
    }

    override fun setViewModel(vm: SearchViewModel) {
        mViewModel = vm
        DataBindingUtil.setContentView<ActivitySearchBinding>(this, R.layout.activity_search)
    }

    override fun initView() {
        search_bar.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null && query.length > 0 && mViewModel != null) {
                    updateSearchResult(query, searchCode++)
//                    updateSearchResult("极道天魔", searchCode++)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
        searchRecycler.layoutManager = LinearLayoutManager(this)
        searchRecycler.adapter = BindingAdapter(mViewModel?.getModel()?.resultList,
                R.layout.item_search_recycler_view, SearchClickEvent())
        searchRecycler.setItemAnimator(DefaultItemAnimator())
    }

    override fun updateSearchResult(bookname: String?, shCode: Int) {
        bookname ?: return
        mViewModel ?: return
        mViewModel!!.getModel()?.resultList?.clear()
        searchRecycler.adapter.notifyDataSetChanged()
        mViewModel!!.getSearchSite()?.forEach {
            Observable.create<SearchBean.SearchResultBean> { e -> e.onNext(mViewModel!!.updateResultList(bookname, it, shCode)) }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { result ->
                        if (result.reqCode + 1 == searchCode && result.url.length > 0) {
                            mViewModel!!.getModel()?.resultList?.add(result)
                            searchRecycler.adapter.notifyDataSetChanged()
                        }
                    }
        }
    }


    inner class SearchClickEvent : IClickEvent {
        fun downloadBook(v: View) {
            Observable.create<Boolean>({ e -> e.onNext(mViewModel!!.addBook(v.resultName.text.toString(), v.resultUrl.text.toString())) })
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe { r ->
                        if (r) {
                            Toast.makeText(NetNovelReaderApplication.context, R.string.add_success, Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(NetNovelReaderApplication.context, R.string.add_fail, Toast.LENGTH_SHORT).show()
                        }
                    }
        }
    }
}
