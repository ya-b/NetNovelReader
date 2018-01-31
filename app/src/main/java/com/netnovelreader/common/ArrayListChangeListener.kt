package com.netnovelreader.common

import android.databinding.ObservableArrayList
import android.databinding.ObservableList
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * Created by yangbo on 2018/1/24.
 */
class ArrayListChangeListener<T>(private val adapter: BindingAdapter<T>) :
        ObservableList.OnListChangedCallback<ObservableArrayList<T>>() {

    override fun onChanged(p0: ObservableArrayList<T>?) {
        notifyDataSetChanged()
    }

    override fun onItemRangeChanged(p0: ObservableArrayList<T>?, p1: Int, p2: Int) {
        notifyDataSetChanged()
    }

    override fun onItemRangeInserted(p0: ObservableArrayList<T>?, p1: Int, p2: Int) {
        notifyDataSetChanged()
    }

    override fun onItemRangeMoved(p0: ObservableArrayList<T>?, p1: Int, p2: Int, p3: Int) {
        notifyDataSetChanged()
    }

    override fun onItemRangeRemoved(p0: ObservableArrayList<T>?, p1: Int, p2: Int) {
        notifyDataSetChanged()
    }

    private fun notifyDataSetChanged() {
        Observable.just(1).observeOn(AndroidSchedulers.mainThread()).subscribe {
            adapter.notifyDataSetChanged()
        }
    }
}