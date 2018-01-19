package com.netnovelreader.search

import android.content.Intent
import android.databinding.DataBindingUtil
import android.databinding.ObservableArrayList
import android.databinding.ObservableInt
import android.databinding.ObservableList
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import com.netnovelreader.R
import com.netnovelreader.base.BindingAdapter
import com.netnovelreader.base.IClickEvent
import com.netnovelreader.databinding.ActivitySearchBinding
import com.netnovelreader.service.DownloadService
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.item_search_recycler_view.view.*

class SearchActivity : AppCompatActivity(), ISearchContract.ISearchView {
    var mViewModel: SearchViewModel? = null
    var arrayListChangeListener = ArrayListChangeListener()

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

        //搜索事件监听
        search_bar.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
            var tmp = ""
            var tmpTime = System.currentTimeMillis()
            override fun onQueryTextSubmit(query: String): Boolean {
                if(tmp == query && System.currentTimeMillis() - tmpTime < 1000) return true  //点击间隔小于1秒，并且搜索书名相同不再搜索
                if (query.length > 0 && mViewModel != null) {
//                    updateSearchResult(query, searchCode++)
                    mViewModel?.searchBook("电视剧世界" )
                    tmp = query
                    tmpTime = System.currentTimeMillis()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })

        searchRecycler.layoutManager = LinearLayoutManager(this)
        searchRecycler.adapter = BindingAdapter(mViewModel?.resultList,
                R.layout.item_search_recycler_view, SearchClickEvent())
        searchRecycler.setItemAnimator(DefaultItemAnimator())
        mViewModel?.resultList?.addOnListChangedCallback(arrayListChangeListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewModel?.resultList?.removeOnListChangedCallback(arrayListChangeListener)
    }

    override fun updateSearchResult(bookname: String?, shCode: ObservableInt) {
    }

    inner class SearchClickEvent : IClickEvent {
        fun downloadBook(v: View) {
            if(v.resultName.text.toString().length > 0 &&  v.resultUrl.text.toString().length > 0){
                val path = mViewModel!!.addBookToShelf(v.resultName.text.toString(), v.resultUrl.text.toString())
                Toast.makeText(this@SearchActivity, R.string.start_download, Toast.LENGTH_SHORT).show()
                val intent = Intent(v.context, DownloadService::class.java)
                intent.putExtra("localpath", path)
                intent.putExtra("catalogurl", v.resultUrl.text.toString())
                startService(intent)
            }
        }
    }

    inner class ArrayListChangeListener : ObservableList.OnListChangedCallback<ObservableArrayList<SearchBean>>(){
        override fun onChanged(p0: ObservableArrayList<SearchBean>?) {
            notifyDataSetChanged()
        }

        override fun onItemRangeChanged(p0: ObservableArrayList<SearchBean>?, p1: Int, p2: Int) {
            notifyDataSetChanged()
        }

        override fun onItemRangeInserted(p0: ObservableArrayList<SearchBean>?, p1: Int, p2: Int) {
            notifyDataSetChanged()
        }

        override fun onItemRangeMoved(p0: ObservableArrayList<SearchBean>?, p1: Int, p2: Int, p3: Int) {
            notifyDataSetChanged()
        }

        override fun onItemRangeRemoved(p0: ObservableArrayList<SearchBean>?, p1: Int, p2: Int) {
            notifyDataSetChanged()
        }

        fun notifyDataSetChanged(){
            runOnUiThread { searchRecycler.adapter.notifyDataSetChanged() }
        }
    }
}
