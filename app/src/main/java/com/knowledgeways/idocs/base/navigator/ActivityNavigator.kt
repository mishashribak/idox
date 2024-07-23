package com.knowledgeways.idocs.base.navigator

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.annotation.IdRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class ActivityNavigator @Inject constructor() : Navigator {

    override fun finishActivity(activity: Activity) {
        activity.finish()
    }

    override fun finishActivityWithResult(
        activity: Activity,
        resultCode: Int,
        resultIntentFun: (Intent.() -> Unit)?
    ) {
        val intent = resultIntentFun?.let { Intent().apply(it) }
        activity.setResult(resultCode, intent)
        activity.finish()
    }

    override fun finishAffinity(activity: Activity) {
        activity.finishAffinity()
    }

    override fun startActivity(activity: Activity, intent: Intent) {
        activity.startActivity(intent)
    }

    override fun startActivity(activity: Activity, action: String, uri: Uri?) {
        activity.startActivity(Intent(action, uri))
    }

    override fun startActivity(
        activity: Activity,
        activityClass: Class<out Activity>,
        adaptIntentFun: (Intent.() -> Unit)?
    ) {
        startActivityInternal(activity, activityClass, null, adaptIntentFun)
    }

    override fun startActivityForResult(
        activity: Activity,
        activityClass: Class<out Activity>,
        requestCode: Int,
        adaptIntentFun: (Intent.() -> Unit)?
    ) {
        startActivityInternal(activity, activityClass, requestCode, adaptIntentFun)
    }

    private fun startActivityInternal(
        activity: Activity,
        activityClass: Class<out Activity>,
        requestCode: Int?,
        adaptIntentFun: (Intent.() -> Unit)?
    ) {
        val intent = Intent(activity, activityClass)
        adaptIntentFun?.invoke(intent)

        if (requestCode != null) {
            activity.startActivityForResult(intent, requestCode)
        } else {
            activity.startActivity(intent)
        }
    }

    override fun replaceFragment(
        activity: FragmentActivity,
        @IdRes containerId: Int,
        fragment: Fragment,
        fragmentTag: String?
    ) {
        replaceFragmentInternal(
            activity.supportFragmentManager,
            containerId,
            fragment,
            fragmentTag,
            false
        )
    }

    override fun replaceFragmentAndAddToBackStack(
        activity: FragmentActivity,
        @IdRes containerId: Int,
        fragment: Fragment,
        fragmentTag: String?
    ) {
        replaceFragmentInternal(
            activity.supportFragmentManager,
            containerId,
            fragment,
            fragmentTag,
            true
        )
    }

    override fun replaceFragmentDirectly(
        activity: FragmentActivity,
        @IdRes containerId: Int,
        fragment: Fragment,
        fragmentTag: String?
    ) {
        val ft = activity.supportFragmentManager.beginTransaction()
            .replace(containerId, fragment, fragmentTag ?: fragment.javaClass.simpleName)
        ft.commitNowAllowingStateLoss()
    }

    protected fun replaceFragmentInternal(
        fm: FragmentManager,
        @IdRes containerId: Int,
        fragment: Fragment,
        fragmentTag: String?,
        addToBackstack: Boolean
    ) {
        val ft = fm.beginTransaction()
            .replace(containerId, fragment, fragmentTag ?: fragment.javaClass.simpleName)
        if (addToBackstack) {
            ft.addToBackStack(fragmentTag ?: fragment.javaClass.simpleName).commit()
            fm.executePendingTransactions()
        } else {
            ft.commitNow()
        }
    }

    override fun addFragment(
        activity: FragmentActivity,
        @IdRes containerId: Int,
        fragment: Fragment,
        fragmentTag: String?
    ) {
        addFragmentInternal(
            activity.supportFragmentManager,
            containerId,
            fragment,
            fragmentTag,
            false
        )
    }

    override fun addFragmentAndAddToBackStack(
        activity: FragmentActivity,
        @IdRes containerId: Int,
        fragment: Fragment,
        fragmentTag: String?
    ) {
        addFragmentInternal(
            activity.supportFragmentManager,
            containerId,
            fragment,
            fragmentTag,
            true
        )
    }

    protected fun addFragmentInternal(
        fm: FragmentManager,
        @IdRes containerId: Int,
        fragment: Fragment,
        fragmentTag: String?,
        addToBackstack: Boolean
    ) {
        val ft = fm.beginTransaction()
            .add(containerId, fragment, fragmentTag ?: fragment.javaClass.simpleName)
        if (addToBackstack) {
            ft.addToBackStack(fragmentTag ?: fragment.javaClass.simpleName).commitAllowingStateLoss()
            fm.executePendingTransactions()
        } else {
            ft.commitNow()
        }
    }

    override fun popFragmentBackStackImmediate(activity: FragmentActivity) {
        activity.supportFragmentManager.popBackStack()
    }

    override fun <T : DialogFragment> showDialogFragment(
        activity: FragmentActivity,
        dialog: T,
        fragmentTag: String
    ) {
        
    }

}