package com.knowledgeways.idocs.network.model

data class PDFHistory(
    val documentId: String,
    val transferId: String,
    val from: String,
    val to: String,
    val date: String,
    val status: String,
    val action: String,
    val description: String,
    val shortDescription: String,
    val priority: String
)