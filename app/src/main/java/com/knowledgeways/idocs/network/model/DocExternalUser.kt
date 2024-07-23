package com.knowledgeways.idocs.network.model


data class DocExternalUser(
    val docId: String,
    var orgData: List<ExternalUser>
)
