package com.knowledgeways.idocs.network.model.documentdetail

data class Watermark(
    val alpha: Double,
    val fgColor: String,
    val text: String,
    val textSize: Int
): java.io.Serializable