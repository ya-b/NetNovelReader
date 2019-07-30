package com.netnovelreader.repo.http

import com.netnovelreader.repo.db.BookInfoEntity
import com.netnovelreader.repo.db.SiteSelectorEntity
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

object WebService {
    val TIMEOUT = 3L
    lateinit var readerAPI: NovalReaderAPI
    val searchBook by lazy { SearchBook() }
    val bookLinkRanking by lazy { BookLinkRanking() }

    fun init(logger: HttpLoggingInterceptor.Logger?) {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
            .addNetworkInterceptor(
                HttpLoggingInterceptor(
                    logger ?: HttpLoggingInterceptor.Logger.DEFAULT
//                    object : HttpLoggingInterceptor.Logger {
//                        override fun log(message: String) {
//                        }
//                    }
                ).apply { this.level = HttpLoggingInterceptor.Level.BASIC }
            )
            .build()
        readerAPI = Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("http://139.159.226.67/reader/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(NovalReaderAPI::class.java)

    }

    interface NovalReaderAPI {
        @GET
        fun getSiteSelectorList(@Url url: String = "rule/query"): Single<List<SiteSelectorEntity>>

        @POST("login")
        @FormUrlEncoded
        fun login(@Field("username") username: String, @Field("password") password: String): Single<ResponseBody>

        @POST("record/restore")
        fun restoreRecord(@Header("Authorization") token: String): Single<ArrayList<BookInfoEntity>>

        @POST("record/save")
        @Multipart
        fun saveRecord(@Header("Authorization") token: String, @Part file: MultipartBody.Part): Single<ResponseBody>

        @GET
        fun request(@Url url: String): Single<Response<ResponseBody>>
    }
}