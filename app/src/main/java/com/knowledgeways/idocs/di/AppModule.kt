package com.knowledgeways.idocs.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.knowledgeways.idocs.IDoxApp
import com.knowledgeways.idocs.base.navigator.ActivityNavigator
import com.knowledgeways.idocs.base.navigator.ChildFragmentNavigator
import com.knowledgeways.idocs.base.navigator.FragmentNavigator
import com.knowledgeways.idocs.base.navigator.Navigator
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Singleton


@Module
class AppModule {


    @Provides
    @Singleton
    fun provideContext(application: IDoxApp): Context {
        return application
    }


    @Provides
    @Singleton
    fun provideCompositeDisposable(): CompositeDisposable {
        return CompositeDisposable()
    }


    @Provides
    @Singleton
    fun provideGson(): Gson? {
        return GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
    }


    @Provides
    @Singleton
    fun provideNavigator(activityNavigator: ActivityNavigator): Navigator {
        return activityNavigator
    }

    @Provides
    @Singleton
    fun provideFragmentNavigator(childFragmentNavigator: ChildFragmentNavigator): FragmentNavigator {
        return childFragmentNavigator
    }
}