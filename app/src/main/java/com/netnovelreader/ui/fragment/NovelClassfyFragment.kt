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
import com.netnovelreader.databinding.FragmentNovelClassfyBinding
import com.netnovelreader.ui.activity.NovelCatalogDetailActivity
import com.netnovelreader.viewmodel.ShelfViewModel

class NovelClassfyFragment : Fragment() {
    val viewModel by lazy { activity?.obtainViewModel(ShelfViewModel::class.java) }
    lateinit var binding: FragmentNovelClassfyBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_novel_classfy, container, false)
        binding.maleRecyclerView.init(
            RecyclerAdapter(viewModel?.resultList, R.layout.item_novel_classfy, viewModel, false),
            GridDivider(activity!!.baseContext, 1, Color.BLACK),
            object : GridLayoutManager(activity, 3, RecyclerView.VERTICAL, false) {
                override fun supportsPredictiveItemAnimations(): Boolean {
                    return false
                }
            }
        )
        viewModel?.getNovelCatalogData()
        initLiveData()
        return binding.root
    }

    fun initLiveData() {
        viewModel?.paddingCommand?.observe(this, Observer {
            if (it != null && it != 0 && binding.malelabel.paddingTop != it) {
                binding.malelabel.setPadding(0, it, 0, 0)
            }
        })
        viewModel?.openCatalogDetailCommand?.observe(this, Observer {
            val intent = Intent(activity, NovelCatalogDetailActivity::class.java)
            intent.putExtra("major", it)
            intent.putExtra("themeid", intent.getIntExtra("themeid", R.style.AppThemeBlack))
            startActivity(intent)
        })
    }
}
