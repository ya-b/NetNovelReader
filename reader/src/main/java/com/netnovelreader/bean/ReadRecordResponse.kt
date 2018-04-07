package com.netnovelreader.bean

import com.netnovelreader.data.local.db.ShelfBean

data class ReadRecordResponse(val books: ArrayList<ReadRecordBean>){
    data class ReadRecordBean(
            var i: Int? = null,
            var b: String?,
            var d: String? = null,
            var r: String? = null,
            var s: String? = null,
            var l: String? = null,
            var a: Int? = null
    ){
        fun toSitePreferenceBean(): ShelfBean{
            return ShelfBean(i, b, d, r, s, l, a)
        }
    }
}