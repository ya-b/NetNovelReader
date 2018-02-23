package com.netnovelreader.common

import android.databinding.ObservableArrayList
import android.databinding.ObservableList
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

/**
 * Created by yangbo on 2018/1/24.
 */
class ArrayListChangeListener<T>(private val block: () -> Unit) :
    ObservableList.OnListChangedCallback<ObservableArrayList<T>>() {

    override fun onChanged(p0: ObservableArrayList<T>?) {
        launch(UI) { block() }
    }

    override fun onItemRangeChanged(p0: ObservableArrayList<T>?, p1: Int, p2: Int) {
        launch(UI) { block() }
    }

    override fun onItemRangeInserted(p0: ObservableArrayList<T>?, p1: Int, p2: Int) {
        launch(UI) { block() }
    }

    override fun onItemRangeMoved(p0: ObservableArrayList<T>?, p1: Int, p2: Int, p3: Int) {
        launch(UI) { block() }
    }

    override fun onItemRangeRemoved(p0: ObservableArrayList<T>?, p1: Int, p2: Int) {
        launch(UI) { block() }
    }
}