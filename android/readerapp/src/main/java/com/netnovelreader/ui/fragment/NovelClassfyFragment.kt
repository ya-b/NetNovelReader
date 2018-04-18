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
import com.netnovelreader.common.*
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_novel_classfy, container, false)
        binding.viewModel = viewModel
        binding.classfyView.init(
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
        val tab = (activity as ShelfActivity).shelfTab
        tab.post { binding.classfyView.setPadding(0, tab.height.also { tabHeight = it }, 0, 0) }
        return binding.root
    }

    fun initLiveData() {
        viewModel?.openCatalogDetailCommand?.observe(this, Observer {
            val intent = Intent(activity, CategoryDetailActivity::class.java)
            intent.putExtra("major", it)
            intent.putExtra("themeid", context!!.sharedPreferences()
                .get(context!!.getString(R.string.themeKey), "black").parseTheme())
            startActivityForResult(intent, 123)
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == 10000) {
            (activity as ShelfActivity).shelfViewPager.setCurrentItem(0, false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.classfyView.adapter = null
    }
}
