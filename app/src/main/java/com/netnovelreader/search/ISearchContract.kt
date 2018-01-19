package com.netnovelreader.search

import android.databinding.ObservableInt
import com.netnovelreader.base.IView
import com.netnovelreader.base.IViewModel

/**
 * Created by yangbo on 18-1-14.
 */
interface ISearchContract {
    interface ISearchView: IView<SearchViewModel> {
        fun updateSearchResult(bookname: String?, shCode: ObservableInt)
    }
    interface ISearchViewModel: IViewModel<SearchBean> {
        fun addBookToShelf(bookname: String, url: String): String
        fun searchBookFromSite(bookname: String, siteinfo: Array<String?>, searchCode: Int)
        fun searchBook(bookname: String?)
    }
}