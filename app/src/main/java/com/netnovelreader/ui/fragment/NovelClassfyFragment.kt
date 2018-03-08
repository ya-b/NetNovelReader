package com.netnovelreader.ui.fragment

import android.arch.lifecycle.Observer
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netnovelreader.R
import com.netnovelreader.common.GridDivider
import com.netnovelreader.common.RecyclerAdapter
import com.netnovelreader.common.init
import com.netnovelreader.common.obtainViewModel
import com.netnovelreader.data.PreferenceManager
import com.netnovelreader.databinding.FragmentNovelClassfyBinding
import com.netnovelreader.ui.activity.CategoryDetailActivity
import com.netnovelreader.ui.activity.ShelfActivity
import com.netnovelreader.viewmodel.ShelfViewModel
import kotlinx.android.synthetic.main.activity_shelf.*

class NovelClassfyFragment : Fragment() {
    private var viewModel: ShelfViewModel? = null
    private lateinit var binding: FragmentNovelClassfyBinding
    private var tabHeight = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = activity?.obtainViewModel(ShelfViewModel::class.java)
        binding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_novel_classfy, container, false)
        binding.maleRecyclerView.init(
            RecyclerAdapter(viewModel?.resultList, R.layout.item_novel_classfy, viewModel, false),
            GridDivider(activity!!.baseContext, 1, Color.GRAY),
            object : GridLayoutManager(activity, 3, RecyclerView.VERTICAL, false) {
                override fun supportsPredictiveItemAnimations(): Boolean {
                    return false
                }
            }
        )
        viewModel?.getNovelCatalogData()
        initLiveData()
        (activity as ShelfActivity).shelfTab.run {
            post {
                tabHeight = height
                binding.malelabel.setPadding(0, tabHeight, 0, 0)
            }
        }
        return binding.root
    }

    fun initLiveData() {
        viewModel?.openCatalogDetailCommand?.observe(this, Observer {
            val intent = Intent(activity, CategoryDetailActivity::class.java)
            intent.putExtra("major", it)
            intent.putExtra("themeid", PreferenceManager.getThemeId(activity!!.baseContext))
            startActivity(intent)
        })
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onDestroy() {
        super.onDestroy()
        binding.maleRecyclerView.adapter = null
    }
}
