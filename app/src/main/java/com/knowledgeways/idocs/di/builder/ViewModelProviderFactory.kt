package com.knowledgeways.idocs.di.builder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.knowledgeways.idocs.ui.login.LoginViewModel
import com.knowledgeways.idocs.ui.main.MainViewModel
import com.knowledgeways.idocs.ui.splash.LaunchViewModel
import com.knowledgeways.idocs.ui.url_settings.UrlSettingsViewModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ViewModelProviderFactory @Inject constructor(): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        (return when {
            modelClass.isAssignableFrom(LaunchViewModel::class.java) -> {
                LaunchViewModel()  as T
            }
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel()  as T
            }
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel()  as T
            }
            modelClass.isAssignableFrom(UrlSettingsViewModel::class.java) -> {
                UrlSettingsViewModel()  as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel Class")
        })
    }
}