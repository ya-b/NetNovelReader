package com.netnovelreader.reader

import android.databinding.ObservableArrayList
import com.netnovelreader.base.IView
import com.netnovelreader.base.IViewModel

/**
 * Created by yangbo on 18-1-13.
 */
interface IReaderContract {
    interface IReaderView : IView<ReaderViewModel> {
        fun showDialog()
    }

    interface IReaderViewModel : IViewModel<ReaderBean> {
        fun initData(): Int
        fun nextChapter(): Boolean
        fun previousChapter(): Boolean
        fun pageByCatalog(chapterName: String?): Boolean
        fun updateCatalog(): ObservableArrayList<ReaderBean.Catalog>
    }
}