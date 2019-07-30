package com.netnovelreader.ui.fragments


import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.appcompat.widget.Toolbar
import android.view.View
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.SwitchPreference
import com.netnovelreader.R
import com.netnovelreader.ui.activities.MainActivity

class SettingFragment : PreferenceFragmentCompat() {
    lateinit var toolbar: Toolbar

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = context?.applicationContext?.packageName
        addPreferencesFromResource(R.xml.pref_setting)
        //todo 优化

        (findPreference(getString(R.string.nightModeKey)) as SwitchPreference?)?.setOnPreferenceChangeListener { _, _ ->
            activity?.recreate()
            true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar = view.findViewById(R.id.settingToolbar)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as MainActivity?)?.setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            NavHostFragment.findNavController(this).popBackStack()
        }

    }
}
