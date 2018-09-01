package com.netnovelreader

interface IProgressListener {

    fun onProgress(
        finshedChapterNum: Int,
        totalChapterNum: Int,
        finshedBookNum: Int,
        totalBookNum: Int
    )
}