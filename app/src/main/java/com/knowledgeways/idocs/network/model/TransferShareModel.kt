package com.knowledgeways.idocs.network.model

data class TransferShareModel(
    val documentId: String, val organizationId: String, val actionIds: ArrayList<String>,
    val priorityLevelId: String, val description: String, val recipientType: String,
)