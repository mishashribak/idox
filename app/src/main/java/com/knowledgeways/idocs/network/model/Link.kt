package com.knowledgeways.idocs.network.model

data class Link(
    val destinationDocumentId: String,
    val sourceDocumentId: String,
    val title: String,
    val attachmentId : String?,
    val documentId: String?,
    val fileExtension: String?,
    val category: String?,
    val order: String?
)