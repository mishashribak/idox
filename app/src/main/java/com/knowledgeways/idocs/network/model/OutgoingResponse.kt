package com.knowledgeways.idocs.network.model

data class OutgoingResponse(
    var category: String,
    var confidentialLevelId: String,
    var description: String,
    var descriptionHtml: String,
    var documentId: String,
    var externalTransfers: List<String>,
    var linkType: String,
    var linkedCorrespondenceIds: List<String>,
    var priorityLevelId: String,
    var signOrganizationId: String,
    var subject: String,
    var transfers: List<String>
)