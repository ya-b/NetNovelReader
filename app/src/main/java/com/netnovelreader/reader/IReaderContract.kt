package com.netnovelreader.reader

import com.netnovelreader.common.base.IView
import com.netnovelreader.common.base.IViewModel
import kotlinx.coroutines.experimental.Deferred

/**
 * Created by yangbo on 18-1-13.
 */
interface IReaderContract {
    interface IReaderView : IView<ReaderViewModel> {
        fun showDialog()
    }

    interface IReaderViewModel : IViewModel<ReaderBean> {
        suspend fun initData(): Int
        suspend fun getChapter(
            type: ReaderViewModel.CHAPTERCHANGE,
            chapterName: String?
        ): Deferred<Boolean>

        suspend fun getCatalog()
        suspend fun downloadAndShow(): Deferred<Boolean>
        suspend fun setRecord(chapterNum: Int, pageNum: Int)
        suspend fun autoRemove()
    }
}