package com.netnovelreader.repo.http.resp

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SearchBookResp(
    var bookname: String,
    var url: String,
    var imageUrl: String,
    var latestChapter: String
): Parcelable