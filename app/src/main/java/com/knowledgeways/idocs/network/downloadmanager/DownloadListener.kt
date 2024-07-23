package com.knowledgeways.idocs.network.downloadmanager

import java.io.File


interface DownloadListener {
    fun onFinish(file: File?)
    fun onProgress(progress: Int, downloadedLengthKb: Long, totalLengthKb: Long)
    fun onFailed(errMsg: String?)
}
