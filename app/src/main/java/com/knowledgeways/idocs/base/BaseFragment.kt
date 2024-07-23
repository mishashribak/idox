package com.knowledgeways.idocs.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.base.navigator.Navigator
import com.knowledgeways.idocs.di.Injectable
import com.knowledgeways.idocs.di.builder.ViewModelProviderFactory
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

abstract class BaseFragment<T : ViewDataBinding?, V : BaseViewModel?> : Fragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProviderFactory

    @Inject
    lateinit var navigator: Navigator

    lateinit var baseActivity: BaseActivity<*, *>
        private set

    private var mRootView: View? = null
    var viewDataBinding: T? = null
        private set
    private var mViewModel: V? = null
    var isViewAlive = true

    /**
     * Override for set binding variable
     *
     * @return variable id
     */
    private val bindingVariable: Int = 0

    /**
     * @return layout resource id
     */
    @get:LayoutRes
    abstract val layoutId: Int

    /**
     * Override for set view model
     *
     * @return view model instance
     */
    abstract val viewModel: V

    var progressView: View? = null


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BaseActivity<*, *>) {
            baseActivity = context
            baseActivity.onFragmentAttached()
        }
    }

    protected open fun <T : ViewModel> getViewModel(cls: java.lang.Class<T>): T {
        return ViewModelProvider(this)[cls]
    }

    protected open fun <T : ViewModel> getViewModel(
        fregmentActivity: FragmentActivity,
        cls: java.lang.Class<T>
    ): T {
        return ViewModelProvider(fregmentActivity)[cls]
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        performDependencyInjection()
        super.onCreate(savedInstanceState)
        mViewModel = viewModel
        setHasOptionsMenu(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewDataBinding = DataBindingUtil.inflate<T>(inflater, layoutId, container, false)
        mRootView = viewDataBinding!!.root
        return mRootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewDataBinding!!.setVariable(bindingVariable, mViewModel)
        viewDataBinding!!.lifecycleOwner = this
        viewDataBinding!!.executePendingBindings()
        mViewModel?.isLoading!!.observe(baseActivity, { consumeResponse(it) })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isViewAlive = false
        viewDataBinding = null
    }

    open fun consumeResponse(o: Boolean) {
        if (o) {
            showLoading()
        } else {
            hideLoading()
        }
    }

    fun showLoading() {
        if (progressView != null) {
            progressView!!.visibility = View.VISIBLE
        }
    }

    fun hideLoading() {
        if (progressView != null) {
            progressView!!.visibility = View.GONE
        }
    }



    private fun performDependencyInjection() {
        AndroidSupportInjection.inject(this)
    }

    interface Callback {
        fun onFragmentAttached()
        fun onFragmentDetached(tag: String?)
    }


    private fun showSuccessCaseFor(successView: View, errorView: View) {
        successView.visibility = View.VISIBLE
        errorView.visibility = View.GONE
    }

    private fun showErrorCaseFor(successView: View, errorView: View) {
        successView.visibility = View.GONE
        errorView.visibility = View.VISIBLE
    }



    fun startActivity(activityClass: Class<out Activity>) {
        navigator.startActivity(baseActivity, activityClass)
        applyAnimation()
    }

    override fun startActivity(intent: Intent){
        super.startActivity(intent)
        applyAnimation()
    }

    private fun applyAnimation(){
        baseActivity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out)

    }

    fun finishActivity() {
        navigator.finishActivity(baseActivity)
    }

    fun finishActivityWithResult(resultCode: Int, resultIntentFun: (Intent.() -> Unit)? = null) {
        navigator.finishActivityWithResult(baseActivity, resultCode, resultIntentFun)
    }

    fun addFragment(@IdRes containerId: Int, fragment: Fragment, fragmentTag: String? = null) {
        navigator.addFragmentAndAddToBackStack(baseActivity, containerId, fragment, fragmentTag)
    }

    fun replaceFragment(@IdRes containerId: Int, fragment: Fragment, fragmentTag: String? = null) {
        navigator.replaceFragmentAndAddToBackStack(baseActivity, containerId, fragment, fragmentTag)
    }

    fun replaceFragmentDirectly(
        @IdRes containerId: Int,
        fragment: Fragment,
        fragmentTag: String? = null
    ) {
        navigator.replaceFragment(baseActivity, containerId, fragment, fragmentTag)
    }

    fun popBackStack() {
        navigator.popFragmentBackStackImmediate(baseActivity)
    }

}