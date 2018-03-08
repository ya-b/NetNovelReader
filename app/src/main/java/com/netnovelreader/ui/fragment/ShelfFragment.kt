package com.netnovelreader.ui.fragment


import android.app.AlertDialog
import android.arch.lifecycle.Observer
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netnovelreader.R
import com.netnovelreader.common.RecyclerAdapter
import com.netnovelreader.common.checkPermission
import com.netnovelreader.common.init
import com.netnovelreader.common.obtainViewModel
import com.netnovelreader.data.PreferenceManager
import com.netnovelreader.databinding.FragmentShelfBinding
import com.netnovelreader.ui.activity.ReaderActivity
import com.netnovelreader.ui.activity.ShelfActivity
import com.netnovelreader.viewmodel.ShelfViewModel
import kotlinx.android.synthetic.main.activity_shelf.*
import kotlinx.coroutines.experimental.launch

class ShelfFragment : Fragment() {
    var shelfViewModel: ShelfViewModel? = null
    var isFirstResume = true
    lateinit var binding: FragmentShelfBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        shelfViewModel = activity?.obtainViewModel(ShelfViewModel::class.java)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_shelf, container, false)
        binding.viewModel = shelfViewModel
        RecyclerAdapter(shelfViewModel?.bookList, R.layout.item_shelf, shelfViewModel, true)
            .let { binding.shelfRecycler.init(it, null) }
        binding.shelfRefresh.setColorSchemeResources(R.color.gray)
        initLiveData()
        (activity as ShelfActivity).shelfTab.run {
            post {
                binding.shelfRecycler.setPadding(0, height, 0, 0)
                binding.shelfRecycler.scrollToPosition(0)
            }
        }
        return binding.root
    }

    fun initLiveData() {
        shelfViewModel?.stopRefershCommand?.observe(this, Observer {
            binding.shelfRefresh.isRefreshing = false
        })
        shelfViewModel?.readBookCommand?.observe(this, Observer {
            if (it.isNullOrEmpty()) return@Observer
            shelfViewModel?.refreshType = 1
            val intent = Intent(activity, ReaderActivity::class.java).apply {
                putExtra("bookname", it)
                putExtra("themeid", PreferenceManager.getThemeId(activity!!.baseContext))
            }
            startActivity(intent)
        })
        shelfViewModel?.showDialogCommand?.observe(this, Observer {
            AlertDialog.Builder(activity)
                .setTitle(getString(R.string.deleteBook).replace("book", it!!))
                .setPositiveButton(
                    R.string.yes,
                    { _, _ -> launch { shelfViewModel?.deleteBook(it) } })
                .setNegativeButton(R.string.no, null)
                .create()
                .show()
        })
    }

    override fun onResume() {
        super.onResume()
        if (!isFirstResume && shelfViewModel?.refreshType == 0) return
        if (activity!!.checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            launch { shelfViewModel?.refreshBookList() }
        }
        isFirstResume = false
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.shelfRecycler.adapter = null
    }
}
