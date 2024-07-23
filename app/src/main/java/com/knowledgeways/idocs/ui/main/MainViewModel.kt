package com.knowledgeways.idocs.ui.main

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.knowledgeways.idocs.base.BaseViewModel
import com.knowledgeways.idocs.db.PreferenceManager
import com.knowledgeways.idocs.network.ResultWrapper
import com.knowledgeways.idocs.network.client.DownloadAPIClient
import com.knowledgeways.idocs.network.downloadmanager.DownloadListener
import com.knowledgeways.idocs.network.model.*
import com.knowledgeways.idocs.network.model.documentdetail.DocumentDetail
import com.knowledgeways.idocs.network.model.user.Organization
import com.knowledgeways.idocs.network.repository.DownloadRepository
import com.knowledgeways.idocs.utils.AppConstants
import com.knowledgeways.idocs.utils.FileUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class MainViewModel : BaseViewModel() {
    private var _boxLiveData = MutableLiveData<List<Box>>()
    val boxLiveData: LiveData<List<Box>>
        get() = _boxLiveData

    private var _logoutResult = MutableLiveData<String>("")
    val logoutResult: LiveData<String>
        get() = _logoutResult

    private var _documentTotalLiveData = MutableLiveData<JsonObject>()
    val documentTotalLiveData: LiveData<JsonObject> get() = _documentTotalLiveData

    lateinit var downloadRepository: DownloadRepository

    var fileDownloadStatus = DownloadStatus(-1, 0, false)

    private var _progressLiveData = MutableLiveData<DownloadStatus>()
    var progressLiveData: LiveData<DownloadStatus> = _progressLiveData

    private var _documentList = MutableLiveData<ResultWrapper<List<Document>>>()
    val documentList: LiveData<ResultWrapper<List<Document>>>
        get() = _documentList

    private var _documentDetail = MutableLiveData<ResultWrapper<DocumentDetail>>()
    var documentDetail: LiveData<ResultWrapper<DocumentDetail>> = _documentDetail

    var pageNumber = 0
    var searchPageNumber = 0

    private var _selectedBox = MutableLiveData<Box>()
    val selectedBox = _selectedBox

    var _isThemeChanged = MutableLiveData<Int>()
    var isThemeChanged: LiveData<Int> = _isThemeChanged

    var selectedExternalOrganization: Organization? = null
    var selectedInternalOrganization: Organization? = null
    var selectedCategory: Category? = null
    var _syncResultLiveData = MutableLiveData<Boolean>()
    val syncResultLiveData: LiveData<Boolean>
        get() = _syncResultLiveData

    private var _searchResult = MutableLiveData<ResultWrapper<List<Document>>>()
    val searchResult: LiveData<ResultWrapper<List<Document>>>
        get() = _searchResult

    var searchKey = ""
    var referenceNumber = ""
    var dateFrom = ""
    var dateTo = ""
    var dateFromHijri = ""
    var dateToHijri = ""
    var documentOTP = ""
    var downloadingFile = 0 // 0 - File or Image, 1 - PDF

    var _isTempFilesChanged = MutableLiveData<Int>(0)
    var isTempFilesChanged: LiveData<Int> = _isTempFilesChanged

    private var _internalOutgoingLiveData = MutableLiveData<ResultWrapper<OutgoingResponse>>()
    var internalOutgoingLiveData: LiveData<ResultWrapper<OutgoingResponse>> = _internalOutgoingLiveData

    private var _externalOutgoingLiveData = MutableLiveData<ResultWrapper<OutgoingResponse>>()
    var externalOutgoingLiveData: LiveData<ResultWrapper<OutgoingResponse>> = _externalOutgoingLiveData

    init {
        getSyncData(false)
    }

    fun initViewModel(context: Context) {
        DownloadAPIClient.init(context, object : DownloadListener {
            override fun onFinish(file: File?) {

            }

            override fun onProgress(progress: Int, downloadedLengthKb: Long, totalLengthKb: Long) {
                setProgress(progress)
            }

            override fun onFailed(errMsg: String?) {

            }
        })

        downloadRepository = DownloadRepository(DownloadAPIClient.apiService)
    }

    fun setFileType(fileType: Int) {
        fileDownloadStatus.fileType = fileType
        _progressLiveData.postValue(fileDownloadStatus)
    }

    private fun setProgress(progress: Int) {
        fileDownloadStatus.progress = progress
        _progressLiveData.postValue(fileDownloadStatus)
    }

    private fun setDownloadFinished() {
        fileDownloadStatus.unzipFinished = true
        _progressLiveData.postValue(fileDownloadStatus)
    }

    fun getSyncData(isFromUser: Boolean) {
        _syncResultLiveData.postValue(false)
        CoroutineScope(Dispatchers.IO).launch {
            val boxResult = withContext(Dispatchers.Default) {
                repository.getBoxes(getDefaultTheme()?.themeId ?: "")
            }

            val internalOrganizationResult =
                if (isFromUser || PreferenceManager.syncData?.needSyncInternalOrganizations == true) withContext(
                    Dispatchers.Default
                ) {
                    repository.getOrganization()
                } else null

            val externalOrganizationResult =
                if (isFromUser || PreferenceManager.syncData?.needSyncExternalOrganizations == true)
                    withContext(Dispatchers.Default) {
                        repository.getExternalOrganizations(
                            true
                        )
                    }
                else null

            val externalUserResult =
                if (isFromUser || PreferenceManager.syncData?.needSyncOrganizationsUsers == true)
                    withContext(Dispatchers.Default) {
                        repository.getExternalUsers()
                    }
                else null

            val actionResult =
                if (isFromUser || PreferenceManager.syncData?.needSyncActions == true)
                    withContext(Dispatchers.Default) {
                        repository.getActions()
                    }
                else null

            val priorityResult =
                if (isFromUser || PreferenceManager.syncData?.needSyncPriority == true)
                    withContext(Dispatchers.Default) {
                        repository.getPriorities()
                    }
                else null

            val confidentialResult =
                if (isFromUser || PreferenceManager.syncData?.needSyncConfidential == true)
                    withContext(Dispatchers.Default) {
                        repository.getConfidential()
                    }
                else null

            val categoryResult =
                if (isFromUser || PreferenceManager.syncData?.needSyncDocumentCategory == true)
                    withContext(Dispatchers.Default) {
                        repository.getCategories()
                    }
                else null

            val transferableOrganizationResult =
                if (isFromUser || PreferenceManager.syncData?.needSyncTransferableExternalOrganizations == true)
                    withContext(Dispatchers.Default) {
                        repository.getTransferableOrganizations()
                    }
                else null

            val mixedOrganizationResult =
                if (isFromUser || PreferenceManager.syncData?.needSyncOrganizations == true)
                    withContext(Dispatchers.Default) {
                        repository.getMixedOrganization()
                    }
                else null

            if (boxResult.first().isSuccessful
                && (internalOrganizationResult == null || internalOrganizationResult.first().isSuccessful)
                && (externalOrganizationResult == null || externalOrganizationResult.first().isSuccessful)
                && (externalUserResult == null || externalUserResult.first().isSuccessful)
                && (actionResult == null || actionResult.first().isSuccessful)
                && (priorityResult == null || priorityResult.first().isSuccessful)
                && (confidentialResult == null || confidentialResult.first().isSuccessful)
                && (categoryResult == null || categoryResult.first().isSuccessful)
                && (transferableOrganizationResult == null || transferableOrganizationResult.first().isSuccessful)
                && (mixedOrganizationResult == null || mixedOrganizationResult.first().isSuccessful)
            ) {
                if (boxResult.first().isSuccessful) {
                    PreferenceManager.boxes = boxResult.first().body()!!
                    _boxLiveData.postValue(boxResult.first().body()!!)
                    getDocumentTotal()
                }

                if (internalOrganizationResult != null) {
                    PreferenceManager.internalOrganization =
                        internalOrganizationResult.first().body()!!
                }

                if (externalOrganizationResult != null) {
                    PreferenceManager.externalOrganization =
                        externalOrganizationResult.first().body()!!
                }

                if (externalUserResult != null) {
                    PreferenceManager.externalUsers = externalUserResult.first().body()!!
                }

                if (actionResult != null) {
                    PreferenceManager.actions = actionResult.first().body()!!
                }

                if (priorityResult != null) {
                    PreferenceManager.priorities = priorityResult.first().body()!!
                }

                if (confidentialResult != null) {
                    PreferenceManager.confidential = confidentialResult.first().body()!!
                }

                if (categoryResult != null) {
                    PreferenceManager.categories = categoryResult.first().body()!!
                }

                if (transferableOrganizationResult != null) {
                    PreferenceManager.transferableOrganization =
                        transferableOrganizationResult.first().body()!!
                }

                if (mixedOrganizationResult != null) {
                    PreferenceManager.mixedOrganization = mixedOrganizationResult.first().body()!!
                }
                _syncResultLiveData.postValue(true)
            }
        }
    }

    fun getBoxes() {
        viewModelScope.launch {
            repository.getBoxes(getDefaultTheme()?.themeId ?: "").onStart {}
                .catch {
                }
                .collect { response ->
                    if (response.isSuccessful) {
                        PreferenceManager.boxes = response.body()!!
                        _boxLiveData.postValue(response.body()!!)
                        getDocumentTotal()
                    }

                }
        }
    }

    fun getDocumentTotal() {
        viewModelScope.launch {
            repository.getDocumentTotal().onStart { }
                .catch { }
                .collect { response ->
                    PreferenceManager.documentTotal = response
                    _documentTotalLiveData.postValue(response)
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout().onStart { }
                .catch { }
                .collect { response ->
                    PreferenceManager.loggedInStatus = 0
                    _logoutResult.postValue(response)
                }
        }
    }

    fun downloadFileImages(
        mContext: Context,
        defaultThemeId: String,
        fileType: Int,
    ) {
        downloadingFile = 0
        viewModelScope.launch {
            setFileType(fileType)

            val client =
                if (fileType == AppConstants.FILE_TYPE_IMAGE) downloadRepository.downloadThemeImages(
                    defaultThemeId
                )
                else downloadRepository.downloadThemeIcons(defaultThemeId)

            client.onStart { }
                .catch { }
                .collect { response ->
                    // Write the downloaded file into Zip
                    FileUtils.writeResponseBodyToDisk(
                        mContext,
                        response.body()!!,
                        defaultThemeId,
                        fileType
                    ) {
                        downloadFileImages(mContext, defaultThemeId, AppConstants.FILE_TYPE_ICON)
                    }
                    if (fileType == AppConstants.FILE_TYPE_ICON) {
                        PreferenceManager.resourceStatus = 2
                        unzipFile(mContext, defaultThemeId, AppConstants.FILE_TYPE_IMAGE)
                    }
                }
        }
    }

    fun unzipFile(context: Context, themeID: String, fileType: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                setLoading(true)
                FileUtils.unzip(
                    FileUtils.getZipFileName(
                        context, themeID,
                        fileType
                    ), FileUtils.getUnzipFileDirectory(context, themeID, fileType)
                )
                if (fileType == AppConstants.FILE_TYPE_IMAGE)
                    unzipFile(context, themeID, AppConstants.FILE_TYPE_ICON)
                if (fileType == AppConstants.FILE_TYPE_ICON) {
                    setLoading(false)
                    PreferenceManager.resourceStatus = 3
                    setDownloadFinished()
                }
            } catch (e: Exception) {
            }
        }
    }

    fun removeDocuments() {
        viewModelScope.launch(Dispatchers.IO) {
            _documentList.postValue(ResultWrapper.Success(ArrayList()))
        }
    }

    fun getDocumentList(boxId: String, pageNumber: Int) {
        _documentList.postValue(ResultWrapper.Loading(null))
        viewModelScope.launch(Dispatchers.IO) {
            repository.getDocumentList(boxId, pageNumber).onStart { }
                .catch {
                    _documentList.postValue(ResultWrapper.GenericError())
                }
                .collect { response ->
                    _documentList.postValue(ResultWrapper.Success(response))
                }
        }
    }

    fun initializeProgressData() {
        fileDownloadStatus = DownloadStatus(-1, 0, false)
        _progressLiveData.postValue(fileDownloadStatus)
    }

    private fun getSubject(): String? {
        return searchKey.ifEmpty { null }
    }

    private fun getRefNumber(): String? {
        return referenceNumber.ifEmpty { null }
    }

    fun searchDocuments(page: Int) {
        _searchResult.postValue(ResultWrapper.Loading(null))
        viewModelScope.launch(Dispatchers.IO) {
            repository.searchDocumentList(
                getSubject(),
                selectedCategory?.value,
                getRefNumber(),
                selectedInternalOrganization?.id,
                selectedExternalOrganization?.id,
                dateFrom,
                dateTo,
                10,
                page,
                dateFromHijri,
                dateToHijri
            )
                .onStart { }
                .catch { }
                .collect { response ->
                    _searchResult.postValue(ResultWrapper.Success(response))
                }
        }
    }

    fun getDocumentDetail(document: Document) {
        _documentDetail.postValue(ResultWrapper.Loading(null))
        viewModelScope.launch(Dispatchers.IO) {
            when (val response = repository.getDocumentDetail(
                document.boxId ?: "", document.documentId ?: "",
                document.transferId ?: ""
            )) {
                is ResultWrapper.Success -> {

                    if (response.value.body() == null) {
                        val headers = response.value.headers()
                        val headerMapList: Map<String, List<String>> = headers.toMultimap()
                        val otpList = headerMapList["X-otp"]

                        if (otpList != null) {
                            for (otp in otpList) {
                                documentOTP = otp
                            }

                        }
                        _documentDetail.postValue(
                            ResultWrapper.GenericError(
                                201,
                                response.value.errorBody()!!.string()
                            )
                        )

                    } else {
                        _documentDetail.postValue(ResultWrapper.Success(response.value.body()!!))
                    }
                }
                is ResultWrapper.GenericError -> {
                    _documentDetail.postValue(
                        ResultWrapper.GenericError(
                            response.code,
                            response.error
                        )
                    )
                }
                is ResultWrapper.NetworkError -> _documentDetail.postValue(
                    ResultWrapper.GenericError(
                        null,
                        "Network Error!!!"
                    )
                )

                else -> {}
            }
        }
    }

    fun verityOTP(otp: String) {
        _documentDetail.postValue(ResultWrapper.Loading(null))
        viewModelScope.launch(Dispatchers.IO) {
            when (val response = repository.verityOTPForPDF(
                otp
            )) {
                is ResultWrapper.Success -> {
                    if (response.value.body() == null) {
                        _documentDetail.postValue(
                            ResultWrapper.GenericError(
                                202,
                                response.value.errorBody()!!.string()
                            )
                        )
                    } else {
                        _documentDetail.postValue(ResultWrapper.Success(response.value.body()!!))
                    }
                }
                is ResultWrapper.GenericError -> {
                    _documentDetail.postValue(
                        ResultWrapper.GenericError(
                            response.code,
                            response.error
                        )
                    )
                }
                is ResultWrapper.NetworkError -> _documentDetail.postValue(
                    ResultWrapper.GenericError(
                        null,
                        "Network Error!!!"
                    )
                )
                else -> {}
            }
        }
    }

    fun internalOutgoing(){
        _internalOutgoingLiveData.postValue(ResultWrapper.Loading(null))
        viewModelScope.launch(Dispatchers.IO) {
            when (val response = repository.internalOutgoing(
            )) {
                is ResultWrapper.Success -> {
                    _internalOutgoingLiveData.postValue(ResultWrapper.Success(response.value))
                }
                is ResultWrapper.GenericError -> {
                    _internalOutgoingLiveData.postValue(
                        ResultWrapper.GenericError(
                            response.code,
                            response.error
                        )
                    )
                }
                is ResultWrapper.NetworkError -> _internalOutgoingLiveData.postValue(
                    ResultWrapper.GenericError(
                        null,
                        "Network Error!!!"
                    )
                )
                else -> {}
            }
        }
    }

    fun externalOutgoing(){
        _externalOutgoingLiveData.postValue(ResultWrapper.Loading(null))
        viewModelScope.launch(Dispatchers.IO) {
            when (val response = repository.internalOutgoing(
            )) {
                is ResultWrapper.Success -> {
                    _externalOutgoingLiveData.postValue(ResultWrapper.Success(response.value))
                }
                is ResultWrapper.GenericError -> {
                    _externalOutgoingLiveData.postValue(
                        ResultWrapper.GenericError(
                            response.code,
                            response.error
                        )
                    )
                }
                is ResultWrapper.NetworkError -> _externalOutgoingLiveData.postValue(
                    ResultWrapper.GenericError(
                        null,
                        "Network Error!!!"
                    )
                )
                else -> {}
            }
        }
    }

    fun updateTempFile() {
        _isThemeChanged.value = _isTempFilesChanged.value!! + 1
    }
}