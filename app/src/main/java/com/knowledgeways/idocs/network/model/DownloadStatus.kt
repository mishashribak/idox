package com.knowledgeways.idocs.network.model

data class DownloadStatus(
    var fileType: Int,
    var progress: Int,
    var unzipFinished: Boolean)
