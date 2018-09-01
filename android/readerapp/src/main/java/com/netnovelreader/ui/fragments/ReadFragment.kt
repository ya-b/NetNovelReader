package com.netnovelreader.ui.fragments

import android.app.AlertDialog
import android.app.Dialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import com.loadingdialog.LoadingDialog
import com.netnovelreader.R
import com.netnovelreader.ViewModelFactory
import com.netnovelreader.databinding.FragmentReadBinding
import com.netnovelreader.repo.http.paging.NetworkState
import com.netnovelreader.ui.adapters.CatalogAdapter
import com.netnovelreader.utils.get
import com.netnovelreader.utils.sharedPreferences
import com.netnovelreader.utils.toast
import com.netnovelreader.vm.ReadViewModel

class ReadFragment : androidx.fragment.app.Fragment() {

    private var bookname: String? = null
    private var viewModel: ReadViewModel? = null
    private var dialog: AlertDialog? = null
    private var cacheDialog: AlertDialog? = null
    private var catalogView: androidx.recyclerview.widget.RecyclerView? = null
    private lateinit var binding: FragmentReadBinding
    private var loadingDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { bookname = it.getString("bookname") }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_read,
            container,
            false
        )
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        context?.let { loadingDialog = LoadingDialog(context!!) }
        viewModel = activity?.application?.let {
            ViewModelProviders.of(this, ViewModelFactory.getInstance(it))
        }?.get(ReadViewModel::class.java)
        binding.readerView.isDrawTime = false

        binding.viewModel = viewModel
        viewModel?.bookName = bookname ?: ""
        viewModel?.cacheNum = context?.sharedPreferences()
            ?.get(getString(R.string.auto_download_key), true)
            .let { compareValues(it, false) * 3 }
        viewModel?.start()
        viewModel?.brightnessCommand?.observe(this, Observer {
            it ?: return@Observer
            activity?.window?.attributes =
                    activity?.window?.attributes?.apply { screenBrightness = it }
        })
        viewModel?.showDialogCommand?.observe(this, Observer {
            if (it == true) showDialog() else dialog?.dismiss()
        })
        viewModel?.cacheDialogCommand?.observe(this, Observer { showCacheDialog() })
        viewModel?.initPageViewCommand?.observe(this,
            Observer { binding.readerView.prepare(it ?: 1) }
        )
        viewModel?.toastCommand?.observe(this, Observer { it?.let { context?.toast(it) } })
        viewModel?.exitCommand?.observe(this, Observer {
            NavHostFragment.findNavController(this).popBackStack()
        })
        viewModel?.changeSourceCommand?.observe(this, Observer {
            it?.takeIf { it.length > 0 } ?: return@Observer
            val bundle = Bundle().apply {
                putInt("type", SearchFragment.TYPE_CHANGESOURCE)
                putString("bookname", bookname)
                putString("chapterName", it.toString())
            }
            it.delete(0, it.length)
            NavHostFragment.findNavController(this@ReadFragment)
                .navigate(R.id.action_readFragment_to_searchFragment, bundle)
        })
        viewModel?.networkState?.observe(this, Observer {
            when(it) {
                NetworkState.LOADING -> {
                    loadingDialog?.show()
                    binding.retryLayout.visibility = View.GONE
                }
                NetworkState.LOADED -> {
                    loadingDialog?.dismiss()
                    binding.retryLayout.visibility = View.GONE
                }
                else -> {
                    loadingDialog?.dismiss()
                    binding.retryLayout.visibility = View.VISIBLE
                }
            }
        })
    }

    //显示目录
    private fun showDialog() {
        context ?: return
        if (dialog == null) {
            val builder = AlertDialog.Builder(context)
            catalogView = androidx.recyclerview.widget.RecyclerView(context!!)
            catalogView?.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            val adapter = CatalogAdapter(viewModel, viewModel?.allChapters)
            catalogView?.adapter = adapter
            dialog = builder.setView(catalogView).create()
        }
        catalogView?.scrollToPosition((viewModel?.chapterNum?.get() ?: 1) - 1)
        dialog?.show()
        dialog?.window?.setLayout(binding.root.width * 5 / 6, binding.root.height * 9 / 10)
    }

    //显示目录
    private fun showCacheDialog() {
        context ?: return
        androidx.appcompat.app.AlertDialog.Builder(context!!)
            .setTitle(getString(R.string.cache_content))
            .setPositiveButton(R.string.enter) { _, _ ->
                viewModel?.cacheContent()
            }
            .setNeutralButton(R.string.cancel, null)
            .create()
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel?.autoDelCache()
        viewModel?.destroy()
    }
}
