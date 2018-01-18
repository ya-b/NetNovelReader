package com.netnovelreader.search

import android.util.Log
import com.netnovelreader.data.database.SearchSQLManager
import com.netnovelreader.data.database.ShelfSQLManager
import com.netnovelreader.data.network.SearchBook
import com.netnovelreader.utils.id2Bookname
import java.net.URLEncoder

/**
 * Created by yangbo on 18-1-14.
 */
class SearchViewModel : ISearchContract.ISearchViewModel {
    private var searchBean: SearchBean? = null

    override fun getModel(): SearchBean? {
        searchBean ?: synchronized(this) {
            searchBean ?: run { searchBean = SearchBean() }
        }
        return searchBean
    }

    override fun addBookToShelf(bookname: String, url: String): String{
        return id2Bookname(ShelfSQLManager().addBookToShelf(bookname, url))
    }

    override fun updateResultList(bookname: String?, siteinfo: Array<String?>, searchCode: Int): SearchBean.SearchResultBean {
        bookname ?: return SearchBean.SearchResultBean(searchCode, "","")
        var result: String? = null
        val url = siteinfo[1]!!.replace(SearchSQLManager.SEARCH_NAME, URLEncoder.encode(bookname, siteinfo[7]))
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
        val s = result.split("~~~")
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
}