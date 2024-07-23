package com.knowledgeways.idocs.network

import com.knowledgeways.idocs.network.model.ErrorResponse

interface HandleResponse<T> {
    fun handleErrorResponse(error: ErrorResponse)
    fun handleSuccessResponse(response: T)
}