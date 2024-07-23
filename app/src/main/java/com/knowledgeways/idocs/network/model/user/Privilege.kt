package com.knowledgeways.idocs.network.model.user

data class Privilege(
    val createDecree: Boolean?,
    val createExternalOutgoing: Boolean?,
    val createInternalOutgoing: Boolean?
)