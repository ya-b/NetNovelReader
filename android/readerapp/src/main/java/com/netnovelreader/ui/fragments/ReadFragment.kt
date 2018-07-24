package com.netnovelreader.ui.fragments

import android.app.AlertDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netnovelreader.R
import com.netnovelreader.ViewModelFactory
import com.netnovelreader.databinding.FragmentReadBinding
import com.netnovelreader.ui.adapters.CatalogAdapter
import com.netnovelreader.utils.get
import com.netnovelreader.utils.sharedPreferences
import com.netnovelreader.vm.ReadViewModel
import kotlinx.android.synthetic.main.fragment_read.*

class ReadFragment : Fragment() {

    private var bookname: String? = null
    private var viewModel: ReadViewModel? = null
    private var dialog: AlertDialog? = null
    private var catalogView: RecyclerView? = null
    private lateinit var binding: FragmentReadBinding

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

        viewModel = activity?.application?.let {
            ViewModelProviders.of(this, ViewModelFactory.getInstance(it))
        }?.get(ReadViewModel::class.java)
        binding.readerView.isDrawTime = false
//        binding.readerView.isDrawTime = context?.sharedPreferences()
//            ?.get(getString(R.string.full_screen_key), false) ?: false
        binding.viewModel = viewModel
        viewModel?.bookName = bookname ?: ""
        viewModel?.cacheNum = context?.sharedPreferences()
            ?.get(getString(R.string.auto_download_key), true)
            .let { compareValues(it, false) * 3 }
        viewModel?.start()
        viewModel?.brightnessCommand?.observe(this, Observer {
            it ?: return@Observer
            activity?.window?.attributes = activity?.window?.attributes?.apply { screenBrightness = it }
        })
        viewModel?.showDialogCommand?.observe(this, Observer {
            if (it == true) showDialog() else dialog?.dismiss()
        })
        viewModel?.prepare()?.let { binding.readerView.prepare(it) }
    }

    //显示目录
    private fun showDialog() {
        if (dialog == null) {
            val builder = AlertDialog.Builder(context)
            catalogView = RecyclerView(context)
            catalogView?.layoutManager = LinearLayoutManager(context)
            val adapter = CatalogAdapter(viewModel, viewModel?.allChapters)
            catalogView?.adapter = adapter
            dialog = builder.setView(catalogView).create()
        }
        catalogView?.scrollToPosition((viewModel?.chapterNum?.get() ?: 1) - 1)
        dialog?.show()
        dialog?.window?.setLayout(readerView.width * 5 / 6, readerView.height * 9 / 10)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel?.autoDelCache()
    }
}
