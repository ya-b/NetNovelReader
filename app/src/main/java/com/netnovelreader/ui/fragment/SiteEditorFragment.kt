package com.netnovelreader.ui.fragment


import android.databinding.DataBindingUtil
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.netnovelreader.R
import com.netnovelreader.common.obtainViewModel
import com.netnovelreader.databinding.FragmentSiteEditorBinding
import com.netnovelreader.viewmodel.SettingViewModel

class SiteEditorFragment : Fragment() {
    val settingViewModel by lazy { activity?.obtainViewModel(SettingViewModel::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentSiteEditorBinding>(inflater,
                R.layout.fragment_site_editor, container, false)
        binding.siteBean = settingViewModel?.editedSite
        return binding.root.apply { this.setBackgroundColor(Color.WHITE) }
    }
}
