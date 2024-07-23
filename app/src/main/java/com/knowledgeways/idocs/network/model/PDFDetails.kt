package com.knowledgeways.idocs.network.model

import com.google.gson.JsonObject

data class PDFDetails(
    val documentId: String?,
    val transferId: String?,
    val boxId: String?,
    val category: String?,
    val status: String?,
    val priorityLabel: String?,
    val priorityValue: String?,
    val confidentialLabel: String?,
    val confidentialValue: String?,
    val properties: JsonObject?
)
