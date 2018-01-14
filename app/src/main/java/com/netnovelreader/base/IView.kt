package com.netnovelreader.base

/**
 * Created by yangbo on 2018/1/11.
 */
interface IView<VM> {
    fun initView()
    fun setViewModel(vm: VM)
}