package com.knowledgeways.idocs.network


sealed class ResultWrapper<out T> {
    class Loading<T>(val data: T ?= null): ResultWrapper<T>()
    data class Success<out T>(val value: T): ResultWrapper<T>()
    data class GenericError(val code: Int? = null, val error: String? = null): ResultWrapper<Nothing>()
    object NetworkError: ResultWrapper<Nothing>()
}