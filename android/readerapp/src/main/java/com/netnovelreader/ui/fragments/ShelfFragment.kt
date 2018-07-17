package com.netnovelreader.ui.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import androidx.navigation.fragment.NavHostFragment
import com.netnovelreader.R
import com.netnovelreader.ViewModelFactory
import com.netnovelreader.databinding.FragmentShelfBinding
import com.netnovelreader.ui.activities.MainActivity
import com.netnovelreader.ui.adapters.ShelfPageListAdapter
import com.netnovelreader.vm.ShelfViewModel
import kotlinx.android.synthetic.main.fragment_shelf.*

class ShelfFragment : Fragment() {

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
        (activity as MainActivity?)?.setSupportActionBar(binding.shelfToolbar)
        setHasOptionsMenu(true)

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
                    .navigate(R.id.action_shelfFragment_to_readFragment, bundle)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.clear()
        inflater?.inflate(R.menu.menu_shelf, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.search_button -> {
                NavHostFragment.findNavController(this)
                    .navigate(R.id.action_shelfFragment_to_searchFragment)
            }
            R.id.action_settings -> {

            }
            R.id.edit_site_selector -> {
                NavHostFragment.findNavController(this)
                    .navigate(R.id.action_shelfFragment_to_siteSelectorsFragment)
            }
            R.id.login -> {
                NavHostFragment.findNavController(this)
                    .navigate(R.id.action_shelfFragment_to_loginFragment)
            }
            R.id.syncRecord -> {
                NavHostFragment.findNavController(this)
                    .navigate(R.id.action_shelfFragment_to_syncRecordFragment)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
