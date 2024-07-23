package com.knowledgeways.idocs.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
//import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import java.io.IOException
import java.util.*

class LoggingInterceptor {/* : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        request = request.newBuilder()
            .header("Accept", "application/json")
            .method(request.method, request.body).build()
        val t1 = System.nanoTime()
        val requestLog = String.format(
            Locale.US, "request to %s \n %s",
            request.url, requestBodyToString(request)
        )

        Log.d("AppLogger", "Request Log -->  ${requestLog}")

        var response = chain.proceed(request)

        while (!response.isSuccessful) {
            // retry the request
            response.close()
            response = chain.proceed(request)
        }

        val t2 = System.nanoTime()
        val responseLog = String.format(
            Locale.US, "Received response for %s in %.1fms%n%s",
            response.request.url, (t2 - t1) / 1e6, response.headers
        )

        Log.d("AppLogger", "Response Log  -->   $responseLog")
        val bodyString = response.body!!.string()

        Log.d("AppLogger","Response Body -->  $bodyString")

        return response.newBuilder()
            .body(bodyString.toResponseBody(response.body!!.contentType()))
            .build()
    }

    private fun requestBodyToString(request: Request): String? {
        return try {
            val copy = request.newBuilder().build()
            val buffer = Buffer()
            if (copy.body != null) {
                copy.body!!.writeTo(buffer)
            }
            buffer.readUtf8()
        } catch (e: IOException) {
            "Request Exception"
        }
    }
    */
}