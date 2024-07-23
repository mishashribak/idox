package com.knowledgeways.idocs.base

import android.app.ActionBar
import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.WindowManager
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.knowledgeways.idocs.BR
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.base.navigator.Navigator
import com.knowledgeways.idocs.di.builder.ViewModelProviderFactory
import dagger.android.AndroidInjection
import io.reactivex.disposables.Disposable
import javax.inject.Inject


abstract class BaseActivity<T : ViewDataBinding?, V :BaseViewModel>: AppCompatActivity(), BaseFragment.Callback {

    @Inject
    lateinit var viewModelFactory: ViewModelProviderFactory

    var viewDataBinding: T? = null
        private set

    private var mViewModel: V ?= null
    abstract val viewModel: V

    private val bindingVariable : Int = BR._all

    lateinit var progressView : View

    private var disposables: ArrayList<Disposable> = ArrayList()

    @Inject
    lateinit var navigator: Navigator

    @get:LayoutRes
    abstract val layoutId: Int

    protected open fun <T: ViewModel> getViewModel(modelClass: Class<T>): T{
        return ViewModelProvider(this)[modelClass]
    }

    override fun onFragmentAttached() {}
    override fun onFragmentDetached(tag: String?) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        setFullScreen()
        performDependencyInjection()
        super.onCreate(savedInstanceState)

        performDataBinding()
        viewModel.isLoading.observe(this){
            isLoading -> checkIsLoading(isLoading)
        }
    }

    fun setFullScreen(){
        val decorView = window.decorView
        // Hide the status bar.
        // Hide the status bar.
        val uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
        decorView.systemUiVisibility = uiOptions
        // Remember that you should never show the action bar if the
        // status bar is hidden, so hide that too if necessary.
        // Remember that you should never show the action bar if the
        // status bar is hidden, so hide that too if necessary.
        val actionBar: ActionBar? = actionBar
        actionBar?.hide()

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private fun performDependencyInjection(){
        AndroidInjection.inject(this)
    }

    private fun performDataBinding(){
        viewDataBinding = DataBindingUtil.setContentView<T>(this, layoutId)
        mViewModel = if (mViewModel == null) viewModel else mViewModel
        viewDataBinding?.setVariable(bindingVariable, mViewModel)
        viewDataBinding?.executePendingBindings()
    }

    fun isTablet(): Boolean{
        return resources.getBoolean(R.bool.isTablet)
    }

    fun checkIsLoading(isLoading: Boolean){
      if (::progressView.isInitialized){
          progressView.visibility = if (isLoading) VISIBLE else GONE
      }
    }

    fun showErrorDialog(errorMessage: String) {

        val alertDialog: AlertDialog = AlertDialog.Builder(this).create()
        alertDialog.setTitle("")
        alertDialog.setMessage(errorMessage)
        alertDialog.setButton(
            AlertDialog.BUTTON_NEUTRAL, "OK"
        ) { dialog, _ -> dialog.dismiss() }
        alertDialog.show()
    }

    fun showTryAgainDialog(errorMessage: String, onTryAgain :() ->Unit) {

        val alertDialog: AlertDialog = AlertDialog.Builder(this).create()
        alertDialog.setTitle("")
        alertDialog.setCancelable(false)
        alertDialog.setMessage(errorMessage)
        alertDialog.setButton(
            AlertDialog.BUTTON_NEUTRAL, "Try again"
        ) { dialog, _ ->
            onTryAgain()
            dialog.dismiss() }
        alertDialog.show()
    }


    fun addFragment(@IdRes containerId: Int, fragment: Fragment) {
        navigator.addFragmentAndAddToBackStack(this, containerId, fragment)
    }

    fun replaceFragment(@IdRes containerId: Int, fragment: Fragment) {
        navigator.replaceFragmentAndAddToBackStack(this, containerId, fragment)
    }

    fun replaceFragmentDirectly(@IdRes containerId: Int, fragment: Fragment) {
        navigator.replaceFragmentDirectly(this, containerId, fragment)
    }

    fun popFragment() {
        navigator.popFragmentBackStackImmediate(this)
    }
}