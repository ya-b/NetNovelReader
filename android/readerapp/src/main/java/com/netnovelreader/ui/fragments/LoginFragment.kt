package com.netnovelreader.ui.fragments

import android.app.Activity
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
import com.netnovelreader.databinding.FragmentLoginBinding
import com.netnovelreader.ui.activities.MainActivity
import com.netnovelreader.utils.toast
import com.netnovelreader.vm.LoginViewModel

class LoginFragment : Fragment() {
    var viewModel: LoginViewModel? = null
    lateinit var binding: FragmentLoginBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        binding.login.setOnClickListener {
            viewModel?.login(binding.username.text.toString(), binding.password.text.toString())
        }
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as MainActivity?)?.setSupportActionBar(binding.loginToolbar)
        binding.loginToolbar.setNavigationOnClickListener {
            NavHostFragment.findNavController(this).popBackStack()
        }

        viewModel = activity?.application?.let {
            ViewModelProviders.of(this, ViewModelFactory.getInstance(it))
        }?.get(LoginViewModel::class.java)
        binding.viewModel = viewModel
        viewModel?.toastCommand?.observe(this, Observer {
            it ?: return@Observer
            if(it == Activity.RESULT_OK.toString()) {
                binding.username.clearFocus()
                binding.password.clearFocus()
                NavHostFragment.findNavController(this).popBackStack()
            } else {
                context?.toast(it)
            }
        })
    }
}