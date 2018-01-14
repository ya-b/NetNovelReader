package com.netnovelreader.reader

import com.netnovelreader.base.IModel
import com.netnovelreader.base.IView
import com.netnovelreader.base.IViewModel

/**
 * Created by yangbo on 18-1-13.
 */
interface IReaderContract {
    interface IReaderView: IView<ReaderViewModel>{
        fun updateText(boolean: Boolean)
    }
    interface IReaderViewModel: IViewModel<ReaderModel> {
        fun getChapterText(boolean: Boolean): Array<StringBuilder>
    }
}