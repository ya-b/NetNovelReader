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
        suspend fun initData(): Int
        suspend fun nextChapter(): Boolean
        suspend fun previousChapter(): Boolean
        suspend fun pageByCatalog(chapterName: String?): Boolean
        suspend fun updateCatalog(): ObservableArrayList<ReaderBean.Catalog>
        suspend fun downloadChapter(chapterName: String?): Boolean
        suspend fun setRecord(chapterNum: Int, pageNum: Int)
        suspend fun autoRemove()
    }
}