package com.knowledgeways.idocs.di.builder

import com.knowledgeways.idocs.ui.main.document.DocumentFragment
import com.pspdfkit.ui.PdfFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentCustomPDFModule {
    @ContributesAndroidInjector
    abstract fun contributePDFFragment(): PdfFragment
}