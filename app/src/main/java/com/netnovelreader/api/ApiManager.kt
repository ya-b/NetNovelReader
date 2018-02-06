package com.netnovelreader.api

import com.netnovelreader.api.bean.QuerySuggest
import com.netnovelreader.api.bean.SearchHotWord
import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 文件： ApiManager
 * 描述：
 * 作者： YangJunQuan   2018/2/5.
 */
object ApiManager {
    var mAPI: ZhuiShuShenQiAPI? = null
        get() {
            if (null == field) {
                synchronized(ZhuiShuShenQiAPI::class.java) {
                    field = Retrofit.Builder()
                            .baseUrl("http://api.zhuishushenqi.com")
                            .addConverterFactory(GsonConverterFactory.create())
                            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                            .build()
                            .create(ZhuiShuShenQiAPI::class.java)
                }
            }
            return field!!
        }
}

interface ZhuiShuShenQiAPI {


    /**
     * 完整Url：http://api.zhuishushenqi.com/book/search-hotwords
     * 作用：追书神器搜索热词（大约100个搜索热词）
     */
    @GET("http://api.zhuishushenqi.com/book/search-hotwords")
    fun hotWords(): Observable<SearchHotWord>


    /**
     * 完整Url:http://api05iye5.zhuishushenqi.com/book/auto-suggest?query={搜索关键字}&packageName=com.ushaqi.zhuishushenqi
     * 作用：根据搜索关键字提供搜索建议列表
     */
    @GET("http://api05iye5.zhuishushenqi.com/book/auto-suggest?")
    fun searchSuggest(@Query("query") query: String, @Query("packageName") packageName: String): Observable<QuerySuggest>
}


