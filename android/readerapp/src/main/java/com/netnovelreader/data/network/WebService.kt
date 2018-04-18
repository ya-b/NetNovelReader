package com.netnovelreader.data.network

import com.netnovelreader.bean.*
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

object WebService {
    val zhuiShuShenQi: ZhuiShuShenQiAPI by lazy {
        Retrofit.Builder()
                .baseUrl("http://api.zhuishushenqi.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ZhuiShuShenQiAPI::class.java)
    }
    val novelReader: NovalReaderAPI by lazy {
        Retrofit.Builder()
                .baseUrl("http://139.159.226.67/reader/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(NovalReaderAPI::class.java)
    }

    interface NovalReaderAPI {
        @GET
        fun getPicture(@Url url: String): Call<ResponseBody>

        @GET
        fun getSitePreference(@Url url: String = "rule/query"): Call<RespMessage>

        @POST("login") @FormUrlEncoded
        fun login(@Field("username") username: String, @Field("password") password: String): Call<RespMessage>

        @POST("record/restore")
        fun restoreRecord(@Header("Authorization") token: String): Call<RespMessage>

        @POST("record/save") @Multipart
        fun saveRecord(@Header("Authorization") token: String, @Part file: MultipartBody.Part): Call<RespMessage>
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
        fun seachBookListByTypeAndMajor(
                @Query("gender") gender: String? = "male",
                @Query("type") type: String?,
                @Query("major") major: String?,
                @Query("minor") minor: String? = "",
                @Query("start") start: String? = "0",
                @Query("limit") limit: String? = "50"
        ): Call<NovelList>

        @GET("http://api.zhuishushenqi.com/cats/lv2/statistics")
        fun getNovelCatalogData(): Call<NovelCatalog>
    }
}