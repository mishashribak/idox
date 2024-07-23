package com.knowledgeways.idocs.ui.splash

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.knowledgeways.idocs.base.BaseViewModel
import com.knowledgeways.idocs.db.PreferenceManager
import com.knowledgeways.idocs.network.downloadmanager.DownloadListener
import com.knowledgeways.idocs.network.client.DownloadAPIClient
import com.knowledgeways.idocs.network.repository.DownloadRepository
import com.knowledgeways.idocs.network.ResultWrapper
import com.knowledgeways.idocs.network.client.ApiClient
import com.knowledgeways.idocs.network.model.DownloadStatus
import com.knowledgeways.idocs.network.model.theme.Theme
import com.knowledgeways.idocs.network.repository.Repository
import com.knowledgeways.idocs.utils.AppConstants
import com.knowledgeways.idocs.utils.ConverterUtils
import com.knowledgeways.idocs.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class LaunchViewModel : BaseViewModel() {
    private var _themesLiveData = MutableLiveData<ResultWrapper<List<Theme>>>()
    val themeLiveData: LiveData<ResultWrapper<List<Theme>>>
        get() = _themesLiveData

    var fileDownloadStatus = DownloadStatus(-1, 0, false)

    private var _progressLiveData = MutableLiveData<DownloadStatus>()
    var progressLiveData: LiveData<DownloadStatus> = _progressLiveData

    lateinit var downloadRepository: DownloadRepository
    fun initViewModel(context: Context){
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

    fun fetchThemeList() {
        if (PreferenceManager.themeListString.isNotEmpty()) {
            getLocalThemeList()
        } else {
            getThemeList()
        }
    }

    private fun getThemeList() {
        setFileType(-1)
        viewModelScope.launch(Dispatchers.IO) {

                when (val orgResponse = repository.getThemeList()) {
                    is ResultWrapper.NetworkError -> {
                        _themesLiveData.postValue(ResultWrapper.GenericError(null, "Network Error!!!"))
                    }
                    is ResultWrapper.GenericError -> {
                        _themesLiveData.postValue(ResultWrapper.GenericError(orgResponse.code, orgResponse.error))
                    }
                    is ResultWrapper.Success -> {
                        PreferenceManager.themeListString = ConverterUtils.convertThemeListToString(orgResponse.value)
                        _themesLiveData.postValue(ResultWrapper.Success(orgResponse.value))
                        PreferenceManager.resourceStatus = 1
                    }

                    else -> {}
                }
                setLoading(true)
        }
    }

    private fun getLocalThemeList() {
        val themeList = ConverterUtils.getThemeList() ?: ArrayList()
        _themesLiveData.postValue(ResultWrapper.Success(themeList))
    }

    override fun getDefaultTheme(): Theme?{
        if (PreferenceManager.resourceStatus == 0){
            return when (val result : ResultWrapper<List<Theme>> = themeLiveData.value!!){
                is ResultWrapper.Success -> {
                    // Show the SwitchOrganization Popup
                   val  themeList = result.value
                    var defaultTheme: Theme ?= null
                    for (theme in themeList){
                        if (theme.default == true){
                            defaultTheme = theme
                            break
                        }
                    }
                    defaultTheme
                }else -> null

            }
        }else{
            return super.getDefaultTheme()
        }
    }

    fun downloadFileImages(
        mContext: Context,
        defaultThemeId: String,
        fileType: Int,
    ) {
        viewModelScope.launch {
            setFileType(fileType)

            val client =  if (fileType == AppConstants.FILE_TYPE_IMAGE) downloadRepository.downloadThemeImages(
                defaultThemeId
            )
            else downloadRepository.downloadThemeIcons(defaultThemeId)

            client.onStart {  }
                .catch {  }
                .collect{ response->
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

    private fun setFileType(fileType: Int){
        fileDownloadStatus.fileType = fileType
        _progressLiveData.postValue(fileDownloadStatus)
    }

    private fun setProgress(progress: Int){
        fileDownloadStatus.progress = progress
        _progressLiveData.postValue(fileDownloadStatus)
    }

    private fun setDownloadFinished(){
        fileDownloadStatus.unzipFinished = true
        _progressLiveData.postValue(fileDownloadStatus)
    }
}