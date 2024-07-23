package com.knowledgeways.idocs.base.navigator

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.annotation.IdRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

interface Navigator {

    companion object {
        const val EXTRA_ARG = "_args"
    }

    fun finishActivity(activity: Activity)
    fun finishActivityWithResult(
        activity: Activity,
        resultCode: Int,
        resultIntentFun: (Intent.() -> Unit)? = null
    )

    fun finishAffinity(activity: Activity)

    fun startActivity(activity: Activity, intent: Intent)
    fun startActivity(activity: Activity, action: String, uri: Uri? = null)
    fun startActivity(
        activity: Activity,
        activityClass: Class<out Activity>,
        adaptIntentFun: (Intent.() -> Unit)? = null
    )

    fun startActivityForResult(
        activity: Activity,
        activityClass: Class<out Activity>,
        requestCode: Int,
        adaptIntentFun: (Intent.() -> Unit)? = null
    )

    fun replaceFragment(
        activity: FragmentActivity,
        @IdRes containerId: Int,
        fragment: Fragment,
        fragmentTag: String? = null
    )

    fun replaceFragmentAndAddToBackStack(
        activity: FragmentActivity,
        @IdRes containerId: Int,
        fragment: Fragment,
        fragmentTag: String? = null
    )

    fun replaceFragmentDirectly(
        activity: FragmentActivity,
        @IdRes containerId: Int,
        fragment: Fragment,
        fragmentTag: String? = null
    )

    fun addFragment(
        activity: FragmentActivity,
        @IdRes containerId: Int,
        fragment: Fragment,
        fragmentTag: String? = null
    )

    fun addFragmentAndAddToBackStack(
        activity: FragmentActivity,
        @IdRes containerId: Int,
        fragment: Fragment,
        fragmentTag: String? = null
    )

    fun popFragmentBackStackImmediate(activity: FragmentActivity)

    fun <T : DialogFragment> showDialogFragment(
        activity: FragmentActivity,
        dialog: T,
        fragmentTag: String = dialog.javaClass.name
    )
}