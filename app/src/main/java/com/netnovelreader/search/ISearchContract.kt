package com.netnovelreader.search

import com.netnovelreader.base.BindingAdapter
import com.netnovelreader.base.IView
import com.netnovelreader.base.IViewModel
import com.netnovelreader.shelf.ShelfModel
import com.netnovelreader.shelf.ShelfViewModel

/**
 * Created by yangbo on 18-1-14.
 */
interface ISearchContract {
    interface ISearchView: IView<SearchViewModel> {
        fun updateSearchResult(bookname: String?)
    }
    interface ISearchViewModel: IViewModel<SearchModel> {

    }
}