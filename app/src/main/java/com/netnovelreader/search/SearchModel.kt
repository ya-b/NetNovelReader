package com.netnovelreader.search

import android.databinding.BaseObservable
import android.databinding.Bindable
import android.databinding.ObservableArrayList
import java.util.Vector

/**
 * Created by yangbo on 18-1-14.
 */
class SearchModel : BaseObservable() {
    @Bindable
    var resultList: ObservableArrayList<SearchResultBean>
    init {
        resultList = ObservableArrayList<SearchResultBean>()
    }
    class SearchResultBean(val url: String)
}