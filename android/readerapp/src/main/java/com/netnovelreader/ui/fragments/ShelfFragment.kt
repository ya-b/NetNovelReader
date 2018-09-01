package com.netnovelreader.ui.fragments

import android.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import com.netnovelreader.R
import com.netnovelreader.ViewModelFactory
import com.netnovelreader.databinding.FragmentShelfBinding
import com.netnovelreader.ui.adapters.ShelfPageListAdapter
import com.netnovelreader.vm.ShelfViewModel
import kotlinx.android.synthetic.main.fragment_shelf.*

class ShelfFragment : androidx.fragment.app.Fragment() {

    private var viewModel: ShelfViewModel? = null
    lateinit var binding: FragmentShelfBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_shelf, container, false
        )
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = activity?.application?.let {
            ViewModelProviders.of(this, ViewModelFactory.getInstance(it))
        }?.get(ShelfViewModel::class.java)

        binding.shelfRefresh.setOnRefreshListener { viewModel?.updateBooks() }
        val adapter = ShelfPageListAdapter(viewModel)
        shelfRecycler.adapter = adapter
        viewModel?.allBooks?.observe(this, Observer(adapter::submitList))
        viewModel?.stopRefershCommand?.observe(this, Observer {
            binding.shelfRefresh.isRefreshing = false
        })
        viewModel?.startReaderFrag?.observe(this, Observer { bookname ->
            if (!bookname.isNullOrEmpty()) {
                val bundle = Bundle().apply { putString("bookname", bookname.toString()) }
                bookname!!.delete(0, bookname.length)   //会调用多次，先这样 todo 优化
                NavHostFragment.findNavController(this@ShelfFragment)
                    .navigate(R.id.action_homeFragment_to_readFragment, bundle)
            }
        })
        viewModel?.dialogCommand?.observe(this, Observer {
            val bookname = it?.toString()?.takeIf { it.isNotEmpty() } ?: return@Observer
            it.delete(0, it.length)
            AlertDialog.Builder(context)
                .setTitle(String.format(getString(R.string.delete_book), bookname))
                .setPositiveButton(R.string.enter, { _,_ -> viewModel?.deleteBook(bookname) })
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show()
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel?.destroy()
    }
}
