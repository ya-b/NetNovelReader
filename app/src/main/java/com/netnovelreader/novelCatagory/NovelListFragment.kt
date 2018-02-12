package com.netnovelreader.novelCatagory

import android.content.Intent
import android.databinding.ObservableArrayList
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.netnovelreader.R
import com.netnovelreader.api.ApiManager
import com.netnovelreader.api.bean.NovelList
import com.netnovelreader.common.ArrayListChangeListener
import com.netnovelreader.common.BindingAdapter
import com.netnovelreader.common.base.IClickEvent
import com.netnovelreader.common.enqueueCall
import com.netnovelreader.noveldetail.NovelDetailActivity
import kotlinx.android.synthetic.main.fragment_novel_list.*

/**
 * 文件： NovelListFragment
 * 描述： 这个Fragment显示的是  type 类型 major 类型的小说列表  ，如 “最热门” 的  “轻小说”
 * 作者： YangJunQuan   2018-2-11.
 */
class NovelListFragment : Fragment() {


    private lateinit var listListener: ArrayListChangeListener<NovelList.BooksBean>
    private var bookList: ObservableArrayList<NovelList.BooksBean> = ObservableArrayList()
    private var type: String? = null
    private var major: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        type = arguments!!.getString("type")
        major = arguments!!.getString("major")
        return inflater.inflate(R.layout.fragment_novel_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initData()
    }

    private fun initView() {

        val adapter = BindingAdapter(bookList, R.layout.item_catalog_detial, NovelListItemClickEvent())
        listListener = ArrayListChangeListener(adapter)
        bookList.addOnListChangedCallback(listListener)

        novelList.layoutManager = LinearLayoutManager(context)
        novelList.adapter = adapter
        novelList.itemAnimator = DefaultItemAnimator()

    }

    private fun initData() {
        ApiManager.mAPI!!.seachBookListByTypeAndMajor(type = type, major = major).enqueueCall {
            it?.let {
                bookList.clear()
                bookList.addAll(it.books!!)
            }
        }
    }

    inner class NovelListItemClickEvent : IClickEvent {

        fun onClickDetail(v: View) {
            val id = v.findViewById<TextView>(R.id.bookId).text.toString()
            ApiManager.mAPI?.getNovelIntroduce(id)?.enqueueCall {
                val intent = Intent(context, NovelDetailActivity::class.java)
                intent.putExtra("data", it)
                context!!.startActivity(intent)
            }
        }
    }


}
