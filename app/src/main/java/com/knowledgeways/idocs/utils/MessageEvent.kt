package com.knowledgeways.idocs.utils

data class MessageEvent(
    val message: Int,
    val data: Any?
) {
    constructor(message: Int) : this(message, null)
}