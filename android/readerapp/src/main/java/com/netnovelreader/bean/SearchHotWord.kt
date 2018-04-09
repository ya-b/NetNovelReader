package com.netnovelreader.bean

data class SearchHotWord(
        var isOk: Boolean = false,
        var searchHotWords: List<SearchHotWordsBean>? = null
) {
    data class SearchHotWordsBean(
            var word: String? = null,
            var times: Int = 0,
            var isNew: Int = 0,
            var soaring: Int = 0
    )
}