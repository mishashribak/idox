package com.knowledgeways.idocs.di

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.knowledgeways.idocs.IDoxApp
import dagger.android.AndroidInjection
import dagger.android.HasAndroidInjector

object AppInjector {

    fun init(app: IDoxApp) {
        DaggerAppComponent.builder()
            .application(app)
            .build().inject(app)

        app.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, p1: Bundle?) {
                handleActivity(activity)
            }

            override fun onActivityStarted(activity: Activity) {
            }

            override fun onActivityResumed(activity: Activity) {

            }

            override fun onActivityPaused(activity: Activity) {

            }

            override fun onActivityStopped(activity: Activity) {

            }

            override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {

            }

            override fun onActivityDestroyed(p0: Activity) {

            }
        })
    }

    private fun handleActivity(activity: Activity) {
        if (activity is HasAndroidInjector) {
            AndroidInjection.inject(activity)
        }
    }
}