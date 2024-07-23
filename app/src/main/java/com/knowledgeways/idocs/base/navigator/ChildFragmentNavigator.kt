package com.knowledgeways.idocs.base.navigator

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import javax.inject.Singleton

@Singleton
abstract class ChildFragmentNavigator : ActivityNavigator(), FragmentNavigator {

    override fun replaceChildFragment(
        activity: FragmentActivity,
        @IdRes containerId: Int,
        fragment: Fragment,
        fragmentTag: String?
    ) {
        replaceFragmentInternal(
            fragment.childFragmentManager,
            containerId,
            fragment,
            fragmentTag,
            false
        )
    }

    override fun replaceChildFragmentAndAddToBackStack(
        activity: FragmentActivity,
        @IdRes containerId: Int,
        fragment: Fragment,
        fragmentTag: String?
    ) {
        replaceFragmentInternal(
            fragment.childFragmentManager,
            containerId,
            fragment,
            fragmentTag,
            true
        )
    }

    override fun addChildFragment(
        activity: FragmentActivity,
        @IdRes containerId: Int,
        fragment: Fragment,
        fragmentTag: String?
    ) {
        addFragmentInternal(
            fragment.childFragmentManager,
            containerId,
            fragment,
            fragmentTag,
            false
        )
    }

    override fun addChildFragmentAndAddToBackStack(
        activity: FragmentActivity,
        @IdRes containerId: Int,
        fragment: Fragment,
        fragmentTag: String?
    ) {
        addFragmentInternal(fragment.childFragmentManager, containerId, fragment, fragmentTag, true)
    }

    override fun popChildFragmentBackstackImmediate(fragment: Fragment) {
        fragment.childFragmentManager.popBackStackImmediate()
    }

}
