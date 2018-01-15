package com.netnovelreader.search

import android.util.Log
import android.view.View
import com.netnovelreader.base.IClickEvent
import com.netnovelreader.data.database.ChapterSQLManager
import com.netnovelreader.data.database.SearchSQLManager
import com.netnovelreader.data.database.ShelfSQLManager
import com.netnovelreader.data.network.ParseHtml
import com.netnovelreader.data.network.SearchBook
import com.netnovelreader.utils.id2Bookname
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.item_search_recycler_view.view.*
import java.net.URLEncoder

/**
 * Created by yangbo on 18-1-14.
 */
class SearchViewModel : ISearchContract.ISearchViewModel {
    private var searchBean: SearchBean? = null

    override fun getModel(): SearchBean? {
        searchBean ?: run {
            synchronized(this) {
                searchBean ?: run { searchBean = SearchBean() }
            }
        }
        return searchBean
    }

    fun addBook(bookname: String, url: String): Boolean{
        var bookPath = id2Bookname(ShelfSQLManager().addBookToShelf(bookname, url))
        var map = ParseHtml().getCatalog(url)
        return ChapterSQLManager().createTable(bookPath).addAllChapter(map, bookPath)
    }

    override fun updateResultList(bookname: String?, siteinfo: Array<String?>, searchCode: Int): SearchBean.SearchResultBean {
        bookname ?: return SearchBean.SearchResultBean(searchCode, "","")
        var result: String? = null
        var url = siteinfo[1]!!.replace(SearchSQLManager.SEARCH_NAME, URLEncoder.encode(bookname, siteinfo[7]))
        try{
            if (siteinfo[0].equals("0")) {
                result = SearchBook().search(url, siteinfo[4] ?: "", siteinfo[6] ?: "")
            } else {
                result = SearchBook().search(url, siteinfo[2] ?: "", siteinfo[3] ?: "",
                        siteinfo[4] ?: "", siteinfo[5] ?: "", siteinfo[6] ?: "")
            }
        }catch (e: Exception){
            result ?: return SearchBean.SearchResultBean(searchCode, "","")
        }
        result ?: return SearchBean.SearchResultBean(searchCode, "","")
        var s = result.split("~")
        return SearchBean.SearchResultBean(searchCode, s[0], s[1])
    }

    override fun getSearchSite(): ArrayList<Array<String?>>? {
        val sqlManager = SearchSQLManager()
        val cursor = sqlManager.queryAll()
        cursor ?: return null
        val siteList = ArrayList<Array<String?>>()
        while (cursor.moveToNext()) {
            siteList.add(Array<String?>(8){it -> cursor.getString(it + 2 ) })
        }
        cursor.close()
        sqlManager.closeDB()
        return siteList
    }


//    var searchCode = 0
//    fun search(bookname: String, shCode: Int){
//        searchCode = shCode
//        getSearchSite()?.forEach {
//            Observable.create<SearchBean.SearchResultBean> { e -> e.onNext(updateResultList(bookname, it, shCode)) }
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe { result ->
//                        if (result.reqCode == searchCode && result.url.length > 0) {
//                            getModel()?.resultList?.add(result)
//                        }
//                    }
//        }
//    }
}