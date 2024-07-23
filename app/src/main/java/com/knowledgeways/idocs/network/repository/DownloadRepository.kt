package com.knowledgeways.idocs.network.repository

import com.knowledgeways.idocs.db.PreferenceManager
import com.knowledgeways.idocs.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.ResponseBody
import retrofit2.Response

class DownloadRepository(private val apiService: ApiService) {

    suspend fun downloadThemeImages(defaultThemeId: String): Flow<Response<ResponseBody>> {
        return flow {
            emit(apiService.downloadThemeImages(defaultThemeId))
        }.flowOn(Dispatchers.Main)
    }

    suspend fun downloadThemeIcons(defaultThemeId: String): Flow<Response<ResponseBody>> {
        return flow {
            emit(apiService.downloadThemeIcons(defaultThemeId))
        }.flowOn(Dispatchers.Main)
    }

    suspend fun downloadPDFFile(
        boxId: String,
        documentId: String,
        transferId: String
    ): Flow<Response<ResponseBody>> {
        return flow {
            emit(apiService.downloadPDFFile(
                PreferenceManager.sessionId + PreferenceManager.rememberMe,
                boxId, documentId, transferId))
        }.flowOn(Dispatchers.Main)
    }

    suspend fun downloadAttachment(
        boxId: String,
        documentId: String,
        attachmentId: String
    ): Flow<Response<ResponseBody>> {
        return flow {
            emit(apiService.downloadAttachedPDFFile(
                PreferenceManager.sessionId + PreferenceManager.rememberMe,
                boxId, documentId, attachmentId))
        }.flowOn(Dispatchers.Main)
    }
}