package com.netnovelreader.ui.fragments

import android.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.*
import androidx.navigation.fragment.NavHostFragment
import com.netnovelreader.R
import com.netnovelreader.ViewModelFactory
import com.netnovelreader.databinding.DialogEditRuleBinding
import com.netnovelreader.databinding.FragmentSiteSelectorsBinding
import com.netnovelreader.repo.db.SiteSelectorEntity
import com.netnovelreader.ui.activities.MainActivity
import com.netnovelreader.ui.adapters.SiteSelectorPageListAdapter
import com.netnovelreader.vm.SiteSelectorViewModel
import kotlinx.android.synthetic.main.fragment_site_selectors.*

class SiteSelectorsFragment : androidx.fragment.app.Fragment() {
    private var viewModel: SiteSelectorViewModel? = null
    private lateinit var binding: FragmentSiteSelectorsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_site_selectors, container, false
        )
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
        viewModel?.editPreferenceCommand?.observe(this, Observer {
            it?.let { editPreferenceDialog(it) }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel?.destroy()
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
                editPreferenceDialog(
                    SiteSelectorEntity(
                        null,
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        ""
                    )
                )
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

    private fun editPreferenceDialog(entity: SiteSelectorEntity) {
        val binding = DataBindingUtil.inflate<DialogEditRuleBinding>(
            LayoutInflater.from(context),
            R.layout.dialog_edit_rule, null, false
        )
        binding.data = entity
        AlertDialog.Builder(context)
            .setView(binding.root)
            .setPositiveButton(
                R.string.enter,
                { _, _ -> binding.data?.let { viewModel?.savePreference(it) } })
            .setNeutralButton(R.string.cancel, null)
            .create()
            .show()
    }
}
