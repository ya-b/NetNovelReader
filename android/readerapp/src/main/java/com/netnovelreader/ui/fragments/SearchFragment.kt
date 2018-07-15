package com.netnovelreader.ui.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import com.netnovelreader.R
import com.netnovelreader.ViewModelFactory
import com.netnovelreader.databinding.FragmentSearchBinding
import com.netnovelreader.repo.http.resp.SearchBookResp
import com.netnovelreader.service.DownloadService
import com.netnovelreader.ui.adapters.SearchResultAdapter
import com.netnovelreader.utils.toast
import com.netnovelreader.vm.SearchViewModel


class SearchFragment : Fragment(), SearchView.OnQueryTextListener {

    private var viewModel: SearchViewModel? = null
    private lateinit var binding: FragmentSearchBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_search, container, false
        )
        binding.searchView.onActionViewExpanded()
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = activity?.application?.let {
            ViewModelProviders.of(this, ViewModelFactory.getInstance(it))
        }?.get(SearchViewModel::class.java)
        binding.viewModel = viewModel

        val adapter = SearchResultAdapter(viewModel, viewModel?.searchResultList)
        binding.searchFragRecycler.adapter = adapter
        binding.searchFragRecycler.layoutManager = object : LinearLayoutManager(context) {
            override fun supportsPredictiveItemAnimations() = false
        }
        binding.searchView.setOnQueryTextListener(this)
        binding.searchView.onActionViewExpanded()

        viewModel?.exitCommand?.observe(this, Observer {
            binding.searchView.clearFocus()
            NavHostFragment.findNavController(this).popBackStack()
        })
        viewModel?.toaskCommand?.observe(this, Observer { it?.let { context?.toast(it) } })
        viewModel?.downloadCommand?.observe(this, Observer {
            it ?: return@Observer
            Intent(context, DownloadService::class.java)
                .apply { putExtra(DownloadService.INFO, it) }
                .also { context?.startService(it) }
        })
        viewModel?.confirmCommand?.observe(this, Observer {
            it ?: return@Observer
            dialog(it)
        })
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        viewModel?.searchBook(query)
        binding.searchView.clearFocus()                    //提交搜索commit后收起键盘
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return true
    }

    private fun dialog(search: SearchBookResp) {
        context ?: return
        AlertDialog.Builder(context!!)
            .setTitle(String.format(getString(R.string.download_book), search.bookname))
            .setPositiveButton(R.string.add_book) { _, _ -> viewModel?.download(search, true) }
            .setNegativeButton(R.string.add_and_download) { _, _ -> viewModel?.download(search, false) }
            .setNeutralButton(R.string.cancel, null)
            .create()
            .show()
    }
}
