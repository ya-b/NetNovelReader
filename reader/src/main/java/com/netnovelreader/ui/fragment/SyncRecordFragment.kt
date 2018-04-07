package com.netnovelreader.ui.fragment

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netnovelreader.R
import com.netnovelreader.common.obtainViewModel
import com.netnovelreader.databinding.FragmentSyncRecordBinding
import com.netnovelreader.viewmodel.SettingViewModel


class SyncRecordFragment : Fragment() {
    private var mViewModel: SettingViewModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mViewModel = activity?.obtainViewModel(SettingViewModel::class.java)

        return DataBindingUtil.inflate<FragmentSyncRecordBinding>(inflater, R.layout.fragment_sync_record,
                container, false)
                .apply { viewModel = mViewModel }
                .root
    }
}
