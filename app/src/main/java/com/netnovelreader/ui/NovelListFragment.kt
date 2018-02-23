package com.netnovelreader.ui

import android.content.Intent
import android.databinding.ObservableArrayList
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netnovelreader.R
import com.netnovelreader.bean.NovelList
import com.netnovelreader.common.RecyclerAdapter
import com.netnovelreader.common.enqueueCall
import com.netnovelreader.common.init
import com.netnovelreader.data.network.ApiManager
import com.netnovelreader.interfaces.IClickEvent
import kotlinx.android.synthetic.main.fragment_novel_list.*

/**
 * 文件： NovelListFragment
 * 描述： 这个Fragment显示的是  type 类型 major 类型的小说列表  ，如 “最热门” 的  “轻小说”
 * 作者： YangJunQuan   2018-2-11.
 */
class NovelListFragment : Fragment() {

    private var bookList: ObservableArrayList<NovelList.BooksBean> = ObservableArrayList()
    private var type: String? = null
    private var major: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
        novelList.init(
            RecyclerAdapter(bookList, R.layout.item_catalog_detial, NovelListItemClickEvent()), null
        )
    }

    private fun initData() {
        ApiManager.mAPI.seachBookListByTypeAndMajor(type = type, major = major).enqueueCall {
            it?.let {
                bookList.clear()
                bookList.addAll(it.books!!)
            }
        }
    }

    inner class NovelListItemClickEvent : IClickEvent {

        fun onClickDetail(id: String) {
            ApiManager.mAPI.getNovelIntroduce(id).enqueueCall {
                val intent = Intent(context, NovelDetailActivity::class.java)
                intent.putExtra("data", it)
                context!!.startActivity(intent)
            }
        }
    }

}
