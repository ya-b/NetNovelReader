package com.netnovelreader.common

import android.databinding.ObservableArrayList
import android.databinding.ObservableList

/**
 * Created by yangbo on 2018/1/24.
 */
class ArrayListChangeListener<T>(private val block: () -> Unit) :
    ObservableList.OnListChangedCallback<ObservableArrayList<T>>() {

    override fun onChanged(p0: ObservableArrayList<T>?) = block()

    override fun onItemRangeChanged(p0: ObservableArrayList<T>?, p1: Int, p2: Int) = block()

    override fun onItemRangeInserted(p0: ObservableArrayList<T>?, p1: Int, p2: Int) = block()

    override fun onItemRangeMoved(p0: ObservableArrayList<T>?, p1: Int, p2: Int, p3: Int) = block()

    override fun onItemRangeRemoved(p0: ObservableArrayList<T>?, p1: Int, p2: Int) = block()
}