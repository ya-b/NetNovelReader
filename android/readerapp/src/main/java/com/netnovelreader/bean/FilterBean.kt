package com.netnovelreader.bean

import android.databinding.ObservableBoolean
import android.databinding.ObservableField

data class FilterBean(
        var minorType: ObservableField<String>,
        var selected: ObservableBoolean
)
