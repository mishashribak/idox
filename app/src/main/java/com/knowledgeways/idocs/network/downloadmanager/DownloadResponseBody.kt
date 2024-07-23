package com.knowledgeways.idocs.network.downloadmanager

import android.util.Log
import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.*
import java.io.IOException
import java.util.concurrent.Executor


class DownloadResponseBody(
    private val responseBody: ResponseBody?,
    private val downloadListener: DownloadListener?
) :
    ResponseBody() {
    private var bufferedSource: BufferedSource? = null
    private var uiExecutor: Executor?
    override fun contentType(): MediaType? {
        return responseBody?.contentType()
    }

    override fun contentLength(): Long {
        return responseBody?.contentLength() ?: 0L
    }

    override fun source(): BufferedSource {
        if (bufferedSource == null) {
            bufferedSource = source(responseBody?.source()!!).buffer()
        }
        return bufferedSource!!
    }

    private fun source(source: Source): Source {
        return object : ForwardingSource(source) {
            var totalBytesRead = 0L

            @Throws(IOException::class)
            override fun read(sink: Buffer, byteCount: Long): Long {
                val bytesRead = super.read(sink, byteCount)
                // read() returns the number of bytes read, or -1 if this source is exhausted.
                if (null != downloadListener) {
                    totalBytesRead += if (bytesRead != -1L) bytesRead else 0
                    Log.d(
                        "DownloadUtil",
                        "Total Downloaded：" + totalBytesRead + " of：" + responseBody!!.contentLength()
                    )
                    val progress = (totalBytesRead * 100 / responseBody!!.contentLength()).toInt()
                    if (uiExecutor == null) {
                        uiExecutor = MainThreadExecutor()
                    }
                    uiExecutor!!.execute {
                        downloadListener.onProgress(
                            progress,
                            totalBytesRead / 1024,
                            responseBody.contentLength() / 1024
                        )
                    }
                }
                return bytesRead
            }
        }
    }

    init {
        uiExecutor = MainThreadExecutor()
    }
}