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
        fun initData(textAreaWidth: Int, textAreaHeight: Int, txtFontSize: Float)
        fun pageToNext(textAreaWidth: Int, textAreaHeight: Int, txtFontSize: Float)
        fun pageToPrevious(textAreaWidth: Int, textAreaHeight: Int, txtFontSize: Float)
        fun pageByCatalog(chapterName: String, textAreaWidth: Int, textAreaHeight: Int, txtFontSize: Float)
        fun changeFontSize(textAreaWidth: Int, textAreaHeight: Int, txtFontSize: Float)
        fun updateCatalog(): ObservableArrayList<ReaderBean.Catalog>
    }
}