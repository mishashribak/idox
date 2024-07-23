package com.knowledgeways.idocs.ui.splash

import android.Manifest
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.base.BaseActivity
import com.knowledgeways.idocs.databinding.ActivityLaunchBinding
import com.knowledgeways.idocs.db.PreferenceManager
import com.knowledgeways.idocs.network.ResultWrapper
import com.knowledgeways.idocs.network.client.ApiClient
import com.knowledgeways.idocs.network.model.DownloadStatus
import com.knowledgeways.idocs.network.model.theme.Theme
import com.knowledgeways.idocs.ui.login.LoginActivity
import com.knowledgeways.idocs.ui.url_settings.UrlSettingsActivity
import com.knowledgeways.idocs.utils.AppConstants
import com.knowledgeways.idocs.utils.AppConstants.REQUEST_VALUE_PERMISSION_SETTINGS
import com.knowledgeways.idocs.utils.AppConstants.REQUEST_VALUE_URL_SETTINGS
import com.knowledgeways.idocs.utils.CommonUtils
import com.permissionx.guolindev.PermissionX
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject


class LaunchActivity : BaseActivity<ActivityLaunchBinding, LaunchViewModel>(), HasAndroidInjector {

    override val layoutId: Int
        get() = R.layout.activity_launch

    override val viewModel: LaunchViewModel
        get() {
            return getViewModel(LaunchViewModel::class.java)
        }

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    override fun androidInjector() = dispatchingAndroidInjector

    private var selfProgress = 0
    var mCountDownTimer: CountDownTimer? = null
    var settingPageOpened = false
    override fun onCreate(savedInstanceState: Bundle?) {
        setFullScreen()
        super.onCreate(savedInstanceState)

        viewModel.initViewModel(this)

        progressView = viewDataBinding?.layoutProgress?.progressBar!!
        initRedirection()
        addObservers()

        viewDataBinding?.layoutProgress?.apply {
            tvSettings.visibility = VISIBLE
            enableSettingButton(false)
            tvSettings.setOnClickListener {
                val i = Intent(this@LaunchActivity, UrlSettingsActivity::class.java)
                startActivityForResult(i, REQUEST_VALUE_URL_SETTINGS)
            }
        }
    }

    private fun enableSettingButton(enable: Boolean){
        viewDataBinding?.layoutProgress!!.tvSettings.apply {
            isEnabled = enable
            isClickable = enable
        }
    }

    private fun initRedirection(){
        if (PreferenceManager.resourceStatus == 3) {
            gotoLogin(true)
        } else {
            if (CommonUtils.isNetworkConnected(this)){
                viewDataBinding?.layoutProgress?.progressBar?.visibility = VISIBLE
                if (PreferenceManager.resourceStatus == 0) {
                    viewModel.fetchThemeList()
                } else {
                    downloadTheme(CommonUtils.getDefaultThemeId())
                }
            }else{
                enableSettingButton(true)
                viewDataBinding?.layoutProgress?.progressBar?.visibility = GONE
                showTryAgainDialog("Please check your internet connection"){
                    initRedirection()
                }
            }
        }
    }

    private fun addObservers() {
        observeThemeList()
        observeDownloadStatus()
    }

    private fun observeDownloadStatus() {
        viewModel.progressLiveData.observe(this) { state ->
            updateDownloadStatus(state)
        }
    }

    private fun updateDownloadStatus(status: DownloadStatus) {
        if (status.fileType == AppConstants.FILE_TYPE_ICON) mCountDownTimer!!.cancel()
        viewDataBinding?.layoutProgress?.apply {
            tvDescription.text = when (status.fileType) {
                -1 -> resources.getString(R.string.str_fetching_theme_list)
                AppConstants.FILE_TYPE_IMAGE -> resources.getString(R.string.str_downloading_theme_images)
                else -> resources.getString(R.string.str_downloading_theme_icons)
            }
            initProgressLayout(status.fileType == -1)
            if (status.fileType == AppConstants.FILE_TYPE_IMAGE){
                val currentProgress = progressHorizontal.progress + ((status.progress * (100 - progressHorizontal.progress))/ 100)
                progressHorizontal.progress = currentProgress
                "${if (currentProgress > 100) 100 else currentProgress}%".also { tvProgress.text = it }
            }else{
                progressHorizontal.progress = status.progress
                "${if (status.progress > 100) 100 else status.progress}%".also { tvProgress.text = it }
            }

            gotoLogin(status.unzipFinished)
        }
    }

    private fun initProgressLayout(boolean: Boolean){
        viewDataBinding?.layoutProgress?.apply {
            progressHorizontal.visibility = if (boolean) GONE else VISIBLE
            progressDefault.visibility = if (boolean) VISIBLE else GONE
            tvProgress.visibility = if (boolean) INVISIBLE else VISIBLE
        }
    }

    private fun observeThemeList() {
        viewModel.themeLiveData.observe(this) { state ->
            processThemeList(state)
        }
    }

    private fun processThemeList(state: ResultWrapper<List<Theme>?>) {
        when (state) {
            is ResultWrapper.Success -> {
                enableSettingButton(false)
                // Show the SwitchOrganization Popup
                val defaultTheme = viewModel.getDefaultTheme()
                downloadTheme(defaultTheme?.themeId ?: "default-theme")
            }
            is ResultWrapper.GenericError -> {
                val errorMsg = state.error
                Log.e("Login", errorMsg ?: "There was error to fetch themeList")

                // Show the error dialog
                showErrorDialog(errorMsg ?: "There was error to fetch themeList, try again later")

                enableSettingButton(true)
            }

            is ResultWrapper.NetworkError -> {
                enableSettingButton(true)
                viewDataBinding?.layoutProgress?.progressBar?.visibility = GONE
                showTryAgainDialog("Please check your internet connection"){
                    initRedirection()
                }
            }
            else -> {}
        }
    }

    private fun startDownloadTimer(){
        mCountDownTimer = object : CountDownTimer(50* 1000, 800) {
            override fun onTick(millisUntilFinished: Long) {
                if (viewModel.progressLiveData.value?.fileType == AppConstants.FILE_TYPE_IMAGE){
                    selfProgress+= 1
                    viewDataBinding?.layoutProgress?.progressHorizontal?.progress = selfProgress
                    "${selfProgress}%".also { viewDataBinding?.layoutProgress?.tvProgress?.text = it }
                }
            }

            override fun onFinish() {
                //Do what you want

            }
        }
        mCountDownTimer!!.start()
    }

    private fun downloadTheme(defaultThemeId: String) {

        if (SDK_INT >= 30){
            requestPermission()
        }else{
            // Download the theme icons
            PermissionX.init(this@LaunchActivity)
                .permissions(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                .request { allGranted, _, deniedList ->

                    //    downloadFile(defaultThemeId, AppConstants.FILE_TYPE_IMAGE)
                    if (allGranted) {
                        if (PreferenceManager.resourceStatus == 1) {
                            viewModel.downloadFileImages(
                                this@LaunchActivity,
                                defaultThemeId,
                                AppConstants.FILE_TYPE_IMAGE
                            )
                            startDownloadTimer()
                        } else if (PreferenceManager.resourceStatus == 2) {
                            viewModel.unzipFile(
                                this@LaunchActivity,
                                defaultThemeId,
                                AppConstants.FILE_TYPE_IMAGE
                            )
                        }

                    } else {
                        enableSettingButton(true)
                        Toast.makeText(
                            this,
                            "These permissions are denied: $deniedList",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }

    private fun requestPermission() {
        settingPageOpened = true

        if (SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse(String.format("package:%s", applicationContext.packageName))
                startActivityForResult(intent, REQUEST_VALUE_PERMISSION_SETTINGS)
            } catch (e: Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                startActivityForResult(intent, REQUEST_VALUE_PERMISSION_SETTINGS)
            }
        } else {
            //below android 11
            ActivityCompat.requestPermissions(
                this@LaunchActivity,
                arrayOf(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE),
                REQUEST_VALUE_PERMISSION_SETTINGS
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int,  data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_VALUE_PERMISSION_SETTINGS) {
            if (SDK_INT >= Build.VERSION_CODES.R) {
                settingPageOpened = false
                if (Environment.isExternalStorageManager()) {
                    if (PreferenceManager.resourceStatus == 1) {
                        viewModel.downloadFileImages(
                            this@LaunchActivity,
                            CommonUtils.getDefaultThemeId(),
                            AppConstants.FILE_TYPE_IMAGE
                        )
                        startDownloadTimer()
                    } else if (PreferenceManager.resourceStatus == 2) {
                        viewModel.unzipFile(
                            this@LaunchActivity,
                            CommonUtils.getDefaultThemeId(),
                            AppConstants.FILE_TYPE_IMAGE
                        )
                    }
                    // perform action when allow permission success
                } else {
                    if (!settingPageOpened){
                        requestPermission()
                    }
                }
            }
        }else if (requestCode == REQUEST_VALUE_URL_SETTINGS){
            if (resultCode == RESULT_OK){
                ApiClient.init(this@LaunchActivity)
                viewModel.changeRepository()
                viewModel.initViewModel(this@LaunchActivity)
                initRedirection()
            }else{
                initRedirection()
            }
        }
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    private fun gotoLogin(mFinished: Boolean) {
        if (mFinished) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}