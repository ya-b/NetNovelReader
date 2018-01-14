package com.netnovelreader.search

import android.view.View
import com.netnovelreader.base.IClickEvent
import com.netnovelreader.data.database.BaseSQLManager
import com.netnovelreader.data.database.SearchSQLManager
import com.netnovelreader.data.network.SearchBook
import java.net.URLEncoder

/**
 * Created by yangbo on 18-1-14.
 */
class SearchViewModel : ISearchContract.ISearchViewModel{
    private var searchModel: SearchModel? = null

    override fun getModel(): SearchModel? {
        searchModel ?: run{
            synchronized(this){
                searchModel ?: run{ searchModel = SearchModel() }
            }
        }
        return searchModel
    }

    fun updateResultList(bookname: String?) : Boolean {
        bookname ?: return false
        val searchBook = SearchBook()
        getModel()?.resultList?.clear()
        val sqlManager = SearchSQLManager()
        val cursor = sqlManager.queryAll()
        cursor ?: return false
        var result: String?
        if (cursor.moveToNext()){
            val charset = cursor.getString(cursor.getColumnIndex(BaseSQLManager.SEARCHCHARSET))
            val url = cursor.getString(cursor.getColumnIndex(BaseSQLManager.SEARCHURL))
                    .replace(SearchSQLManager.SEARCH_NAME, URLEncoder.encode(bookname, charset))
            val noRedirectSelector = cursor.getString(cursor.getColumnIndex(BaseSQLManager.NOREDIRECTSELECTOR))
            if(cursor.getInt(cursor.getColumnIndex(BaseSQLManager.ISREDIRECT)) == 0){
                result = searchBook.search(url, noRedirectSelector)
            }else{
                result = searchBook.search(url, cursor.getString(cursor.getColumnIndex(BaseSQLManager.REDIRECTFILELD)),
                        cursor.getString(cursor.getColumnIndex(BaseSQLManager.REDIRECTSELECTOR)), noRedirectSelector)
            }
            if(result != null){
                getModel()?.resultList?.add(SearchModel.SearchResultBean(result))
            }
        }
        cursor?.close()
        sqlManager.closeDB()
        return true
    }

    class SearchClickEvent : IClickEvent {
        fun downloadBook(v: View){

        }
    }
}