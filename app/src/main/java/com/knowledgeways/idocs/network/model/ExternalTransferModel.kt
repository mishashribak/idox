package com.knowledgeways.idocs.network.model

data class ExternalTransferModel(
    val documentId: String, val actionIds: ArrayList<String>,
    val priorityLevelId: String, val description: String, val recipientType: String,
    val externalOrganization: ExternalOrgModel
)