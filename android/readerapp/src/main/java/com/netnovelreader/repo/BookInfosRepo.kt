package com.netnovelreader.repo

import android.app.Application

class BookInfosRepo(app: Application) : Repo(app) {
    private var dao = db.bookInfoDao()

    fun getAllBookInfos() =
        dao.allBooks()

    fun deleteBook(bookname: String) {
        dao.getBookInfo(bookname)?.also { dao.delete(it) }
    }

    fun setMaxOrderToBook(bookname: String) {
        dao.getBookInfo(bookname)
            ?.also {
                it.orderNumber = dao.getMaxOrderNum() + 1
                it.hasUpdate = false
            }
            ?.also { dao.update(it) }

    }
}