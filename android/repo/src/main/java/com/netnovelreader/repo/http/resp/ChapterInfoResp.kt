package com.netnovelreader.repo.http.resp

import java.io.Serializable

data class ChapterInfoResp (var id: Int, var chapterName: String, var chapterUrl: String) :
    Serializable