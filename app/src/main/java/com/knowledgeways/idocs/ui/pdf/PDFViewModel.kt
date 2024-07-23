package com.knowledgeways.idocs.ui.pdf

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.knowledgeways.idocs.base.BaseViewModel
import com.knowledgeways.idocs.db.PreferenceManager
import com.knowledgeways.idocs.network.ResultWrapper
import com.knowledgeways.idocs.network.client.DownloadAPIClient
import com.knowledgeways.idocs.network.downloadmanager.DownloadListener
import com.knowledgeways.idocs.network.model.*
import com.knowledgeways.idocs.network.model.documentdetail.DocumentDetail
import com.knowledgeways.idocs.network.model.user.DocOrganization
import com.knowledgeways.idocs.network.model.user.Organization
import com.knowledgeways.idocs.network.repository.DownloadRepository
import com.knowledgeways.idocs.utils.AppConstants
import com.knowledgeways.idocs.utils.AppConstants.MESSAGE_ADD_SUCCESS
import com.knowledgeways.idocs.utils.AppConstants.MESSAGE_DELETE_SUCCESS
import com.knowledgeways.idocs.utils.AppConstants.TYPE_INTERNAL
import com.knowledgeways.idocs.utils.ConverterUtils
import com.knowledgeways.idocs.utils.FileUtils
import com.knowledgeways.idocs.utils.PopupUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class PDFViewModel : BaseViewModel() {

    lateinit var downloadRepository: DownloadRepository
    var fileDownloadStatus = DownloadStatus(-1, 0, false)

    private var _progressLiveData = MutableLiveData<DownloadStatus>()
    var progressLiveData: LiveData<DownloadStatus> = _progressLiveData
    var downloadingFile = 0 // 0 - File or Image, 1 - PDF

    private var _pdfMetaData = MutableLiveData<HashMap<String, String>>()
    val pdfMetaData: LiveData<HashMap<String, String>>
        get() = _pdfMetaData

    private var _linkedPDFMetaData = MutableLiveData<HashMap<String, String>>()
    val linkedPDFMetaData: LiveData<HashMap<String, String>>
        get() = _linkedPDFMetaData

    private var _pdfHistory = MutableLiveData<List<PDFHistory>>()
    val pdfHistory: LiveData<List<PDFHistory>>
        get() = _pdfHistory

    var pdfDetails: PDFDetails? = null
    var linkedPDFDetails: PDFDetails? = null

    var pdfMetaDataKeyList: List<String> = ArrayList()
    var linkedPDFMetaKeyList: List<String> = ArrayList()

    private var _pdfLinks = MutableLiveData<List<Link>>()
    val pdfLinks : LiveData<List<Link>>
       get() = _pdfLinks

    private var _pdfAttachments = MutableLiveData<List<Link>>()
    val pdfAttachments : LiveData<List<Link>>
        get() = _pdfAttachments

    private var _documentDetail = MutableLiveData<ResultWrapper<DocumentDetail>>()
    var documentDetail: LiveData<ResultWrapper<DocumentDetail>> = _documentDetail

    private var _mDocumentDetail = MutableLiveData<ResultWrapper<DocumentDetail>>()
    var mDocumentDetail: LiveData<ResultWrapper<DocumentDetail>> = _mDocumentDetail

    private var _publishResult = MutableLiveData<ResultWrapper<Any>>()
    var publishResult: LiveData<ResultWrapper<Any>> = _publishResult

    private var _sendDCCResult = MutableLiveData<ResultWrapper<Any>>()
    var sendDCCResult: LiveData<ResultWrapper<Any>> = _sendDCCResult

    private var _replyResult = MutableLiveData<ResultWrapper<Any>>()
    var replyResult: LiveData<ResultWrapper<Any>> = _replyResult

    private var _signApproveResult = MutableLiveData<ResultWrapper<Any>>()
    var signApproveResult: LiveData<ResultWrapper<Any>> = _signApproveResult

    private var _archiveResult = MutableLiveData<ResultWrapper<Any>>()
    var archiveResult: LiveData<ResultWrapper<Any>> = _archiveResult

    private var _orgListResult = MutableLiveData<DocOrganization>()
    var orgListResult : LiveData<DocOrganization> = _orgListResult

    private var _allOrgListResult = MutableLiveData<DocOrganization>()
    var allOrgListResult : LiveData<DocOrganization> = _allOrgListResult

    private var _sisterOrgResult = MutableLiveData<DocOrganization>()
    var sisterOrgResult : LiveData<DocOrganization> = _sisterOrgResult

    private var _allSisterOrgResult = MutableLiveData<DocOrganization>()
    var allSisterOrgResult : LiveData<DocOrganization> = _allSisterOrgResult

    private var _docUserResult = MutableLiveData<DocExternalUser>()
    val docUserResult : LiveData<DocExternalUser> get() = _docUserResult

    private var _allDocUserResult = MutableLiveData<DocExternalUser>()
    val allDocUserResult : LiveData<DocExternalUser> get() = _allDocUserResult

    private var _addDataResult = MutableLiveData<String>()
    val addDataResult : LiveData<String> get() = _addDataResult

    private var _removeDataResult = MutableLiveData<String>()
    val removeDataResult : LiveData<String> get() = _removeDataResult

    var documentOTP = ""

    var selectedDeletingElement = 0 // 0 : Nothing, 1: User , 2: Org. 3: sister org
    var selectedDeletingOrg: Organization? = null
    var selectedDeletingSisterOrg : Organization? = null
    var selectedDeletingUser: ExternalUser? = null

    private var _unarchiveResult = MutableLiveData<String>()
    val unarchiveResult : LiveData<String> get() = _unarchiveResult

    private var _sendResult = MutableLiveData<String>()
    val sendResult : LiveData<String> get() = _sendResult

    private var _shareResult = MutableLiveData<String>()
    val shareResult : LiveData<String> get() = _shareResult

    private var _followPDFResult = MutableLiveData<String>()
    val followPDFResult : LiveData<String> get() = _followPDFResult

    private var _favoriteMarkResult = MutableLiveData<String>()
    val favoriteMarkResult : LiveData<String> get() = _favoriteMarkResult

    private var _markUnreadResult = MutableLiveData<String>()
    val markUnreadResult : LiveData<String> get() = _markUnreadResult

    private var _deleteDocumentResult = MutableLiveData<String>()
    val deleteDocumentResult : LiveData<String> get() = _deleteDocumentResult

    private var _trashResult = MutableLiveData<String>()
    val trashResult : LiveData<String> get() = _trashResult

    private var _unTrashResult = MutableLiveData<String>()
    val unTrashResult : LiveData<String> get() = _unTrashResult

    init {
        getPriorityNActions()

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

    fun initializeProgressData() {
        fileDownloadStatus = DownloadStatus(-1, 0, false)
        _progressLiveData.value= (fileDownloadStatus)
    }

    private fun setProgress(progress: Int) {
        fileDownloadStatus.progress = progress
        _progressLiveData.value=(fileDownloadStatus)
    }

    fun downloadPDFFromServer(
        mContext: Context,
        document: DocumentDetail,
        onFinished: (documentURI: Uri) -> Unit,
    ) {
        downloadingFile = 1
        viewModelScope.launch (Dispatchers.Main){
            val client = downloadRepository.downloadPDFFile(
                document.boxId ?: "", document.documentId ?: "",
                document.transfer.transferId ?: ""
            )

            client.onStart { }
                .catch { }
                .collect { response ->
                    // Write the downloaded file into Zip
                    FileUtils.writePDFFileToDisk(
                        mContext,
                        response.body()!!,
                        document.documentId ?: "",
                        document.transfer.transferId ?: "",
                    ) { uri ->
                        onFinished(uri)
                    }
                }
        }
    }

    fun downloadPDFAttachmentsFromServer(
        mContext: Context,
        boxId: String,
        documentId: String,
        attachmentId: String,
        onFinished: (documentURI: Uri) -> Unit,
    ) {
        downloadingFile = 1
        viewModelScope.launch (Dispatchers.Main){
            val client = downloadRepository.downloadAttachment(
                boxId, documentId,
                attachmentId
            )

            client.onStart { }
                .catch { }
                .collect { response ->
                    // Write the downloaded file into Zip
                    FileUtils.writePDFFileToDisk(
                        mContext,
                        response.body()!!,
                        documentId,
                        attachmentId,
                    ) { uri ->
                        onFinished(uri)
                    }
                }
        }
    }

    fun getPDFMetaData(boxId: String, documentId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val response = repository.getPDFMetaData(
                boxId, documentId
            )) {
                is ResultWrapper.Success -> {
                    pdfDetails= response.value
                    val tempHashMap = HashMap<String, String>()
                    val jsonObject = response.value.properties!!
                    pdfMetaDataKeyList = jsonObject.keySet().toList()
                    for (key in pdfMetaDataKeyList) {
                        if (jsonObject.get(key) != null) {
                            tempHashMap[key] = jsonObject.get(key).toString()
                        }
                    }
                    _pdfMetaData.postValue(tempHashMap)
                }
                is ResultWrapper.GenericError -> {

                }
                is ResultWrapper.NetworkError -> {

                }
                else -> {}
            }
        }
    }

    fun getLinkedPDFMetaData(boxId: String, documentId: String, rootLinkId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val response = repository.getLinkedPDFMetaData(
                boxId, documentId, rootLinkId
            )) {
                is ResultWrapper.Success -> {
                    linkedPDFDetails = response.value
                    val tempHashMap = HashMap<String, String>()
                    val jsonObject = response.value.properties!!
                    linkedPDFMetaKeyList = jsonObject.keySet().toList()
                    for (key in linkedPDFMetaKeyList) {
                        if (jsonObject.get(key) != null) {
                            tempHashMap[key] = jsonObject.get(key).toString()
                        }
                    }
                    _linkedPDFMetaData.postValue(tempHashMap)
                }
                is ResultWrapper.GenericError -> {

                }
                is ResultWrapper.NetworkError -> {

                }
                else -> {}
            }
        }
    }

    fun getPDFHistory(boxId: String, documentId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val response = repository.getPDFHistory(
                boxId, documentId
            )) {
                is ResultWrapper.Success -> {
                    _pdfHistory.postValue(response.value ?: ArrayList())
                }

                else -> {}
            }
        }
    }

    fun getLinks(boxId: String, documentId: String){
        viewModelScope.launch(Dispatchers.IO) {
             _pdfLinks.postValue(repository.getLinks(boxId, documentId))
        }
    }

    fun getAttachments(boxId: String, documentId: String){
        viewModelScope.launch(Dispatchers.IO) {
            _pdfAttachments.postValue(repository.getAttachments(boxId, documentId))
        }
    }

    fun getDocumentDetail(pdfDetails: PDFDetails) {
        _documentDetail.postValue(ResultWrapper.Loading(null))
        viewModelScope.launch(Dispatchers.IO) {
            when (val response = repository.getDocumentDetail(
                pdfDetails.boxId ?: "", pdfDetails.documentId ?: "",
                pdfDetails.transferId ?: ""
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

    fun verifyOTPForMDetail(otp: String) {
        _mDocumentDetail.postValue(ResultWrapper.Loading(null))
        viewModelScope.launch(Dispatchers.IO) {
            when (val response = repository.verityOTPForPDF(
                otp
            )) {
                is ResultWrapper.Success -> {
                    if (response.value.body() == null) {
                        _mDocumentDetail.postValue(
                            ResultWrapper.GenericError(
                                202,
                                response.value.errorBody()!!.string()
                            )
                        )
                    } else {
                        _mDocumentDetail.postValue(ResultWrapper.Success(response.value.body()!!))
                    }
                }
                is ResultWrapper.GenericError -> {
                    _mDocumentDetail.postValue(
                        ResultWrapper.GenericError(
                            response.code,
                            response.error
                        )
                    )
                }
                is ResultWrapper.NetworkError -> _mDocumentDetail.postValue(
                    ResultWrapper.GenericError(
                        null,
                        "Network Error!!!"
                    )
                )
                else -> {}
            }
        }
    }

    fun getDocumentDetail(document: Document) {
        _mDocumentDetail.postValue(ResultWrapper.Loading(null))
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
                        _mDocumentDetail.postValue(
                            ResultWrapper.GenericError(
                                201,
                                response.value.errorBody()!!.string()
                            )
                        )
                    } else {
                        _mDocumentDetail.postValue(ResultWrapper.Success(response.value.body()!!))
                    }
                }
                is ResultWrapper.GenericError -> {
                    _mDocumentDetail.postValue(
                        ResultWrapper.GenericError(
                            response.code,
                            response.error
                        )
                    )
                }
                is ResultWrapper.NetworkError -> _mDocumentDetail.postValue(
                    ResultWrapper.GenericError(
                        null,
                        "Network Error!!!"
                    )
                )

                else -> {}
            }
        }
    }

    fun publishPDF(boxId: String, docId: String, transferId: String, file: File){
        viewModelScope.launch(Dispatchers.Main) {

            val mTransferId =  RequestBody.create(MediaType.parse("text/plain"), transferId)
            val requestFile: RequestBody =
                RequestBody.create(MediaType.parse("multipart/form-data"), file)
            val document =  MultipartBody.Part.createFormData("annotatedFile", file.name, requestFile)


            when (val response = repository.publishDoc(
                boxId, docId,
                mTransferId, document
            )) {
                is ResultWrapper.Success -> {
                    _publishResult.postValue(ResultWrapper.Success(response.value))
                }
                is ResultWrapper.GenericError -> {
                    _publishResult.postValue(
                        ResultWrapper.GenericError(
                            response.code,
                            response.error
                        )
                    )
                }
                is ResultWrapper.NetworkError -> _publishResult.postValue(
                    ResultWrapper.GenericError(
                        null,
                        "Network Error!!!"
                    )
                )

                else -> {}
            }
        }
    }

    fun getOrganizations(documentId: String){
        viewModelScope.launch(Dispatchers.Main) {
            val docOrg  = DocOrganization(documentId, repository.getFavoriteOrg().first().body() ?: ArrayList())

            val originalList = PreferenceManager.docFavoriteOrganizationList as ArrayList<DocOrganization>
            var isInList = false
            for (i in originalList.indices){
                if (originalList[i].docId == documentId){
                    isInList = true
                    originalList[i] = docOrg
                }
            }
            if (!isInList) originalList.add(docOrg)

            PreferenceManager.docFavoriteOrganizationList = originalList
            for (mDocOrg in originalList){
                if (mDocOrg.docId == documentId){
                    _orgListResult.value =mDocOrg
                }
            }
        }
    }

    fun getAllOrganizations(documentId: String){
        viewModelScope.launch(Dispatchers.Main) {
            val docOrg  = DocOrganization(documentId, repository.getOrganization().first().body() ?: ArrayList())

            val originalList = PreferenceManager.docAllOrganizationList as ArrayList<DocOrganization>
            var isInList = false
            for (i in originalList.indices){
                if (originalList[i].docId == documentId){
                    isInList = true
                    originalList[i] = docOrg
                }
            }
            if (!isInList) originalList.add(docOrg)

            PreferenceManager.docAllOrganizationList = originalList
            for (mDocOrg in originalList){
                if (mDocOrg.docId == documentId){
                    _allOrgListResult.value =mDocOrg
                }
            }
        }
    }

    fun getSisterOrganizations(documentId: String){
        viewModelScope.launch(Dispatchers.Main) {
            val docOrg  = DocOrganization(documentId, repository.getFavoriteExternalOrg().first().body() ?: ArrayList())

            val originalList = PreferenceManager.docFavoriteSisterOrganization as ArrayList<DocOrganization>
            var isInList = false
            for (i in originalList.indices){
                if (originalList[i].docId == documentId){
                    isInList = true
                    originalList[i] = docOrg
                }
            }
            if (!isInList) originalList.add(docOrg)

            PreferenceManager.docFavoriteSisterOrganization = originalList
            for (mDocOrg in originalList){
                if (mDocOrg.docId == documentId)
                    _sisterOrgResult.postValue(mDocOrg)
            }
        }
    }

    fun getAllSisterOrgList(documentId: String){
        viewModelScope.launch(Dispatchers.Main) {
            val docOrg  = DocOrganization(documentId, repository.getExternalOrganizations(true).first().body() ?: ArrayList())

            val originalList = PreferenceManager.docAllSisterOrganization as ArrayList<DocOrganization>
            var isInList = false
            for (i in originalList.indices){
                if (originalList[i].docId == documentId){
                    isInList = true
                    originalList[i] = docOrg
                }
            }
            if (!isInList) originalList.add(docOrg)

            PreferenceManager.docAllSisterOrganization = originalList
            for (mDocOrg in originalList){
                if (mDocOrg.docId == documentId)
                    _allSisterOrgResult.postValue(mDocOrg)
            }
        }
    }

    fun getDocumentUsers(documentId: String){
        viewModelScope.launch(Dispatchers.Main) {
            val docUser  = DocExternalUser(documentId, repository.getFavoriteExternalUsers().first().body() ?: ArrayList())

            val originalList = PreferenceManager.docFavoriteUserList as ArrayList<DocExternalUser>
            var isInList = false
            for (i in originalList.indices){
                if (originalList[i].docId == documentId){
                    isInList = true
                    originalList[i] = docUser
                }
            }
            if (!isInList) originalList.add(docUser)
            PreferenceManager.docFavoriteUserList = originalList

            for (mOrgUser in originalList){
                if (mOrgUser.docId == documentId)
                    _docUserResult.postValue(mOrgUser)
            }
        }
    }

    fun getAllDocumentUsers(documentId: String){
        viewModelScope.launch(Dispatchers.Main) {
            val docUser  = DocExternalUser(documentId, repository.getExternalUsers().first().body() ?: ArrayList())

            val originalList = PreferenceManager.docAllUserList as ArrayList<DocExternalUser>
            var isInList = false
            for (i in originalList.indices){
                if (originalList[i].docId == documentId){
                    isInList = true
                    originalList[i] = docUser
                }
            }
            if (!isInList) originalList.add(docUser)
            PreferenceManager.docAllUserList = originalList

            for (mOrgUser in originalList){
                if (mOrgUser.docId == documentId)
                    _allDocUserResult.postValue(mOrgUser)
            }
        }
    }

    private fun getPriorityNActions(){
        viewModelScope.launch(Dispatchers.IO) {
            repository.getPriorities().onStart { }
                .catch {

                }
                .collect { response ->
                    PreferenceManager.priorities = response.body() ?: ArrayList()
                }

            repository.getActions().onStart {  }
                .catch {

                }
                .collect { response ->
                    PreferenceManager.actions = response.body() ?: ArrayList()
                }
        }
    }

    fun getDocumentRelatedData(documentId: String){
        getAllOrganizations(documentId)
        getAllSisterOrgList(documentId)
        getAllDocumentUsers(documentId)

        getDocumentUsers(documentId)
        getSisterOrganizations(documentId)
        getOrganizations(documentId)

    }

    fun getDocSelectedUserList(documentId: String): List<ExternalUser>{
        val docSelectedUserList = PreferenceManager.docSelectedUserList
        var docSelectedUser: DocExternalUser? = null
        for (user in docSelectedUserList){
            if (user.docId == documentId) docSelectedUser = user
        }

        return docSelectedUser?.orgData ?: ArrayList()
    }

    fun addOrgToSelectedList(documentId: String, organization: Organization){
        val orgList = getDocSelectedOrgList(documentId) as ArrayList<Organization>
        val orgSelectedListData = PreferenceManager.docSelectedOrgList as ArrayList<DocOrganization>
        var isOrgAlreadyInList = false
        for (i in orgList.indices){
            if (orgList[i].id == organization.id){
                orgList[i] = organization
                isOrgAlreadyInList = true
            }
        }

        if (!isOrgAlreadyInList) orgList.add(organization)

        orgSelectedListData.find {it.docId == documentId  }?.orgData = orgList
        if (orgSelectedListData.find {  it.docId == documentId} == null){
            orgSelectedListData.add(DocOrganization(documentId, orgList))
        }
        PreferenceManager.docSelectedOrgList = orgSelectedListData
    }

    fun addOrgToFavorite(org: Organization){
        viewModelScope.launch(Dispatchers.IO) {
            when (val response = repository.addOrgToFavorite(org.id?: "", org.name ?: "", "", org.order?:0
            )) {
                is ResultWrapper.Success -> {
                    _addDataResult.postValue(AppConstants.MESSAGE_ADD_SUCCESS)
                }
                is ResultWrapper.GenericError -> {
                    _addDataResult.postValue( response.error ?: "")
                }
                is ResultWrapper.NetworkError -> {
                    _addDataResult.postValue( "")
                }
                else -> {}
            }
        }
    }

    fun addSisterOrgToFavorite(org: Organization){
        viewModelScope.launch(Dispatchers.IO) {
            when (val response =  repository.addSisterOrgToFavorite(org.id?: "", org.name ?: "", org.order ?: 0, org.type?:"")){
                is ResultWrapper.Success -> {
                    _addDataResult.postValue(AppConstants.MESSAGE_ADD_SUCCESS)
                }
                is ResultWrapper.GenericError -> {
                    _addDataResult.postValue( response.error ?: "")
                }
                is ResultWrapper.NetworkError -> {
                    _addDataResult.postValue( "")
                }
                else -> {}
            }
        }
    }

    fun removeOrgFromFavorite(org: Organization){
        selectedDeletingOrg = null
        viewModelScope.launch(Dispatchers.IO) {
            when (val response = repository.deleteOrgFromFavorite(org.id?: "", org.name ?: "", "", org.order?:0
            )) {
                is ResultWrapper.Success -> {
                    selectedDeletingElement = 2
                    selectedDeletingOrg = org
                    _removeDataResult.postValue(AppConstants.MESSAGE_DELETE_SUCCESS)
                }
                is ResultWrapper.GenericError -> {
                    selectedDeletingElement = 2
                    selectedDeletingOrg = org
                    _removeDataResult.postValue(response.error ?: "Error!")
                }
                is ResultWrapper.NetworkError -> {
                    selectedDeletingElement = 2
                    selectedDeletingOrg = org
                    _removeDataResult.postValue( "")
                }
                else -> {}
            }
        }
    }

    fun removeSisterOrgToFavorite(org: Organization){
        selectedDeletingSisterOrg = null
        viewModelScope.launch(Dispatchers.IO) {
            when (val response =  repository.deleteSisterOrgFromFavorite(org.id?: "", org.name ?: "", org.order ?: 0, org.type?:"")){
                is ResultWrapper.Success -> {
                    selectedDeletingElement = 3
                    selectedDeletingSisterOrg = org
                    _removeDataResult.postValue(MESSAGE_DELETE_SUCCESS)
                }
                is ResultWrapper.GenericError -> {
                    selectedDeletingElement = 3
                    selectedDeletingSisterOrg = org
                    _removeDataResult.postValue(response.error ?: "Error!")
                }
                is ResultWrapper.NetworkError -> {
                    selectedDeletingElement = 3
                    selectedDeletingSisterOrg = org
                    _removeDataResult.postValue( "")
                }
                else -> {}
            }
        }
    }

    fun addUserToSelectedList(documentId: String, user: ExternalUser){
        val orgList = getDocSelectedUserList(documentId) as ArrayList<ExternalUser>
        val orgSelectedListData = PreferenceManager.docSelectedUserList as ArrayList<DocExternalUser>
        var isOrgAlreadyInList = false
        for (i in orgList.indices){
            if (orgList[i].userId == user.userId){
                orgList[i] = user
                isOrgAlreadyInList = true
            }
        }

        if (!isOrgAlreadyInList) orgList.add(user)

        orgSelectedListData.find {it.docId == documentId  }?.orgData = orgList
        if (orgSelectedListData.find {  it.docId == documentId} == null){
            orgSelectedListData.add(DocExternalUser(documentId, orgList))
        }
        PreferenceManager.docSelectedUserList = orgSelectedListData
    }

    fun removeUserFromFavorite(user: ExternalUser){
        selectedDeletingSisterOrg = null

        viewModelScope.launch(Dispatchers.IO) {
            when (val response =  repository.deleteUserFromFavorite(user.userId?: "", user.fullName ?: "", user.organizationId ?: "", user.organizationName?:"")){
                is ResultWrapper.Success -> {
                    selectedDeletingElement = 1
                    selectedDeletingUser = user
                    _removeDataResult.postValue(MESSAGE_DELETE_SUCCESS)
                }
                is ResultWrapper.GenericError -> {
                    selectedDeletingElement = 1
                    selectedDeletingUser = user
                    _removeDataResult.postValue(response.error ?: "Error!")
                }
                is ResultWrapper.NetworkError -> {
                    selectedDeletingElement = 1
                    selectedDeletingUser = user
                    _removeDataResult.postValue("")
                }
                else -> {
                }
            }
        }
    }

    fun addUserToFavorite(user: ExternalUser){
        viewModelScope.launch(Dispatchers.IO) {
            when (val response =  repository.addUserToFavorite(user.userId?: "", user.fullName ?: "", user.organizationId ?: "", user.organizationName?:"")){
                is ResultWrapper.Success -> {
                    _addDataResult.postValue(MESSAGE_ADD_SUCCESS)
                }
                is ResultWrapper.GenericError -> {
                    _addDataResult.postValue( response.error ?: "")
                }
                is ResultWrapper.NetworkError -> {
                    _addDataResult.postValue( "")
                }
                else -> {}
            }
        }
    }

    fun deleteOrgFromSelectedList(documentId: String, organization: Organization){
        val orgList = getDocSelectedOrgList(documentId) as ArrayList<Organization>
        val orgSelectedListData = PreferenceManager.docSelectedOrgList as ArrayList<DocOrganization>
        var isOrgAlreadyInList = false
        var position = 0
        for (i in orgList.indices){
            if (orgList[i].id == organization.id){
                isOrgAlreadyInList = true
                position = i
            }
        }

        if (isOrgAlreadyInList) orgList.removeAt(position)

        orgSelectedListData.find {it.docId == documentId  }?.orgData = orgList

        PreferenceManager.docSelectedOrgList = orgSelectedListData
    }

    fun deleteSisterOrgFromList(documentId: String, organization: Organization){
        val orgList = getDocSelectedOrgList(documentId) as ArrayList<Organization>
        val orgSelectedListData = PreferenceManager.docSelectedSisterOrgList as ArrayList<DocOrganization>
        var isOrgAlreadyInList = false
        var position = 0
        for (i in orgList.indices){
            if (orgList[i].id == organization.id){
                isOrgAlreadyInList = true
                position = i
            }
        }

        if (isOrgAlreadyInList) orgList.removeAt(position)

        orgSelectedListData.find {it.docId == documentId  }?.orgData = orgList

        PreferenceManager.docSelectedSisterOrgList = orgSelectedListData
    }

    fun deleteUserFromSelectedList(documentId: String, user: ExternalUser){
        val orgList = getDocSelectedUserList(documentId) as ArrayList<ExternalUser>
        val orgSelectedListData = PreferenceManager.docSelectedUserList as ArrayList<DocExternalUser>
        var isOrgAlreadyInList = false
        var position = 0
        for (i in orgList.indices){
            if (orgList[i].userId == user.userId){
                isOrgAlreadyInList = true
                position = i
            }
        }

        if (isOrgAlreadyInList) orgList.removeAt(position)

        orgSelectedListData.find {it.docId == documentId  }?.orgData = orgList

        PreferenceManager.docSelectedUserList = orgSelectedListData
    }

    fun getDocSelectedOrgList(documentId: String): List<Organization>{
        val docSelectedUserList = PreferenceManager.docSelectedOrgList
        var docSelectedUser: DocOrganization? = null
        for (user in docSelectedUserList){
            if (user.docId == documentId) docSelectedUser = user
        }

        return docSelectedUser?.orgData ?: ArrayList()
    }


    fun findOrgFromSelectedList(docId: String, orgId: String): Organization?{
        return getDocSelectedOrgList(docId).find { it.id == orgId }
    }

    fun findUserFromSelectedList(docId: String, userId: String): ExternalUser?{
        return getDocSelectedUserList(docId).find { it.userId == userId }
    }

    fun getDocSelectedSisterOrgList(documentId: String): List<Organization>{
        val docSelectedUserList = PreferenceManager.docSelectedSisterOrgList
        var docSelectedUser: DocOrganization? = null
        for (user in docSelectedUserList){
            if (user.docId == documentId) docSelectedUser = user
        }

        return docSelectedUser?.orgData ?: ArrayList()
    }

    fun sendDocument(context: Context, documentId: String, boxId: String, transferId: String, file: File){
        var selectedOrgList = getDocSelectedOrgList(documentId)
        var selectedSisterOrgList = getDocSelectedSisterOrgList(documentId)
        var selectedUserList = getDocSelectedUserList(documentId)

        if (selectedOrgList.isEmpty() && selectedSisterOrgList.isEmpty() && selectedUserList.isEmpty()){
            PopupUtils.showAlert(context, "Selected list should not be empty")
        }else{
            var isToExisting = false
            for (org in selectedOrgList){
                if (org.forwardType != 1){
                    isToExisting = true
                }
            }
            if (!isToExisting){
                for (org in selectedSisterOrgList){
                    if (org.forwardType != 1){
                        isToExisting = true
                    }
                }
            }
            if (!isToExisting){
                for (user in selectedUserList){
                    if (user.forwardType == 1){
                        isToExisting = true
                    }
                }
            }
            if (!isToExisting){
                PopupUtils.showAlert(context, "There should be at least one item with To")
            }else{
                var transfers = ArrayList<TransferShareModel>()
                var externalTranssfers=  ArrayList<ExternalTransferModel>()
                for (org in selectedOrgList){

                    if (org.type == TYPE_INTERNAL){
                        var actionsIds = ArrayList<String>()
                        for (action in org.actionList ?: ArrayList()){
                            actionsIds.add(action.value ?: "")
                        }
                        transfers.add(TransferShareModel(documentId, org.id?:"", actionsIds, org.priority?.value ?: "", org.note ?:"",
                            if (org.forwardType == 1) " CC" else "TO"))
                    }else{
                        var actionsIds = ArrayList<String>()
                        for (action in org.actionList ?: ArrayList()){
                            actionsIds.add(action.value ?: "")
                        }
                        externalTranssfers.add(ExternalTransferModel(documentId, actionsIds, org.priority?.value ?: "", org.note ?:"",
                            if (org.forwardType == 1) " CC" else "TO", ExternalOrgModel(org.id?:"", org.type ?:"")))
                    }
                }

                for (org in selectedSisterOrgList){
                    var actionsIds = ArrayList<String>()
                    for (action in org.actionList ?: ArrayList()){
                        actionsIds.add(action.value ?: "")
                    }
                    externalTranssfers.add(ExternalTransferModel(documentId, actionsIds, org.priority?.value ?: "", org.note ?:"",
                        if (org.forwardType == 1) " CC" else "TO", ExternalOrgModel(org.id?:"", org.type ?:"")))
                }

                for (user in selectedUserList){
                    val externalOrgList = getDocSelectedSisterOrgList(documentId)
                    var orgList = getDocSelectedOrgList(documentId)

                    var isExternalUser = false
                    var mOrg: Organization ?= null
                    for (externalOrg in externalOrgList){
                        if (user.organizationId == externalOrg.id){
                            isExternalUser = true
                            mOrg = externalOrg
                        }
                    }
                    val actionsIds = ArrayList<String>()
                    for (action in user.actionList ?: ArrayList()){
                        actionsIds.add(action.value ?: "")
                    }

                    if (isExternalUser){
                        externalTranssfers.add(ExternalTransferModel(documentId, actionsIds, user.priorityValue?.value ?: "", user.note ?:"",
                            if (user.forwardType == 1) " CC" else "TO", ExternalOrgModel(user.organizationId?:"", mOrg?.type ?:"")))
                    }else{
                        transfers.add(
                            TransferShareModel(documentId, user.organizationId, actionsIds, user.priorityValue?.value ?: "", user.note ?:"",
                            if (user.forwardType == 1) " CC" else "TO")
                        )
                    }
                }

                val mTransferId =  RequestBody.create(MediaType.parse("text/plain"), transferId)
                val requestFile: RequestBody =
                    RequestBody.create(MediaType.parse("multipart/form-data"), file)
                val document =  MultipartBody.Part.createFormData("annotatedFile", file.name, requestFile)

                val mTransfers =  RequestBody.create(MediaType.parse("text/plain"), ConverterUtils.convertTransferToString(transfers))
                val mExternalTransfers =  RequestBody.create(MediaType.parse("text/plain"), ConverterUtils.convertExternalTransferToString(externalTranssfers))

                viewModelScope.launch (Dispatchers.IO){
                    when (val response = repository.sendDocument(
                        boxId, documentId,mTransferId, mTransfers,mExternalTransfers, document
                    )) {
                        is ResultWrapper.Success -> {
                            _sendResult.postValue("success")
                        }
                        is ResultWrapper.GenericError -> {
                            _sendResult.postValue(response.error?: "")
                        }
                        is ResultWrapper.NetworkError -> {
                            _sendResult.postValue("Network Error!")
                        }

                        else -> {}
                    }
                }

            }
        }
    }

    fun unArchive(documentId: String, boxId: String, transferId: String, file: File){
        val mTransferId =  RequestBody.create(MediaType.parse("text/plain"), transferId)
        val requestFile: RequestBody =
            RequestBody.create(MediaType.parse("multipart/form-data"), file)
        val document =  MultipartBody.Part.createFormData("annotatedFile", file.name, requestFile)
        viewModelScope.launch(Dispatchers.IO) {
            when(val response = repository.unArchive(boxId, mTransferId, documentId, document)){
                is ResultWrapper.Success -> {
                    _unarchiveResult.postValue("success")
                }
                is ResultWrapper.GenericError -> {
                    _unarchiveResult.postValue(response.error?: "")
                }
                is ResultWrapper.NetworkError -> {
                    _unarchiveResult.postValue("Network Error!")
                }

                else -> {}
            }
        }
    }

    fun shareMainDocument(documentId: String, file: File){
        val requestFile: RequestBody =
            RequestBody.create(MediaType.parse("multipart/form-data"), file)
        val document =  MultipartBody.Part.createFormData("annotatedFile", file.name, requestFile)
        viewModelScope.launch(Dispatchers.IO) {
            when(val response = repository.shareMainPDF(documentId, document)){
                is ResultWrapper.Success -> {
                    _shareResult.postValue("success")
                }
                is ResultWrapper.GenericError -> {
                    _shareResult.postValue(response.error?: "")
                }
                is ResultWrapper.NetworkError -> {
                    _shareResult.postValue("Network Error!")
                }

                else -> {}
            }
        }
    }

    fun shareAttachedPDF(documentId: String, attachmentId: String,file: File){
        val requestFile: RequestBody =
            RequestBody.create(MediaType.parse("multipart/form-data"), file)
        val document =  MultipartBody.Part.createFormData("annotatedFile", file.name, requestFile)
        viewModelScope.launch(Dispatchers.IO) {
            when(val response = repository.shareAttachedPDF(documentId, attachmentId, document)){
                is ResultWrapper.Success -> {
                    _shareResult.postValue("success")
                }
                is ResultWrapper.GenericError -> {
                    _shareResult.postValue(response.error?: "")
                }
                is ResultWrapper.NetworkError -> {
                    _shareResult.postValue("Network Error!")
                }

                else -> {}
            }
        }
    }

    fun followUpDocument(documentId: String, boxId: String, file: File){
        val requestFile: RequestBody =
            RequestBody.create(MediaType.parse("multipart/form-data"), file)
        val document =  MultipartBody.Part.createFormData("annotatedFile", file.name, requestFile)
        viewModelScope.launch(Dispatchers.IO) {
            when(val response = repository.followUpPDF(boxId, documentId, document)){
                is ResultWrapper.Success -> {
                    _followPDFResult.postValue("success")
                }
                is ResultWrapper.GenericError -> {
                    _followPDFResult.postValue(response.error?: "")
                }
                is ResultWrapper.NetworkError -> {
                    _followPDFResult.postValue("Network Error!")
                }

                else -> {}
            }
        }
    }

    fun favoriteDocument(documentId: String, boxId: String, file: File){
        val requestFile: RequestBody =
            RequestBody.create(MediaType.parse("multipart/form-data"), file)
        val document =  MultipartBody.Part.createFormData("annotatedFile", file.name, requestFile)
        viewModelScope.launch(Dispatchers.IO) {
            when(val response = repository.putFavorite(boxId, documentId, document)){
                is ResultWrapper.Success -> {
                    _favoriteMarkResult.postValue("success")
                }
                is ResultWrapper.GenericError -> {
                    _favoriteMarkResult.postValue(response.error?: "")
                }
                is ResultWrapper.NetworkError -> {
                    _favoriteMarkResult.postValue("Network Error!")
                }

                else -> {}
            }
        }
    }

    fun unReadDocument(documentId: String, boxId: String, file: File){
        val requestFile: RequestBody =
            RequestBody.create(MediaType.parse("multipart/form-data"), file)
        val document =  MultipartBody.Part.createFormData("annotatedFile", file.name, requestFile)
        viewModelScope.launch(Dispatchers.IO) {
            when(val response = repository.unReadPDF(boxId, documentId, document)){
                is ResultWrapper.Success -> {
                    _markUnreadResult.postValue("success")
                }
                is ResultWrapper.GenericError -> {
                    _markUnreadResult.postValue(response.error?: "")
                }
                is ResultWrapper.NetworkError -> {
                    _markUnreadResult.postValue("Network Error!")
                }

                else -> {}
            }
        }
    }

    fun deleteDocument(documentId: String, boxId: String, file: File){
        val requestFile: RequestBody =
            RequestBody.create(MediaType.parse("multipart/form-data"), file)
        val document =  MultipartBody.Part.createFormData("annotatedFile", file.name, requestFile)
        viewModelScope.launch(Dispatchers.IO) {
            when(val response = repository.documentDelete(boxId, documentId, document)){
                is ResultWrapper.Success -> {
                    _deleteDocumentResult.postValue("success")
                }
                is ResultWrapper.GenericError -> {
                    _deleteDocumentResult.postValue(response.error?: "")
                }
                is ResultWrapper.NetworkError -> {
                    _deleteDocumentResult.postValue("Network Error!")
                }

                else -> {}
            }
        }
    }

    fun trashDocument(documentId: String, boxId: String, file: File){
        val requestFile: RequestBody =
            RequestBody.create(MediaType.parse("multipart/form-data"), file)
        val document =  MultipartBody.Part.createFormData("annotatedFile", file.name, requestFile)
        viewModelScope.launch(Dispatchers.IO) {
            when(val response = repository.trashDocument(boxId, documentId, document)){
                is ResultWrapper.Success -> {
                    _trashResult.postValue("success")
                }
                is ResultWrapper.GenericError -> {
                    _trashResult.postValue(response.error?: "")
                }
                is ResultWrapper.NetworkError -> {
                    _trashResult.postValue("Network Error!")
                }

                else -> {}
            }
        }
    }

    fun unTrashDocument(documentId: String, boxId: String, file: File){
        val requestFile: RequestBody =
            RequestBody.create(MediaType.parse("multipart/form-data"), file)
        val document =  MultipartBody.Part.createFormData("annotatedFile", file.name, requestFile)
        viewModelScope.launch(Dispatchers.IO) {
            when(val response = repository.unTrashDocument(boxId, documentId, document)){
                is ResultWrapper.Success -> {
                    _unTrashResult.postValue("success")
                }
                is ResultWrapper.GenericError -> {
                    _unTrashResult.postValue(response.error?: "")
                }
                is ResultWrapper.NetworkError -> {
                    _unTrashResult.postValue("Network Error!")
                }

                else -> {}
            }
        }
    }
}