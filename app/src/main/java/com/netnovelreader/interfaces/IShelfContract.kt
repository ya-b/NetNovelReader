package com.netnovelreader.interfaces

/**
 * Created by yangbo on 18-1-13.
 */
interface IShelfContract {
    interface IShelfView : IView {
        fun updateShelf()
        fun checkPermission(permission: String): Boolean
        fun requirePermission(permission: String, reqCode: Int)
    }

    interface IShelfViewModel : IViewModel {
        suspend fun updateBooks()
        suspend fun refreshBookList()
        suspend fun cancelUpdateFlag(bookname: String)
        suspend fun deleteBook(bookname: String)
    }
}