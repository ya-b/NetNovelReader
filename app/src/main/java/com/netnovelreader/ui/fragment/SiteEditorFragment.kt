package com.netnovelreader.ui.fragment


import android.app.AlertDialog
import android.arch.lifecycle.Observer
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.netnovelreader.R
import com.netnovelreader.common.obtainViewModel
import com.netnovelreader.databinding.FragmentSiteEditorBinding
import com.netnovelreader.viewmodel.SettingViewModel
import kotlinx.coroutines.experimental.launch

class SiteEditorFragment : Fragment() {
    val settingViewModel by lazy { activity?.obtainViewModel(SettingViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingViewModel?.editTextCommand?.observe(this, Observer {
            it ?: return@Observer
            this@SiteEditorFragment.context?.apply {
                val editText = EditText(this).apply { setText(it) }
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.edit_site))
                    .setView(editText)
                    .setPositiveButton(R.string.save, { _, _ ->
                        launch { settingViewModel?.saveText(editText.text.toString()) }
                    })
                    .setNegativeButton(R.string.no, null)
                    .create()
                    .show()
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentSiteEditorBinding>(
            inflater,
            R.layout.fragment_site_editor, container, false
        )
        binding.siteBean = settingViewModel?.edittingSite
        binding.viewModel = settingViewModel
        return binding.root
    }

    companion object {
        val instance by lazy { SiteEditorFragment() }
    }
}
