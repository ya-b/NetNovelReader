package com.netnovelreader.bean

/**
 * 文件： SearchHotWord
 * 描述：
 * 作者： YangJunQuan   2018/2/5.
 */

data class SearchHotWord(
    var isOk: Boolean = false,
    var searchHotWords: List<SearchHotWordsBean>? = null
)


data class SearchHotWordsBean(
    var word: String? = null,
    var times: Int = 0,
    var isNew: Int = 0,
    var soaring: Int = 0
)


