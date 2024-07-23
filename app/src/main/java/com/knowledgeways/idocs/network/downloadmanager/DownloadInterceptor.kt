package com.knowledgeways.idocs.network.downloadmanager

import java.io.IOException

import okhttp3.Interceptor
import okhttp3.Response


class DownloadInterceptor(private val listener: DownloadListener) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain) : Response{
        val originalResponse: Response = chain.proceed(chain.request())


        return originalResponse.newBuilder()
            .body(DownloadResponseBody(originalResponse.body(), listener))
            .build()
    }
}
