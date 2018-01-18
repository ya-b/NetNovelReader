package com.netnovelreader.reader

import com.netnovelreader.base.IView
import com.netnovelreader.base.IViewModel

/**
 * Created by yangbo on 18-1-13.
 */
interface IReaderContract {
    interface IReaderView: IView<ReaderViewModel>{
    }
    interface IReaderViewModel: IViewModel<ReaderBean> {
        fun getChapterText(chapterNum: Int, dirName: String): String
    }
}