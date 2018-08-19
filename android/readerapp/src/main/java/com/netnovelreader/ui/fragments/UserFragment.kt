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
import com.netnovelreader.databinding.FragmentUserBinding
import com.netnovelreader.ui.activities.MainActivity
import com.netnovelreader.utils.toast
import com.netnovelreader.vm.UserViewModel

class UserFragment : Fragment() {
    var viewModel: UserViewModel? = null
    lateinit var binding: FragmentUserBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_user, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = activity?.application?.let {
            ViewModelProviders.of(this, ViewModelFactory.getInstance(it))
        }?.get(UserViewModel::class.java)
        binding.viewModel = viewModel

        (activity as MainActivity?)?.setSupportActionBar(binding.loginToolbar)
        setHasOptionsMenu(false)
        binding.loginToolbar.setNavigationOnClickListener {
            NavHostFragment.findNavController(this).popBackStack()
        }
        binding.loginBtn.setOnClickListener {
            viewModel?.login(binding.username.text.toString(), binding.password.text.toString())
        }

        if(viewModel?.isLogin() == true) {
            binding.logoutLayout.visibility = View.VISIBLE
            binding.loginLayout.visibility = View.GONE
            binding.loginToolbar.setTitle(viewModel?.getUserName())
        }

        viewModel?.exitCommand?.observe(this, Observer {
            NavHostFragment.findNavController(this).popBackStack()
        })
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

    override fun onDestroy() {
        super.onDestroy()
        viewModel?.destroy()
    }
}