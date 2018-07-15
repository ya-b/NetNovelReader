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
import com.netnovelreader.databinding.FragmentSyncRecordBinding
import com.netnovelreader.ui.activities.MainActivity
import com.netnovelreader.utils.toast
import com.netnovelreader.vm.SyncRecordViewModel

class SyncRecordFragment : Fragment() {
    private var viewModel: SyncRecordViewModel? = null
    private lateinit var binding: FragmentSyncRecordBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sync_record,
                container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as MainActivity?)?.setSupportActionBar(binding.syncRecordToolbar)
        binding.syncRecordToolbar.setNavigationOnClickListener {
            NavHostFragment.findNavController(this).popBackStack()
        }

        viewModel = activity?.application?.let {
            ViewModelProviders.of(this, ViewModelFactory.getInstance(it))
        }?.get(SyncRecordViewModel::class.java)
        binding.viewModel = viewModel
        viewModel?.toastCommand?.observe(this, Observer { it?.let { context?.toast(it) } })
    }
}
