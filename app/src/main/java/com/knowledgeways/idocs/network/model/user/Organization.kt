package com.knowledgeways.idocs.network.model.user

import com.knowledgeways.idocs.network.model.Action

data class Organization(
    val id: String?,
    val name: String?,
    val order: Int?,
    val type: String?,
    var actionList: ArrayList<Action>? = ArrayList(),
    var priority: Action ?= null,
    var forwardType : Int? = 0, // 0: To 1: CC
    var note: String? = ""
)