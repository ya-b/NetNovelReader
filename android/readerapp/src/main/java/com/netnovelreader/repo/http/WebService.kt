package com.netnovelreader.repo.http

import com.netnovelreader.repo.db.BookInfoEntity
import com.netnovelreader.repo.db.SiteSelectorEntity
import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

object WebService {
    val readerAPI: NovalReaderAPI by lazy {
        Retrofit.Builder()
            .client(
                OkHttpClient.Builder()
                    .connectTimeout(3, TimeUnit.SECONDS)
                    .addNetworkInterceptor(HttpLoggingInterceptor())
                    .build()
            )
            .baseUrl("http://139.159.226.67/reader/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(NovalReaderAPI::class.java)
    }
    val searchBook by lazy { SearchBook() }

    interface NovalReaderAPI {
        @GET
        fun getSiteSelectorList(@Url url: String = "rule/query"): Observable<List<SiteSelectorEntity>>

        @POST("login")
        @FormUrlEncoded
        fun login(@Field("username") username: String, @Field("password") password: String): Observable<ResponseBody>

        @POST("record/restore")
        fun restoreRecord(@Header("Authorization") token: String): Observable<ArrayList<BookInfoEntity>>

        @POST("record/save")
        @Multipart
        fun saveRecord(@Header("Authorization") token: String, @Part file: MultipartBody.Part): Observable<ResponseBody>

        @GET
        fun request(@Url url: String): Call<ResponseBody>
    }
}