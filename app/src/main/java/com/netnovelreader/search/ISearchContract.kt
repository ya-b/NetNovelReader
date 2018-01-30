package com.netnovelreader.search

import com.netnovelreader.base.IView
import com.netnovelreader.base.IViewModel

/**
 * Created by yangbo on 18-1-14.
 */
interface ISearchContract {
    interface ISearchView : IView<SearchViewModel>
    interface ISearchViewModel : IViewModel<SearchBean> {
        fun addBookToShelf(bookname: String, url: String): String
        fun searchBook(bookname: String?)
        fun saveBookImage(tableName: String, bookname: String)
    }
}