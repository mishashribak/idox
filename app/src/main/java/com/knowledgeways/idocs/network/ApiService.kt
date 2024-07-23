package com.knowledgeways.idocs.network

import com.google.gson.JsonObject
import com.knowledgeways.idocs.network.model.*
import com.knowledgeways.idocs.network.model.documentdetail.DocumentDetail
import com.knowledgeways.idocs.network.model.theme.Theme
import com.knowledgeways.idocs.network.model.user.Organization
import com.knowledgeways.idocs.network.model.user.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*
import java.security.cert.CertPathValidatorException.Reason

interface ApiService {
    @GET("theme")
    suspend fun getThemeList(): List<Theme>

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("username") userName: String,
        @Field("password") password: String,
    ): Response<User>

    @FormUrlEncoded
    @POST("switchOrganization")
    suspend fun switchOrganization(
        @Header("cookie") cookieString: String,
        @Field("organizationId") organizationId: String,
    ): User

    @Streaming
    @POST("theme/{themeId}")
    suspend fun downloadThemeImages(
        @Path("themeId") themeId: String,
    ): Response<ResponseBody>


    @Streaming
    @POST("theme/{themeId}/icons")
    suspend fun downloadThemeIcons(
        @Path("themeId") themeId: String,
    ): Response<ResponseBody>

    @GET("boxes")
    suspend fun getBoxes(
        @Header("cookie") cookieString: String,
        @Query("themeId") themeId: String,
    ): Response<List<Box>>

    @GET("logout")
    suspend fun logout(
        @Header("cookie") cookieString: String
    ) : String

    @GET("boxes/documentTotal")
    suspend fun getDocumentTotal(
        @Header("cookie") cookieString: String
    ): JsonObject

    @GET("organizations")
    suspend fun getOrganizations(
        @Header("cookie") cookieString: String
    ): Response<List<Organization>>

    @GET("externalOrganizations")
    suspend fun getExternalOrganizations(
        @Header("cookie") cookieString: String,
        @Query("syncRequest") syncRequest: Boolean,
    ): Response<List<Organization>>

    @GET("users")
    suspend fun getExternalUsers(
        @Header("cookie") cookieString: String
    ): Response<List<ExternalUser>>

    @GET("actions")
    suspend fun getActions(
        @Header("cookie") cookieString: String
    ): Response<List<Action>>

    @GET("priorities")
    suspend fun getPriorities(
        @Header("cookie") cookieString: String
    ): Response<List<Action>>

    @GET("confidential")
    suspend fun getConfidential(
        @Header("cookie") cookieString: String
    ): Response<List<Action>>

    @GET("categories")
    suspend fun getCategories(
        @Header("cookie") cookieString: String
    ): Response<List<Category>>

    @GET("categories")
    suspend fun getTransferableOrganizations(
        @Header("cookie") cookieString: String,
        @Query("transferable") transferable: String
    ): Response<List<Organization>>

    @GET("categories")
    suspend fun getMixedOrganization(
        @Header("cookie") cookieString: String,
        @Query("mixed") mixed: String
    ): Response<List<Organization>>

    @GET("boxes/{boxId}/documents")
    suspend fun getDocumentList(
        @Header("cookie") cookieString: String,
        @Path("boxId") boxId: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): List<Document>

    @GET("search")
    suspend fun searchDocument(
        @Header("cookie") cookieString: String,
        @Query("subject") subject: String?= null,
        @Query("category") category: String?= null,
        @Query("referenceNumber") referenceNumber: String? = null,
        @Query("organization") organization: String? = null,
        @Query("externalOrganization") externalOrganization: String? = null,
        @Query("dateFrom") dateFrom: String? = null,
        @Query("dateTo") dateTo: String? = null,
        @Query("size") size: Int,
        @Query("page") page: Int,
        @Query("dateFromHijri") dateFromHijri: String? = null,
        @Query("dateToHijri") dateToHijri: String? = null,
    ): List<Document>

    @GET("boxes/{boxId}/documents/{documentId}/{transferId}")
    suspend fun getDocumentDetail(
        @Header("cookie") cookieString: String,
        @Path("boxId") boxId: String,
        @Path("documentId") documentId: String,
        @Path("transferId") transferId: String,
    ) : Response<DocumentDetail>

    @PUT("otp")
    suspend fun verifyOTP(
        @Header("cookie") cookieString: String,
        @Query("otp") otp: String
    ): Response<DocumentDetail>

    @GET("boxes/{boxId}/documents/{documentId}/{transferId}/file")
    suspend fun downloadPDFFile(
        @Header("cookie") cookieString: String,
        @Path("boxId") boxId: String,
        @Path("documentId") documentId: String,
        @Path("transferId") transferId: String,
    ): Response<ResponseBody>

    @GET("boxes/{boxId}/documents/{documentId}/attachments/{attachmentId}/file")
    suspend fun downloadAttachedPDFFile(
        @Header("cookie") cookieString: String,
        @Path("boxId") boxId: String,
        @Path("documentId") documentId: String,
        @Path("attachmentId") transferId: String,
    ): Response<ResponseBody>

    @GET("boxes/{boxId}/documents/{documentId}/metadata")
    suspend fun getPDFMetaData(
        @Header("cookie") cookieString: String,
        @Path("boxId") boxId: String,
        @Path("documentId") documentId: String,
    ): PDFDetails

    @GET("boxes/{boxId}/documents/{documentId}/tracking")
    suspend fun getPDFHistory(
        @Header("cookie") cookieString: String,
        @Path("boxId") boxId: String,
        @Path("documentId") documentId: String,
    ): List<PDFHistory>

    @GET("boxes/{boxId}/documents/{documentId}/links")
    suspend fun getLinks(
        @Header("cookie") cookieString: String,
        @Path("boxId") boxId: String,
        @Path("documentId") documentId: String,
    ): List<Link>

    @GET("boxes/{boxId}/documents/{documentId}/attachments")
    suspend fun getAttachmentList(
        @Header("cookie") cookieString: String,
        @Path("boxId") boxId: String,
        @Path("documentId") documentId: String,
    ): List<Link>

    @GET("boxes/{boxId}/documents/{documentId}/metadata")
    suspend fun getLinkedPDFMetaData(
        @Header("cookie") cookieString: String,
        @Path("boxId") boxId: String,
        @Path("documentId") documentId: String,
        @Query("rootLinkedId") rootLinkId: String
    ): PDFDetails

    @Multipart
    @POST("boxes/{boxId}/documents/{docId}/reply")
    suspend fun replyPDF(
        @Header("cookie") cookieString: String,
        @Path("boxId") boxId: String,
        @Path("docId") documentId: String,
        @Part transferJson: RequestBody?,
        @Part annotatedFile: MultipartBody.Part?
    )

    @Multipart
    @POST("boxes/{boxId}/documents/{documentId}/publish")
    suspend fun publishPDF(
        @Header("cookie") cookieString: String,
        @Path("boxId") boxId: String,
        @Path("documentId") documentId: String,
        @Part("transferId") transferId: RequestBody?,
        @Part annotatedFile: MultipartBody.Part?
    ): Response<Any>

    @Multipart
    @POST("boxes/{boxId}/documents/{documentId}/sendDCC")
    suspend fun sendDCC(
        @Header("cookie") cookieString: String,
        @Path("boxId") boxId: String,
        @Path("documentId") documentId: String,
        @Part("transferId") transferId: RequestBody?,
        @Part annotatedFile: MultipartBody.Part?
    ): Response<Any>

    @Multipart
    @POST("boxes/{boxId}/documents/{documentId}/sendDCC")
    suspend fun reject(
        @Header("cookie") cookieString: String,
        @Path("boxId") boxId: String,
        @Path("documentId") documentId: String,
        @Part("reason") transferId: Reason?,
        @Part annotatedFile: MultipartBody.Part?
    ): Response<Any>

    @Multipart
    @POST("boxes/{boxId}/documents/{documentId}/sign/approve")
    suspend fun signApprove(
        @Header("cookie") cookieString: String,
        @Path("boxId") boxId: String,
        @Path("documentId") documentId: String,
        @Part annotatedFile: MultipartBody.Part?
    ): Response<Any>

    @Multipart
    @POST("boxes/{boxId}/documents/{documentId}/archive")
    suspend fun archive(
        @Header("cookie") cookieString: String,
        @Path("boxId") boxId: String,
        @Path("documentId") documentId: String,
        @Part annotatedFile: MultipartBody.Part?
    ): ResponseBody

    @GET("organizations/favorite")
    suspend fun getFavoriteOrganizations(
        @Header("cookie") cookieString: String
    ): Response<List<Organization>>

    @GET("users/favorite")
    suspend fun getFavoriteExternalUsers(
        @Header("cookie") cookieString: String
    ): Response<List<ExternalUser>>

    @GET("externalOrganizations/favorite")
    suspend fun getFavoriteExternalOrg(
        @Header("cookie") cookieString: String
    ): Response<List<Organization>>

    @POST("organizations/favorite")
    suspend fun addOrgToFavorite(
        @Header("cookie") cookieString: String,
        @Body favoriteRequest: OrgFavoriteRequest
    ): ResponseBody

    @HTTP(method = "DELETE", path = "/kw-ecm/idox/organizations/favorite", hasBody = true)
    suspend fun deleteFromFavorite(
        @Header("cookie") cookieString: String,
        @Body favoriteRequest: OrgFavoriteRequest
    ): ResponseBody

    @POST("externalOrganizations/favorite")
    suspend fun addSisterOrgToFavorite(
        @Header("cookie") cookieString: String,
        @Body favoriteRequest: SisterOrgFavoriteRequest
    ): ResponseBody

    @HTTP(method = "DELETE", path = "/kw-ecm/idox/externalOrganizations/favorite", hasBody = true)
    suspend fun deleteSisterOrgFromFavorite(
        @Header("cookie") cookieString: String,
        @Body favoriteRequest: SisterOrgFavoriteRequest
    ): ResponseBody

    @POST("/users/favorite")
    suspend fun addUserToFavorite(
        @Header("cookie") cookieString: String,
        @Body favoriteRequest: UserFavoriteRequest
    ): ResponseBody

    @HTTP(method = "DELETE", path = "/kw-ecm/idox/users/favorite", hasBody = true)
    suspend fun deleteUserFromFavorite(
        @Header("cookie") cookieString: String,
        @Body favoriteRequest: UserFavoriteRequest
    ): ResponseBody

    @Multipart
    @POST("boxes/{boxId}/documents/{documentId}/send")
    suspend fun sendDocument(
        @Header("cookie") cookieString: String,
        @Path("boxId") boxId: String,
        @Path("documentId") documentId: String,
        @Part("parentTransferId") parentTransferId: RequestBody,
        @Part("transfers") transfers: RequestBody,
        @Part("externalTransfers") externalTransfers: RequestBody?,
        @Part annotatedFile: MultipartBody.Part?
    ): ResponseBody

    @Multipart
    @POST("boxes/{boxId}/documents/{documentId}/unarchive")
    suspend fun unArchive(
        @Header("cookie") cookieString: String,
        @Path("boxId") boxId: String,
        @Path("documentId") documentId: String,
        @Part("transferId") parentTransferId: RequestBody,
        @Part annotatedFile: MultipartBody.Part?
    ): ResponseBody

    @Multipart
    @POST("boxes/{boxId}/documents/{documentId}/shareFile")
    suspend fun shareDocument(
        @Header("cookie") cookieString: String,
        @Path("boxId") boxId: String,
        @Path("documentId") documentId: String,
        @Part("transferId") parentTransferId: RequestBody,
        @Part annotatedFile: MultipartBody.Part?
    ): ResponseBody

    @Multipart
    @POST("log/documents/{documentId}/shareFile")
    suspend fun shareMainPDF(
        @Header("cookie") cookieString: String,
        @Path("documentId") documentId: String,
        @Part annotatedFile: MultipartBody.Part?
    ): ResponseBody

    @Multipart
    @POST("log/documents/{documentId}/shareFile")
    suspend fun shareAttachedPDF(
        @Header("cookie") cookieString: String,
        @Path("documentId") documentId: String,
        @Path("attachmentId") attachId: String,
        @Part annotatedFile: MultipartBody.Part?
    ): ResponseBody

    @Multipart
    @PUT("boxes/{boxId}/documents/{documentId}/followup")
    suspend fun followUp(
        @Header("cookie") cookieString: String,
        @Path("boxId") boxId: String,
        @Path("documentId") documentId: String,
        @Part annotatedFile: MultipartBody.Part?
    ): ResponseBody


    @Multipart
    @PUT("boxes/{boxId}/documents/{documentId}/favorite")
    suspend fun putFavorite(
        @Header("cookie") cookieString: String,
        @Path("boxId") boxId: String,
        @Path("documentId") documentId: String,
        @Part annotatedFile: MultipartBody.Part?
    ): ResponseBody

    @Multipart
    @PUT("boxes/{boxId}/documents/{documentId}/unread")
    suspend fun markUnread(
        @Header("cookie") cookieString: String,
        @Path("boxId") boxId: String,
        @Path("documentId") documentId: String,
        @Part annotatedFile: MultipartBody.Part?
    ): ResponseBody

    @Multipart
    @PUT("boxes/{boxId}/documents/{documentId}/delete")
    suspend fun documentDelete(
        @Header("cookie") cookieString: String,
        @Path("boxId") boxId: String,
        @Path("documentId") documentId: String,
        @Part annotatedFile: MultipartBody.Part?
    ): ResponseBody

    @Multipart
    @PUT("boxes/{boxId}/documents/{documentId}/trash")
    suspend fun documentTrash(
        @Header("cookie") cookieString: String,
        @Path("boxId") boxId: String,
        @Path("documentId") documentId: String,
        @Part annotatedFile: MultipartBody.Part?
    ): ResponseBody

    @Multipart
    @PUT("boxes/{boxId}/documents/{documentId}/untrash")
    suspend fun documentUnTrash(
        @Header("cookie") cookieString: String,
        @Path("boxId") boxId: String,
        @Path("documentId") documentId: String,
        @Part annotatedFile: MultipartBody.Part?
    ): ResponseBody

    @POST("INTERNAL_OUTGOING")
    suspend fun internalOutgoing(
        @Header("cookie") cookieString: String,
    ): OutgoingResponse

    @POST("EXTERNAL_OUTGOING")
    suspend fun externalOutgoing(
        @Header("cookie") cookieString: String,
    ): OutgoingResponse

    @GET("signOrganizations")
    suspend fun getSignOrganizations(
        @Header("cookie") cookieString: String,
    ):  Response<List<Organization>>

    @GET("publishOrganizations")
    suspend fun getPublishedOrganizations(
        @Header("cookie") cookieString: String,
        @Query("parentOrganizationId") parentOrganizationId: Int,
        @Query("searchTerm") searchTerm: String,
        @Query("excludeTargetOrganization") excludeTargetOrganization: Boolean,
        @Query("syncRequest") syncRequest: Boolean,
    ):  Response<List<Organization>>

    @GET("externalOrganizations")
    suspend fun getExternalOrganizations(
        @Header("cookie") cookieString: String,
        @Query("searchTerm") searchTerm: String,
        @Query("syncRequest") syncRequest: Boolean,
    ):  Response<List<Organization>>

    @GET("publishUsers")
    suspend fun getPublishUsers(
        @Header("cookie") cookieString: String,
        @Query("parentOrganizationId") parentOrganizationId: Int,
        @Query("searchTerm") searchTerm: String,
    ):  Response<List<ExternalUser>>

    @GET("search")
    suspend fun searchDocument(
        @Header("cookie") cookieString: String,
        @Query("status") status: String,
        @Query("searchTerm") searchTerm: String,
    ): List<Document>
}