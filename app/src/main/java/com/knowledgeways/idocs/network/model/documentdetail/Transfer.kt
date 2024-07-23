package com.knowledgeways.idocs.network.model.documentdetail

data class Transfer(
    val actionIds: List<Any>,
    val description: String,
    val documentId: String,
    val organizationId: String,
    val priorityLevelId: Any,
    val recipientType: String,
    val replyName: String,
    val transferId: String,
    val userId: String
): java.io.Serializable