package com.knowledgeways.idocs

import android.app.Application
import com.knowledgeways.idocs.db.PreferenceManager
import com.knowledgeways.idocs.di.AppInjector
import com.knowledgeways.idocs.network.client.ApiClient
import com.knowledgeways.idocs.utils.AppConstants
import com.knowledgeways.idocs.utils.ResUtils
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

class IDoxApp : Application(), HasAndroidInjector {

    @Inject
    lateinit var applicationDispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    override fun androidInjector(): AndroidInjector<Any> {
        return applicationDispatchingAndroidInjector
    }

    override fun onCreate() {
        super.onCreate()

        init()
        ResUtils.checkCurrentLocale()
    }

    private fun init() {
        PreferenceManager.init(this)
        AppInjector.init(this)
        ApiClient.init(this)
    }
}