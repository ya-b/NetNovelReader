package com.netnovelreader.common.base

/**
 * Created by yangbo on 2018/1/11.
 */
interface IView<VM> {
    fun init()
    fun setViewModel(vm: VM)
}