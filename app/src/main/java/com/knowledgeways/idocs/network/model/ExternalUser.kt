package com.knowledgeways.idocs.network.model

data class ExternalUser(
    val fullName: String,
    val organizationId: String,
    val organizationName: String,
    val userId: String,
    var actionList: ArrayList<Action>? = ArrayList(),
    var priorityValue: Action? = null,
    var forwardType : Int? = 0, // 0: To 1: CC
    var note: String? = ""
)