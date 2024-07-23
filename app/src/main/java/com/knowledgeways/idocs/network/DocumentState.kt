package com.knowledgeways.idocs.network

sealed class DocumentState<T> (val data: T? = null , val message: String? = null){
    class Success<T>(data: T?): DocumentState<T>(data)

    class Loading<T>(data: T ?= null): DocumentState<T>(data)

    class Error<T>(message: String?, data: T? = null): DocumentState<T>(data, message)
}
