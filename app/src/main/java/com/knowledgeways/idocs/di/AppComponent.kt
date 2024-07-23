package com.knowledgeways.idocs.di

import com.knowledgeways.idocs.IDoxApp
import com.knowledgeways.idocs.di.builder.ActivityBuilder
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [AndroidInjectionModule::class, AppModule::class, ActivityBuilder::class])
interface AppComponent {

    @Component.Builder
    interface Builder{
        @BindsInstance
        fun application(application: IDoxApp): Builder
        fun build (): AppComponent
    }

    fun inject(app: IDoxApp)
}