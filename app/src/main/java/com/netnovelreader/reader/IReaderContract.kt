package com.netnovelreader.reader

import android.databinding.ObservableArrayList
import com.netnovelreader.common.base.IView
import com.netnovelreader.common.base.IViewModel

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
        suspend fun downloadChapter(chapterName: String?): Boolean
        fun setRecord(chapterNum: Int, pageNum: Int)
        fun autoRemove()
    }
}