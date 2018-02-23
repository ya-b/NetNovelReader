package com.netnovelreader.interfaces

import com.netnovelreader.viewmodel.ReaderViewModel

/**
 * Created by yangbo on 18-1-13.
 */
interface IReaderContract {
    interface IReaderView : IView {
        fun showDialog()
    }

    interface IReaderViewModel : IViewModel {
        suspend fun initData(bookName: String, CACHE_NUM: Int): Int
        suspend fun getChapter(
            type: ReaderViewModel.CHAPTERCHANGE,
            chapterName: String?
        )

        suspend fun downloadAndShow()
        suspend fun getCatalog()
        suspend fun setRecord(chapterNum: Int, pageNum: Int)
        suspend fun autoRemove()
    }
}