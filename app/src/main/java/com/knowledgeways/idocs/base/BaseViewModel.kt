package com.knowledgeways.idocs.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.knowledgeways.idocs.db.PreferenceManager
import com.knowledgeways.idocs.network.client.ApiClient
import com.knowledgeways.idocs.network.repository.Repository
import com.knowledgeways.idocs.network.model.theme.Theme
import com.knowledgeways.idocs.network.model.user.Organization
import com.knowledgeways.idocs.utils.CommonUtils
import io.reactivex.disposables.CompositeDisposable

abstract class BaseViewModel : ViewModel() {
    var repository : Repository = Repository(ApiClient.apiService!!)

    private var _isLoading = MutableLiveData<Boolean>()
    val isLoading : LiveData<Boolean> get() = _isLoading

    var compositeDisposable = CompositeDisposable()

    fun changeRepository(){
        repository = Repository(ApiClient.apiService!!)
    }
    open fun getDefaultTheme(): Theme?{
        return CommonUtils.getDefaultTheme()
    }

    fun setLoading(mLoading: Boolean){
        _isLoading.postValue(mLoading)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

    fun getOrganizationList() : List<Organization>{
        return PreferenceManager.userOrganizationList
    }

    fun getSelectedOrganization() : Organization ?{
        getOrganizationList().forEach {
            if (it.id == PreferenceManager.organizationID){
               return  it
            }
        }
        return  null
    }
}