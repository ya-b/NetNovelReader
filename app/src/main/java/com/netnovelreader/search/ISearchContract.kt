package com.netnovelreader.search

import com.netnovelreader.base.IView
import com.netnovelreader.base.IViewModel

/**
 * Created by yangbo on 18-1-14.
 */
interface ISearchContract {
    interface ISearchView: IView<SearchViewModel> {
        fun updateSearchResult(bookname: String?, shCode: Int)
    }
    interface ISearchViewModel: IViewModel<SearchBean> {
        fun updateResultList(bookname: String?, siteinfo: Array<String?>, searchCode: Int): SearchBean.SearchResultBean
        fun getSearchSite(): ArrayList<Array<String?>>?
    }
}