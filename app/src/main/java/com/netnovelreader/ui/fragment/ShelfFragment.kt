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
import com.netnovelreader.common.*
import com.netnovelreader.databinding.FragmentShelfBinding
import com.netnovelreader.ui.activity.ReaderActivity
import com.netnovelreader.viewmodel.ShelfViewModel
import kotlinx.coroutines.experimental.launch

class ShelfFragment : Fragment() {
    val shelfViewModel by lazy { activity?.obtainViewModel(ShelfViewModel::class.java) }
    var isFirstResume = true
    lateinit var binding: FragmentShelfBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_shelf, container, false)
        binding.viewModel = shelfViewModel
        binding.shelfRecycler.init(
            RecyclerAdapter(shelfViewModel?.bookList, R.layout.item_shelf, shelfViewModel, true),
            null
        )
        binding.shelfRefresh.setColorSchemeResources(R.color.gray)
        initLiveData()
        return binding.root
    }

    fun initLiveData() {
        shelfViewModel?.paddingCommand?.observe(this, Observer {
            if (it != null && it != 0 && binding.shelfRecycler.paddingTop != it) {
                binding.shelfRecycler.setPadding(0, it, 0, 0)
                binding.shelfRecycler.scrollToPosition(0)
            }
        })
        shelfViewModel?.notRefershCommand?.observe(
            this,
            Observer { binding.shelfRefresh.isRefreshing = false })
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
        if(!isFirstResume && shelfViewModel?.refreshType == 0) return
        launch {
            if (activity!!.checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                shelfViewModel?.refreshBookList()
            }
        }
        isFirstResume = false
    }
}
