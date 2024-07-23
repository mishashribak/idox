package com.knowledgeways.idocs.network.client

import android.content.Context
import com.google.gson.GsonBuilder
import com.knowledgeways.idocs.db.PreferenceManager
import com.knowledgeways.idocs.network.ApiService
import com.knowledgeways.idocs.network.downloadmanager.DownloadInterceptor
import com.knowledgeways.idocs.network.downloadmanager.DownloadListener
import com.knowledgeways.idocs.utils.AppConstants
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

object DownloadAPIClient {

    const val CACHE_FILE_NAME = "iDOX_CACHE_FILE"
    lateinit var apiService: ApiService

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    fun init(context: Context, listener: DownloadListener){

        // cache
        val cacheFile = File(context.cacheDir, CACHE_FILE_NAME)
        cacheFile.mkdir()
        val cache = Cache(cacheFile, 60 * 1000 * 1000)

        // OKHttp Builder
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val okHttpBuilder = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(interceptor)
            .addInterceptor(DownloadInterceptor(listener))
            .cache(cache)
        okHttpBuilder.interceptors().add(interceptor)


        // OKHttp
        val okHttpClient = okHttpBuilder.build()

        val retrofit = Retrofit.Builder()
            .baseUrl(PreferenceManager.baseUrl)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(okHttpClient)
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }
}