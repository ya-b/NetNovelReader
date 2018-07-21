package com.netnovelreader.ui.fragments


import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import android.support.v7.widget.Toolbar
import android.view.View
import androidx.navigation.fragment.NavHostFragment
import com.netnovelreader.R
import com.netnovelreader.ui.activities.MainActivity

class SettingFragment : PreferenceFragmentCompat() {
    lateinit var toolbar: Toolbar

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        addPreferencesFromResource(R.xml.pref_setting)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar = view.findViewById<Toolbar>(R.id.settingToolbar)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        toolbar.setNavigationOnClickListener {
            NavHostFragment.findNavController(this).popBackStack()
        }
        (activity as MainActivity?)?.setSupportActionBar(toolbar)

    }
}
