package com.netnovelreader.ui.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netnovelreader.R
import com.netnovelreader.common.RecyclerAdapter
import com.netnovelreader.common.init
import com.netnovelreader.common.obtainViewModel
import com.netnovelreader.viewmodel.CategoryDetailViewModel
import kotlinx.android.synthetic.main.fragment_novel_list.*

/**
 * 文件： NovelListFragment
 * 描述： 这个Fragment显示的是  type 类型 major 类型的小说列表  ，如 “最热门” 的  “轻小说”
 * 作者： YangJunQuan   2018-2-11.
 */
class NovelListFragment : Fragment() {
    private var viewModel: CategoryDetailViewModel? = null
    private var type: String? = null
    private var major: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = activity?.obtainViewModel(CategoryDetailViewModel::class.java)
        type = arguments!!.getString("type")
        major = arguments!!.getString("major")
        return inflater.inflate(R.layout.fragment_novel_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        RecyclerAdapter(
            viewModel?.getBookList(type!!, major!!),
            R.layout.item_catalog_detial,
            viewModel,
            true
        )
            .let { novelList.init(it, null) }
        viewModel?.initBooklist(type!!, major!!)
    }

}
