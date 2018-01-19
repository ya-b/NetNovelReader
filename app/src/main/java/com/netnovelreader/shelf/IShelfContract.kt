package com.netnovelreader.shelf

import com.netnovelreader.base.BindingAdapter
import com.netnovelreader.base.IView
import com.netnovelreader.base.IViewModel

/**
 * Created by yangbo on 18-1-13.
 */
interface IShelfContract {
    interface IShelfView: IView<ShelfViewModel>{
        fun updateShelf(adapter: BindingAdapter<ShelfBean>?)
        fun checkPermission(permission: String): Boolean
        fun requirePermission(permission: String, reqCode: Int)
    }
    interface IShelfViewModel: IViewModel<ShelfBean> {
        fun updateBookList()
    }
}