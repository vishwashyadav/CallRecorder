package com.callrecorder

import com.callrecorder.bean.CallDetailsListResponseBean
import com.callrecorder.bean.UploadResponseBean
import com.callrecorder.login.LoginResponseBean
import com.google.gson.JsonObject
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.ConnectionPool
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit


interface WebServiceProvider {

    companion object {

        private const val BASE_URL = "http://165.22.217.248:8081/" //production
       // private const val BASE_URL = "http://192.168.0.104:8081/" //local

        private val okHttpClientForMedia = OkHttpClient.Builder()
                .readTimeout(120, TimeUnit.SECONDS)
                .connectTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .connectionPool(ConnectionPool(3, 120, TimeUnit.SECONDS))
                .retryOnConnectionFailure(true)
                .addInterceptor(HttpLoggingInterceptor().apply { level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE })
                .build()

        private val okHttpClient = OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .connectionPool(ConnectionPool(3, 60, TimeUnit.SECONDS))
                .retryOnConnectionFailure(true)
                .addInterceptor(HttpLoggingInterceptor().apply { level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE })
                .build()

        var retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addCallAdapterFactory(
                        RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        var retrofitForMedia = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClientForMedia)
                .addCallAdapterFactory(
                        RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .addConverterFactory(GsonConverterFactory.create())
                .build()

    }


    @POST("calltracker/register")
    fun login(@Body data: JsonObject): Single<LoginResponseBean>


    @Multipart
    @POST("calltracker/upload")
    fun uploadImage(@Part file: MultipartBody.Part
                    , @Part("fileName") fileName: RequestBody): Single<UploadResponseBean>


    @POST("calltracker/update-upload-details")
    fun updateDetails(@Body data: JsonObject): Single<LoginResponseBean>


    @GET("calltracker//view-user-records/{userId}")
    fun getCallDetails(@Path("userId") userId: String): Single<CallDetailsListResponseBean>


    @POST("calltracker/delete")
    fun deleteRecord(@Body data: JsonObject): Single<LoginResponseBean>


}

