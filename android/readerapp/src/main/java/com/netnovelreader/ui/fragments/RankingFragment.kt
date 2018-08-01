package com.netnovelreader.ui.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import com.netnovelreader.R
import com.netnovelreader.ViewModelFactory
import com.netnovelreader.databinding.FragmentRankingBinding
import com.netnovelreader.ui.adapters.RankingPageListAdapter
import com.netnovelreader.vm.RankingViewModel


class RankingFragment : Fragment() {
    lateinit var binding: FragmentRankingBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_ranking, container, false
        )

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val viewModel = activity?.application?.let {
            ViewModelProviders.of(this, ViewModelFactory.getInstance(it))
        }?.get(RankingViewModel::class.java)

        val adapter = RankingPageListAdapter(viewModel)
        binding.rankingRecycler.adapter = adapter
        viewModel?.ranking?.observe(this, Observer(adapter::submitList))
        viewModel?.networkState?.observe(this, Observer(adapter::setNetworkState))
        viewModel?.searchCommand?.observe(this, Observer {
            it.takeIf { (it?.length ?: 0) > 0  } ?: return@Observer
            val bookname = it!!.toString()
            it.delete(0, it.length)
            val bundle = Bundle().apply {
                putString("bookname", bookname)
                putInt("type", SearchFragment.TYPE_RANKING)
            }
            NavHostFragment.findNavController(this@RankingFragment)
                .navigate(R.id.action_homeFragment_to_searchFragment, bundle)
        })
    }
}
