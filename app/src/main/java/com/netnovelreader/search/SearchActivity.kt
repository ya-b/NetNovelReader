package com.netnovelreader.search

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.util.Log
import com.netnovelreader.R
import com.netnovelreader.base.BindingAdapter
import com.netnovelreader.databinding.ActivityShelfBinding
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

import kotlinx.android.synthetic.main.activity_search.*

class SearchActivity : AppCompatActivity(), ISearchContract.ISearchView {
    var mViewModel: SearchViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setViewModel(SearchViewModel())
        initView()

    }

    override fun setViewModel(vm: SearchViewModel) {
        mViewModel = vm
        DataBindingUtil.setContentView<ActivityShelfBinding>(this, R.layout.activity_search)
    }

    override fun initView() {
        search_bar.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                if(query != null && query.length > 0 && mViewModel != null){
                    updateSearchResult("极道天魔")
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
        searchRecycler.layoutManager = LinearLayoutManager(this)
        searchRecycler.adapter = BindingAdapter(mViewModel?.getModel()?.resultList,
                R.layout.item_search_recycler_view, SearchViewModel.SearchClickEvent())
        searchRecycler.setItemAnimator(DefaultItemAnimator())
    }

    override fun updateSearchResult(bookname: String?) {
        bookname ?: return
        mViewModel ?: return
        Observable.create<Boolean> { e -> e.onNext(mViewModel!!.updateResultList(bookname)) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { searchRecycler.adapter.notifyDataSetChanged() }
    }
}
