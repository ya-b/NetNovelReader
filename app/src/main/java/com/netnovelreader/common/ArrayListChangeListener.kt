package com.netnovelreader.common

import android.databinding.ObservableArrayList
import android.databinding.ObservableList
import android.os.Handler
import android.os.Looper

/**
 * Created by yangbo on 2018/1/24.
 */
class ArrayListChangeListener<T, E>(private val adapter: RecyclerAdapter<T, E>) :
    ObservableList.OnListChangedCallback<ObservableArrayList<T>>() {

    override fun onChanged(p0: ObservableArrayList<T>?) {
        Looper.getMainLooper()
        Handler(Looper.getMainLooper()).post { adapter.notifyDataSetChanged() }
    }

    override fun onItemRangeChanged(p0: ObservableArrayList<T>?, p1: Int, p2: Int) {
        Handler(Looper.getMainLooper()).post {
            adapter.notifyItemRangeRemoved(p1 + 1, p2)
            adapter.notifyItemRangeInserted(p1 + 1, p2)
        }
    }

    override fun onItemRangeInserted(p0: ObservableArrayList<T>?, p1: Int, p2: Int) {
        Handler(Looper.getMainLooper()).post { adapter.notifyItemRangeInserted(p1 + 1, p2) }
    }

    override fun onItemRangeMoved(p0: ObservableArrayList<T>?, p1: Int, p2: Int, p3: Int) {
        Handler(Looper.getMainLooper()).post {
            adapter.notifyItemRangeRemoved(p1 + 1, p3)
            adapter.notifyItemRangeInserted(p2 + 1, p3)
        }
    }

    override fun onItemRangeRemoved(p0: ObservableArrayList<T>?, p1: Int, p2: Int) {
        Handler(Looper.getMainLooper()).post {
            adapter.notifyItemRangeRemoved(p1 + 1, p2)
        }
    }
}