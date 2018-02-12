package com.netnovelreader.api

import com.netnovelreader.api.bean.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

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
    fun hotWords(): Call<SearchHotWord>

    /**
     * 完整Url:http://api05iye5.zhuishushenqi.com/book/auto-suggest?query={搜索关键字}&packageName=com.ushaqi.zhuishushenqi
     * 作用：根据搜索关键字提供搜索建议列表
     */
    @GET("http://api05iye5.zhuishushenqi.com/book/auto-suggest?")
    fun searchSuggest(@Query("query") query: String, @Query("packageName") packageName: String): Call<QuerySuggest>


    @GET("http://api.zhuishushenqi.com/book/{id}")
    fun getNovelIntroduce(@Path("id") id: String?): Call<NovelIntroduce>

    /**
     * 作用：根据搜索书名返回书籍列表
     */
    @GET("http://api.zhuishushenqi.com/book/fuzzy-search?")
    fun searchBook(@Query("query") query: String): Call<QueryNovel>

    /**
     * 作用：根据准确已有的作者名字返回该作者名下的所有书籍
     */
    @GET("http://api.zhuishushenqi.com/book/accurate-search?")
    fun searchBookByAuthor(@Query("author") author: String): Call<QueryNovelByAuthor>



    @GET("http://api.zhuishushenqi.com/book/by-categories?")
    fun seachBookListByTypeAndMajor(@Query("gender") gender:String?="male",
                            @Query("type") type:String?,
                            @Query("major") major:String?,
                            @Query("minor") minor:String?="",
                            @Query("start") start:String?="0",
                            @Query("limit") limit:String?="50"):Call<NovelList>


    @GET("http://api.zhuishushenqi.com/cats/lv2/statistics")
    fun getNovelCatalogData(): Call<NovelCatalog>


    @GET
    fun getPicture(@Url url: String): Call<ResponseBody>

}