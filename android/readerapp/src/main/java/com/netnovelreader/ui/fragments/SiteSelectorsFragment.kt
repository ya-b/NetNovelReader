package com.netnovelreader.ui.fragments

import android.app.AlertDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import androidx.navigation.fragment.NavHostFragment
import com.netnovelreader.R
import com.netnovelreader.ViewModelFactory
import com.netnovelreader.databinding.FragmentSiteSelectorsBinding
import com.netnovelreader.ui.activities.MainActivity
import com.netnovelreader.ui.adapters.SiteSelectorPageListAdapter
import com.netnovelreader.vm.SiteSelectorViewModel
import kotlinx.android.synthetic.main.fragment_site_selectors.*

class SiteSelectorsFragment : Fragment() {
    private var viewModel: SiteSelectorViewModel? = null
    private lateinit var binding: FragmentSiteSelectorsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_site_selectors, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as MainActivity?)?.setSupportActionBar(binding.siteSelectorToolbar)
        setHasOptionsMenu(true)
        binding.siteSelectorToolbar.setNavigationOnClickListener {
            NavHostFragment.findNavController(this).popBackStack()
        }

        viewModel = activity?.application?.let {
            ViewModelProviders.of(this, ViewModelFactory.getInstance(it))
        }?.get(SiteSelectorViewModel::class.java)
        val adapter = SiteSelectorPageListAdapter(viewModel)
        siteSelectorRecycler.adapter = adapter

        viewModel?.allSiteSelector?.observe(this, Observer(adapter::submitList))

    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.clear()
        inflater?.inflate(R.menu.menu_site_selector, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.updateSitePreference -> showUpdateDialog()
            R.id.addSitePreference -> {

            }
        }
        return true
    }

    private fun showUpdateDialog() {
        AlertDialog.Builder(context)
            .setTitle(getString(R.string.whenconflict))
            .setPositiveButton(R.string.perfer_net) { _, _ ->
                viewModel?.updatePreference(true)
            }
            .setNegativeButton(R.string.perfer_local) { _, _ ->
                viewModel?.updatePreference(false)
            }
            .setNeutralButton(R.string.cancel, null)
            .create()
            .show()
    }
}
