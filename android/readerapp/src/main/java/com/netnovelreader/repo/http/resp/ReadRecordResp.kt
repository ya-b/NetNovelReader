package com.netnovelreader.repo.http.resp

import com.netnovelreader.repo.db.BookInfoEntity

data class ReadRecordResp (
    var ret: Int,
    var books: ArrayList<BookInfoEntity>? = null
)