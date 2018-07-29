package com.netnovelreader.repo.http.resp

import java.io.Serializable

data class BookLinkResp(
    val bookname: String,
    val author: String,
    val latestChapter: String,
    val updateTime: String
) : Serializable