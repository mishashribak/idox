package com.knowledgeways.idocs.network.model.user

data class DocOrganization(
    val docId: String,
    var orgData: List<Organization>
)
