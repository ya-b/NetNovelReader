package com.netnovelreader.bean

import android.databinding.ObservableBoolean
import android.databinding.ObservableField

/**
 * 文件： FilterBean
 * 描述：
 * 作者： YangJunQuan   2018-2-12.
 */

data class FilterBean(
        var minorType: ObservableField<String>,
        var selected: ObservableBoolean
)
