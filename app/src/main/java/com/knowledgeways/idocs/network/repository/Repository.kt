package com.knowledgeways.idocs.network.repository

import com.google.gson.JsonObject
import com.knowledgeways.idocs.db.PreferenceManager
import com.knowledgeways.idocs.network.ApiService
import com.knowledgeways.idocs.network.ResultWrapper
import com.knowledgeways.idocs.network.model.*
import com.knowledgeways.idocs.network.model.documentdetail.DocumentDetail
import com.knowledgeways.idocs.network.model.theme.Theme
import com.knowledgeways.idocs.network.model.user.Organization
import com.knowledgeways.idocs.network.model.user.User
import com.knowledgeways.idocs.utils.AppConstants
import com.knowledgeways.idocs.utils.ConverterUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.File
import java.io.IOException

class Repository(
    private val apiService: ApiService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun switchOrganization(organizationId: String): ResultWrapper<User> {
        return safeApiCall(dispatcher) {
            apiService.switchOrganization(
                PreferenceManager.sessionId + PreferenceManager.rememberMe,
                organizationId
            )
        }
    }

    suspend fun getThemeList(): ResultWrapper<List<Theme>> {
        return safeApiCall(dispatcher) {
            apiService.getThemeList()
        }
    }

    suspend fun login(username: String, password: String): ResultWrapper<Response<User>> {
        return safeApiCall(dispatcher) {
            apiService.login(username, password)
        }
    }

    suspend fun getBoxes(themeID: String): Flow<Response<List<Box>>> {
        return flow {
            emit(
                apiService.getBoxes(
                    PreferenceManager.sessionId + PreferenceManager.rememberMe,
                    themeID
                )
            )
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getDocumentTotal(): Flow<JsonObject> {
        return flow {
            emit(
                apiService.getDocumentTotal(
                    PreferenceManager.sessionId + PreferenceManager.rememberMe
                )
            )
        }.flowOn(Dispatchers.IO)
    }

    suspend fun logout(): Flow<String> {
        return flow {
            emit(
                apiService.logout(
                    PreferenceManager.sessionId + PreferenceManager.rememberMe
                )
            )
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getOrganization(): Flow<Response<List<Organization>>> {
        return flow {
            emit(
                apiService.getOrganizations(
                    PreferenceManager.sessionId + PreferenceManager.rememberMe
                )
            )
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getExternalOrganizations(syncRequest: Boolean): Flow<Response<List<Organization>>> {
        return flow {
            emit(
                apiService.getExternalOrganizations(
                    PreferenceManager.sessionId + PreferenceManager.rememberMe,
                    syncRequest
                )
            )
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getExternalUsers(): Flow<Response<List<ExternalUser>>> {
        return flow {
            emit(
                apiService.getExternalUsers(
                    PreferenceManager.sessionId + PreferenceManager.rememberMe
                )
            )
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getActions(): Flow<Response<List<Action>>> {
        return flow {
            emit(
                apiService.getActions(
                    PreferenceManager.sessionId + PreferenceManager.rememberMe
                )
            )
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getPriorities(): Flow<Response<List<Action>>> {
        return flow {
            emit(
                apiService.getPriorities(
                    PreferenceManager.sessionId + PreferenceManager.rememberMe
                )
            )
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getConfidential(): Flow<Response<List<Action>>> {
        return flow {
            emit(
                apiService.getConfidential(
                    PreferenceManager.sessionId + PreferenceManager.rememberMe
                )
            )
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getCategories(): Flow<Response<List<Category>>> {
        return flow {
            emit(
                apiService.getCategories(
                    PreferenceManager.sessionId + PreferenceManager.rememberMe
                )
            )
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getTransferableOrganizations(): Flow<Response<List<Organization>>> {
        return flow {
            emit(
                apiService.getTransferableOrganizations(
                    PreferenceManager.sessionId + PreferenceManager.rememberMe,
                    "true"
                )
            )
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getMixedOrganization(): Flow<Response<List<Organization>>> {
        return flow {
            emit(
                apiService.getMixedOrganization(
                    PreferenceManager.sessionId + PreferenceManager.rememberMe,
                    "true"
                )
            )
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getDocumentList(boxId: String, pageNumber: Int): Flow<List<Document>> {
        return flow {
            emit(
                apiService.getDocumentList(
                    PreferenceManager.sessionId + PreferenceManager.rememberMe,
                    boxId, pageNumber, AppConstants.DOCUMENT_PAGE_SIZE
                )
            )
        }.flowOn(Dispatchers.IO)
    }

    suspend fun searchDocumentList(
        subject: String?,
        category: String?,
        referenceNumber: String?,
        organization: String?,
        externalOrganization: String?,
        dateFrom: String,
        dateTo: String,
        size: Int,
        page: Int,
        dateFromHijri: String,
        dateToHijri: String,
    ): Flow<List<Document>> {
        return flow {
            emit(
                apiService.searchDocument(
                    PreferenceManager.sessionId + PreferenceManager.rememberMe,
                    subject,
                    category,
                    referenceNumber,
                    organization,
                    externalOrganization,
                    dateFrom,
                    dateTo,
                    size,
                    page,
                    dateFromHijri,
                    dateToHijri
                )
            )
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getDocumentDetail(
        boxId: String,
        documentId: String,
        transferId: String
    ): ResultWrapper<Response<DocumentDetail>> {
        return safeApiCall(dispatcher) {
            apiService.getDocumentDetail(
                PreferenceManager.sessionId + PreferenceManager.rememberMe,
                boxId,
                documentId,
                transferId
            )
        }
    }

    suspend fun verityOTPForPDF(
        otp: String
    ): ResultWrapper<Response<DocumentDetail>> {
        return safeApiCall(dispatcher) {
            apiService.verifyOTP(
                PreferenceManager.sessionId + PreferenceManager.rememberMe,
                otp
            )
        }
    }

    suspend fun getPDFHistory(
        boxId: String,
        documentId: String,
    ): ResultWrapper<List<PDFHistory>> {
        return safeApiCall(dispatcher) {
            apiService.getPDFHistory(
                PreferenceManager.sessionId + PreferenceManager.rememberMe,
                boxId,
                documentId,
            )
        }
    }

    suspend fun getPDFMetaData(
        boxId: String,
        documentId: String,
    ): ResultWrapper<PDFDetails> {
        return safeApiCall(dispatcher) {
            apiService.getPDFMetaData(
                PreferenceManager.sessionId + PreferenceManager.rememberMe,
                boxId,
                documentId
            )
        }
    }

    suspend fun getLinks(
        boxId: String,
        documentId: String,
    ): List<Link> {
        return apiService.getLinks(
            PreferenceManager.sessionId + PreferenceManager.rememberMe,
            boxId,
            documentId
        )
    }

    suspend fun getAttachments(
        boxId: String,
        documentId: String,
    ): List<Link> {
        return apiService.getAttachmentList(
            PreferenceManager.sessionId + PreferenceManager.rememberMe,
            boxId,
            documentId
        )
    }

    suspend fun getLinkedPDFMetaData(
        boxId: String,
        documentId: String,
        rootLinkId: String,
    ): ResultWrapper<PDFDetails> {
        return safeApiCall(dispatcher) {
            apiService.getLinkedPDFMetaData(
                PreferenceManager.sessionId + PreferenceManager.rememberMe,
                boxId,
                documentId,
                rootLinkId
            )
        }
    }

    suspend fun publishDoc(
        boxId: String,
        documentId: String,
        transferId: RequestBody,
        attachment: MultipartBody.Part?,
    ): ResultWrapper<Any> {
        return safeApiCall(Dispatchers.Main) {
            apiService.publishPDF(
                PreferenceManager.sessionId + PreferenceManager.rememberMe,
                boxId, documentId, transferId, attachment
            )
        }
    }

    suspend fun sendDCC(
        boxId: String,
        documentId: String,
        transferId: RequestBody,
        attachment: MultipartBody.Part?,
    ): ResultWrapper<Any> {
        return safeApiCall(Dispatchers.Main) {
            apiService.sendDCC(
                PreferenceManager.sessionId + PreferenceManager.rememberMe,
                boxId, documentId, transferId, attachment
            )
        }
    }

    suspend fun replyPDF(
        boxId: String,
        documentId: String,
        transferJson: RequestBody,
        attachment: MultipartBody.Part?,
    ): ResultWrapper<Any> {
        return safeApiCall(Dispatchers.Main) {
            apiService.replyPDF(
                PreferenceManager.sessionId + PreferenceManager.rememberMe,
                boxId, documentId, transferJson, attachment
            )
        }
    }

    suspend fun signApprove(
        boxId: String,
        documentId: String,
        attachment: MultipartBody.Part?,
    ): ResultWrapper<Any> {
        return safeApiCall(Dispatchers.Main) {
            apiService.signApprove(
                PreferenceManager.sessionId + PreferenceManager.rememberMe,
                boxId, documentId, attachment
            )
        }
    }

    suspend fun archive(
        boxId: String,
        documentId: String,
        attachment: MultipartBody.Part?,
    ): ResultWrapper<ResponseBody> {
        return safeApiCall(Dispatchers.Main) {
            apiService.archive(
                PreferenceManager.sessionId + PreferenceManager.rememberMe,
                boxId, documentId, attachment
            )
        }
    }

    suspend fun getFavoriteOrg(): Flow<Response<List<Organization>>> {
        return flow {
            emit(
                apiService.getFavoriteOrganizations(
                    PreferenceManager.sessionId + PreferenceManager.rememberMe
                )
            )
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getFavoriteExternalOrg(): Flow<Response<List<Organization>>> {
        return flow {
            emit(
                apiService.getFavoriteExternalOrg(
                    PreferenceManager.sessionId + PreferenceManager.rememberMe
                )
            )
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getFavoriteExternalUsers(): Flow<Response<List<ExternalUser>>> {
        return flow {
            emit(
                apiService.getFavoriteExternalUsers(
                    PreferenceManager.sessionId + PreferenceManager.rememberMe
                )
            )
        }.flowOn(Dispatchers.IO)
    }

    suspend fun addOrgToFavorite(
        organizationId: String,
        orgName: String,
        parentId: String,
        order: Int
    ): ResultWrapper<ResponseBody> {
        return safeApiCall(dispatcher) {
            apiService.addOrgToFavorite(
                PreferenceManager.sessionId + PreferenceManager.rememberMe,
                OrgFavoriteRequest(organizationId, orgName, parentId, order)
            )

        }
    }

    suspend fun deleteOrgFromFavorite(
        organizationId: String,
        orgName: String,
        parentId: String,
        order: Int
    ): ResultWrapper<ResponseBody> {
        return safeApiCall(dispatcher) {
                apiService.deleteFromFavorite(
                    PreferenceManager.sessionId + PreferenceManager.rememberMe,
                    OrgFavoriteRequest(organizationId, orgName, parentId, order)
                )

        }
    }

    suspend fun addSisterOrgToFavorite(
        organizationId: String,
        orgName: String,
        order: Int,
        type: String
    ):ResultWrapper<ResponseBody>  {
        return safeApiCall(dispatcher) {
                apiService.addSisterOrgToFavorite(
                    PreferenceManager.sessionId + PreferenceManager.rememberMe,
                    SisterOrgFavoriteRequest(organizationId, orgName, order, type)
                )
        }
    }

    suspend fun deleteSisterOrgFromFavorite(
        organizationId: String,
        orgName: String,
        order: Int,
        type: String
    ) :ResultWrapper<ResponseBody>  {
        return safeApiCall(dispatcher) {
                apiService.deleteSisterOrgFromFavorite(
                    PreferenceManager.sessionId + PreferenceManager.rememberMe,
                    SisterOrgFavoriteRequest(organizationId, orgName, order, type)
                )
        }
    }

    suspend fun addUserToFavorite(
        userId: String,
        fullName: String,
        organizationId: String,
        orgName: String
    ) :ResultWrapper<ResponseBody>  {
        return safeApiCall(dispatcher) {
                apiService.addUserToFavorite(
                    PreferenceManager.sessionId + PreferenceManager.rememberMe,
                    UserFavoriteRequest(userId, fullName, organizationId, orgName)
                )
        }
    }

    suspend fun deleteUserFromFavorite(
        userId: String,
        fullName: String,
        organizationId: String,
        orgName: String
    ) :ResultWrapper<ResponseBody>  {
        return safeApiCall(dispatcher) {
                apiService.deleteUserFromFavorite(
                    PreferenceManager.sessionId + PreferenceManager.rememberMe,
                    UserFavoriteRequest(userId, fullName, organizationId, orgName)
                )
        }
    }

    suspend fun sendDocument(
        boxId: String,
        documentId: String,
        parentTransferId: RequestBody,
        transfers: RequestBody,
        externalTransfers: RequestBody?,
        attachment: MultipartBody.Part?,
    ):ResultWrapper<ResponseBody>{
        return safeApiCall(dispatcher) {
            apiService.sendDocument(
                PreferenceManager.sessionId + PreferenceManager.rememberMe,
                boxId, documentId,
                parentTransferId, transfers, externalTransfers,attachment
            )
        }
    }

    suspend fun unArchive(
        boxId: String,
        transferId: RequestBody,
        documentId: String,
        attachment: MultipartBody.Part?,
    ): ResultWrapper<ResponseBody> {
        return safeApiCall(Dispatchers.Main) {
            apiService.unArchive(
                PreferenceManager.sessionId + PreferenceManager.rememberMe,
                boxId, documentId, transferId, attachment
            )
        }
    }

    suspend fun shareMainPDF(
        documentId: String,
        attachment: MultipartBody.Part?,
    ): ResultWrapper<ResponseBody> {
        return safeApiCall(Dispatchers.Main) {
            apiService.shareMainPDF(
                PreferenceManager.sessionId + PreferenceManager.rememberMe,
                documentId, attachment
            )
        }
    }

    suspend fun shareAttachedPDF(
        boxId: String,
        documentId: String,
        attachment: MultipartBody.Part?,
    ): ResultWrapper<ResponseBody> {
        return safeApiCall(Dispatchers.Main) {
            apiService.shareAttachedPDF(
                PreferenceManager.sessionId + PreferenceManager.rememberMe,
                boxId ,documentId, attachment
            )
        }
    }

    suspend fun followUpPDF(
        boxId: String,
        documentId: String,
        attachment: MultipartBody.Part?,
    ): ResultWrapper<ResponseBody> {
        return safeApiCall(Dispatchers.Main) {
            apiService.followUp(
                PreferenceManager.sessionId + PreferenceManager.rememberMe,
                boxId, documentId, attachment
            )
        }
    }

    suspend fun putFavorite(
        boxId: String,
        documentId: String,
        attachment: MultipartBody.Part?,
    ): ResultWrapper<ResponseBody> {
        return safeApiCall(Dispatchers.Main) {
            apiService.putFavorite(
                PreferenceManager.sessionId + PreferenceManager.rememberMe,
                boxId, documentId, attachment
            )
        }
    }

    suspend fun unReadPDF(
        boxId: String,
        documentId: String,
        attachment: MultipartBody.Part?,
    ): ResultWrapper<ResponseBody> {
        return safeApiCall(Dispatchers.Main) {
            apiService.markUnread(
                PreferenceManager.sessionId + PreferenceManager.rememberMe,
                boxId, documentId, attachment
            )
        }
    }

    suspend fun documentDelete(
        boxId: String,
        documentId: String,
        attachment: MultipartBody.Part?,
    ): ResultWrapper<ResponseBody> {
        return safeApiCall(Dispatchers.Main) {
            apiService.documentDelete(
                PreferenceManager.sessionId + PreferenceManager.rememberMe,
                boxId, documentId, attachment
            )
        }
    }

    suspend fun trashDocument(
        boxId: String,
        documentId: String,
        attachment: MultipartBody.Part?,
    ): ResultWrapper<ResponseBody> {
        return safeApiCall(Dispatchers.Main) {
            apiService.documentTrash(
                PreferenceManager.sessionId + PreferenceManager.rememberMe,
                boxId, documentId, attachment
            )
        }
    }

    suspend fun unTrashDocument(
        boxId: String,
        documentId: String,
        attachment: MultipartBody.Part?,
    ): ResultWrapper<ResponseBody> {
        return safeApiCall(Dispatchers.Main) {
            apiService.documentUnTrash(
                PreferenceManager.sessionId + PreferenceManager.rememberMe,
                boxId, documentId, attachment
            )
        }
    }

    suspend fun internalOutgoing(
    ): ResultWrapper<OutgoingResponse>{
        return safeApiCall(Dispatchers.Main){
            apiService.internalOutgoing(PreferenceManager.sessionId + PreferenceManager.rememberMe)
        }
    }

    suspend fun externalOutgoing(
    ): ResultWrapper<OutgoingResponse>{
        return safeApiCall(Dispatchers.Main){
            apiService.externalOutgoing(PreferenceManager.sessionId + PreferenceManager.rememberMe)
        }
    }

    suspend fun getSignOrganizations()
             : Flow<Response<List<Organization>>> {
        return flow {
            emit(
                apiService.getSignOrganizations(
                    PreferenceManager.sessionId + PreferenceManager.rememberMe
                )
            )
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getPublishOrganizations(parentOrganizationId: Int, searchTerm: String, excludeTargetOrganization: Boolean, syncRequest: Boolean)
            : Flow<Response<List<Organization>>>{
        return flow {
            emit(
                apiService.getPublishedOrganizations(
                    PreferenceManager.sessionId + PreferenceManager.rememberMe,
                     parentOrganizationId, searchTerm, excludeTargetOrganization, syncRequest
                )
            )
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getExternalOrganizations( searchTerm: String, syncRequest: Boolean)
            : Flow<Response<List<Organization>>>{
        return flow {
            emit(
                apiService.getExternalOrganizations(
                    PreferenceManager.sessionId + PreferenceManager.rememberMe,
                     searchTerm, syncRequest
                )
            )
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getPublishedUsers(parentOrganizationId: Int, searchTerm: String)
            : Flow<Response<List<ExternalUser>>>{
        return flow {
            emit(
                apiService.getPublishUsers(
                    PreferenceManager.sessionId + PreferenceManager.rememberMe,
                    parentOrganizationId, searchTerm
                )
            )
        }.flowOn(Dispatchers.IO)
    }

    suspend fun searchDocumentList(
        status: String,
        searchTerm: String,
    ): Flow<List<Document>> {
        return flow {
            emit(
                apiService.searchDocument(
                    PreferenceManager.sessionId + PreferenceManager.rememberMe,
                    status,
                    searchTerm,
                )
            )
        }.flowOn(Dispatchers.IO)
    }

    private suspend fun <T> safeApiCall(
        dispatcher: CoroutineDispatcher,
        apiCall: suspend () -> T
    ): ResultWrapper<T> {
        return withContext(dispatcher) {
            try {
                ResultWrapper.Success(apiCall.invoke())
            } catch (throwable: Throwable) {
                when (throwable) {
                    is IOException -> ResultWrapper.NetworkError
                    is HttpException -> {
                        val code = throwable.code()
                        val errorResponse = ConverterUtils.convertErrorBody(throwable)
                        ResultWrapper.GenericError(code, errorResponse)
                    }

                    else -> {
                        ResultWrapper.GenericError(null, null)
                    }
                }
            }
        }
    }

}