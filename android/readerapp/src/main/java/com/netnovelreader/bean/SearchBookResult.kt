package com.netnovelreader.bean

import android.databinding.ObservableField
import java.io.Serializable

data class SearchBookResult(
    val bookname: ObservableField<String>,
    val url: ObservableField<String>,
    val latestChapter: ObservableField<String?>,
    val catalogMap: LinkedHashMap<String, String>
) : Serializable {
    companion object {
        fun new(bookname: String, url: String, latestChapter: String, catalogMap: LinkedHashMap<String, String>) =
            SearchBookResult(ObservableField(bookname), ObservableField(url), ObservableField(latestChapter), catalogMap)
    }
}