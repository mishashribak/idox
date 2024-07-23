package com.knowledgeways.idocs.di.builder

import com.knowledgeways.idocs.ui.main.document.DocumentFragment
import com.knowledgeways.idocs.ui.main.search.SearchFragment
import com.knowledgeways.idocs.ui.main.searchresult.SearchResultFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentMainModule {
    @ContributesAndroidInjector
    abstract fun contributeDocumentFragment(): DocumentFragment

    @ContributesAndroidInjector
    abstract fun contributeSearchFragment(): SearchFragment

    @ContributesAndroidInjector
    abstract fun contributeSearchResultFragment(): SearchResultFragment
}