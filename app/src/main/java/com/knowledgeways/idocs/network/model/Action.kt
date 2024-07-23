package com.knowledgeways.idocs.network.model

data class Action(
    val label: String,
    val value: String,
    var selected: Boolean? = false
)