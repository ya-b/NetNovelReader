package com.netnovelreader.common

import android.databinding.ObservableArrayList
import android.databinding.ObservableList
import android.support.annotation.UiThread

/**
 * Created by yangbo on 2018/1/24.
 */
class ArrayListChangeListener<T>(private val adapter: BindingAdapter<T>) :
    ObservableList.OnListChangedCallback<ObservableArrayList<T>>() {

    @UiThread
    override fun onChanged(p0: ObservableArrayList<T>?) {
        adapter.notifyDataSetChanged()
    }

    @UiThread
    override fun onItemRangeChanged(p0: ObservableArrayList<T>?, p1: Int, p2: Int) {
        adapter.notifyDataSetChanged()
    }

    @UiThread
    override fun onItemRangeInserted(p0: ObservableArrayList<T>?, p1: Int, p2: Int) {
        adapter.notifyDataSetChanged()
    }

    @UiThread
    override fun onItemRangeMoved(p0: ObservableArrayList<T>?, p1: Int, p2: Int, p3: Int) {
        adapter.notifyDataSetChanged()
    }

    @UiThread
    override fun onItemRangeRemoved(p0: ObservableArrayList<T>?, p1: Int, p2: Int) {
        adapter.notifyDataSetChanged()
    }
}