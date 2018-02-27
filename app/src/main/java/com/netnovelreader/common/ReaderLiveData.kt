package com.netnovelreader.common

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

class ReaderLiveData<T> : MutableLiveData<T>() {

    override fun observe(owner: LifecycleOwner, observer: Observer<T>) {
        launch(UI) { super.observe(owner, Observer<T> { t -> observer.onChanged(t) }) }
    }

    override fun setValue(value: T?) {
        launch(UI) { super.setValue(value) }
    }

    fun call() {
        launch(UI) { value = null }
    }
}