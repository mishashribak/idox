package com.knowledgeways.idocs.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.knowledgeways.idocs.base.BaseViewModel
import com.knowledgeways.idocs.db.PreferenceManager
import com.knowledgeways.idocs.network.ResultWrapper
import com.knowledgeways.idocs.network.model.user.User
import com.knowledgeways.idocs.utils.CommonUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginViewModel : BaseViewModel() {

    // Testing
    private var _orgLiveData = MutableLiveData<ResultWrapper<User>>()
    val switchOrgLiveData: LiveData<ResultWrapper<User>>
        get() = _orgLiveData

    // Testing
    private var _usrLiveData = MutableLiveData<ResultWrapper<User>>()
    val usrLiveData: LiveData<ResultWrapper<User>>
        get() = _usrLiveData

    var cookie = ""

    fun callLogin(username: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            setLoading(true)

            when (val orgResponse = repository.login(username, password)) {
                is ResultWrapper.NetworkError -> _usrLiveData.postValue(ResultWrapper.GenericError(null, "Network Error!!!"))
                is ResultWrapper.GenericError -> _usrLiveData.postValue(ResultWrapper.GenericError(orgResponse.code, orgResponse.error))
                is ResultWrapper.Success ->{
                    PreferenceManager.user = orgResponse.value.body()

                    val headers =orgResponse.value.headers()
                    val headerMapList: Map<String, List<String>> = headers.toMultimap()

                    val allCookies = headerMapList["Set-Cookie"]

                    allCookies!!.forEach {
                        if (it.contains("JSESSIONID")) {
                            PreferenceManager.sessionId =  CommonUtils.trimSessionId(it)
                        }else if (it.contains("remember-me")){
                            PreferenceManager.rememberMe = CommonUtils.getRememberMeString(it)
                        }
                    }
/*
                    cookie = orgResponse.value.headers().get("Set-Cookie") ?: ""
                    if (cookie.contains("JSESSIONID")) {
                        PreferenceManager.sessionId =  cookie
                    }*/

                    _usrLiveData.postValue(ResultWrapper.Success(orgResponse.value.body()!!))
                }

                else -> {}
            }

            setLoading(false)
        }
    }

    fun fetchSwitchOrganization(organizationId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            setLoading(true)
            when (val orgResponse = repository.switchOrganization(organizationId)) {
                is ResultWrapper.NetworkError -> _orgLiveData.postValue(ResultWrapper.GenericError(null, "Network Error!!!"))
                is ResultWrapper.GenericError -> _orgLiveData.postValue(ResultWrapper.GenericError(orgResponse.code, orgResponse.error))
                is ResultWrapper.Success -> _orgLiveData.postValue(ResultWrapper.Success(orgResponse.value))
                else -> {}
            }
            setLoading(false)
        }
    }
}