package com.knowledgeways.idocs.base.navigator

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity


interface FragmentNavigator : Navigator {

    fun replaceChildFragment(
        activity: FragmentActivity,
        @IdRes containerId: Int,
        fragment: Fragment,
        fragmentTag: String? = null
    )

    fun replaceChildFragmentAndAddToBackStack(
        activity: FragmentActivity,
        @IdRes containerId: Int,
        fragment: Fragment,
        fragmentTag: String?
    )

    fun addChildFragment(
        activity: FragmentActivity,
        @IdRes containerId: Int,
        fragment: Fragment,
        fragmentTag: String? = null
    )

    fun addChildFragmentAndAddToBackStack(
        activity: FragmentActivity,
        @IdRes containerId: Int,
        fragment: Fragment,
        fragmentTag: String?
    )

    fun popChildFragmentBackstackImmediate(fragment: Fragment)

}