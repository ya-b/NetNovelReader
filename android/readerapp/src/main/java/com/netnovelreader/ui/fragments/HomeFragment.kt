package com.netnovelreader.ui.fragments

import androidx.databinding.DataBindingUtil
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.*
import androidx.navigation.fragment.NavHostFragment

import com.netnovelreader.R
import com.netnovelreader.databinding.FragmentHomeBinding
import com.netnovelreader.ui.activities.MainActivity
import com.netnovelreader.ui.adapters.PageViewAdapter
import com.netnovelreader.utils.get
import com.netnovelreader.utils.sharedPreferences

class HomeFragment : androidx.fragment.app.Fragment() {
    lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_home, container, false
        )
        activity?.let {
            binding.homeViewPager.adapter = PageViewAdapter(
                childFragmentManager,
                arrayOf("本地书架", "热门排行"),
                arrayOf(ShelfFragment::class.java, RankingFragment::class.java)
            )
            binding.homeTab.setupWithViewPager(binding.homeViewPager, false)
        }
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as MainActivity?)?.setSupportActionBar(binding.homeToolbar)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.clear()
        inflater?.inflate(R.menu.menu_shelf, menu)
        context?.sharedPreferences()
            ?.get(getString(R.string.tokenKey), "")
            ?.takeIf { it.isNotEmpty() }
            ?.let { context?.sharedPreferences()?.get(getString(R.string.usernameKey), "user") }
            ?.let { menu?.findItem(R.id.login)?.setTitle(it) }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.login -> {
                NavHostFragment.findNavController(this)
                    .navigate(R.id.action_homeFragment_to_loginFragment)
            }
            R.id.search_button -> {
                val bundle = Bundle().apply { putInt("type", SearchFragment.TYPE_SEARCH) }
                NavHostFragment.findNavController(this)
                    .navigate(R.id.action_homeFragment_to_searchFragment, bundle)
            }
            R.id.action_settings -> {
                NavHostFragment.findNavController(this)
                    .navigate(R.id.action_homeFragment_to_settingFragment)
            }
            R.id.edit_site_selector -> {
                NavHostFragment.findNavController(this)
                    .navigate(R.id.action_homeFragment_to_siteSelectorsFragment)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
