package com.knowledgeways.idocs.di.builder

import com.knowledgeways.idocs.ui.login.LoginActivity
import com.knowledgeways.idocs.ui.main.MainActivity
import com.knowledgeways.idocs.ui.pdf.CustomPDFActivity
import com.knowledgeways.idocs.ui.splash.LaunchActivity
import com.knowledgeways.idocs.ui.url_settings.UrlSettingsActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
interface ActivityBuilder {

    @ContributesAndroidInjector
    fun buildLaunchActivity(): LaunchActivity

    @ContributesAndroidInjector
    fun buildUrlSettingsActivity(): UrlSettingsActivity

    @ContributesAndroidInjector
    fun buildLoginActivity(): LoginActivity

    @ContributesAndroidInjector(modules = [FragmentMainModule::class])
    fun buildMainActivity(): MainActivity

    @ContributesAndroidInjector(modules = [FragmentMainModule::class])
    fun buildCustomPDFActivity(): CustomPDFActivity
}
