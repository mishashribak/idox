package com.knowledgeways.idocs.ui.login

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.util.TypedValue
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.base.BaseActivity
import com.knowledgeways.idocs.databinding.ActivityLoginBinding
import com.knowledgeways.idocs.db.PreferenceManager
import com.knowledgeways.idocs.network.ResultWrapper
import com.knowledgeways.idocs.network.client.ApiClient
import com.knowledgeways.idocs.network.model.theme.Theme
import com.knowledgeways.idocs.network.model.theme.ThemeTextStyle
import com.knowledgeways.idocs.network.model.user.Organization
import com.knowledgeways.idocs.network.model.user.User
import com.knowledgeways.idocs.ui.component.dialog.organization.OrganizationAdapter
import com.knowledgeways.idocs.ui.component.dialog.organization.OrganizationPicker
import com.knowledgeways.idocs.ui.main.MainActivity
import com.knowledgeways.idocs.ui.url_settings.UrlSettingsActivity
import com.knowledgeways.idocs.utils.*
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject


class LoginActivity : BaseActivity<ActivityLoginBinding, LoginViewModel>(),
    OrganizationAdapter.OnOrganizationClickedListener, HasAndroidInjector {

    override val layoutId: Int
        get() = R.layout.activity_login

    override val viewModel: LoginViewModel
        get() {
            return getViewModel(LoginViewModel::class.java)
        }

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    override fun androidInjector() = dispatchingAndroidInjector

    private var strUserName = ""
    private var strPassword = ""
    lateinit var organizationAdapter: OrganizationAdapter
    var popupToolbarColor = ""
    var isTabViewInitialized = false
    var popupForm: ThemeTextStyle? = null
    var defaultTheme: Theme? = null
    var popupButtonForm: ThemeTextStyle? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        progressView = viewDataBinding?.progressBar!!

        initUI()

        // Login Callback
        viewModel.usrLiveData.observe(this) {
            callbackLogin(it)
        }

        // SwitchOrganization Callback
        viewModel.switchOrgLiveData.observe(this) {
            callbackFetchSwitchOrganization(it)
        }

    }

    private fun initUI() {
        defaultTheme = CommonUtils.getDefaultTheme()
        popupForm = defaultTheme!!.popupForm!!.title
        popupButtonForm = defaultTheme!!.popupForm?.button
        initBackground(getCurrentOrientation())
        initEditText()
        initButton()
        initInvisibleView()

    }

    /**
     * Callback called after logged
     */
    private fun callbackLogin(result: ResultWrapper<User>) {

        when (result) {
            is ResultWrapper.Success -> {
                // Show the SwitchOrganization Popup
                enableChooseOrganization(true)
                displayDialog(result.value)
            }
            is ResultWrapper.GenericError -> {
                val errorMsg = result.error
                Log.e("Login", errorMsg ?: "There was error to login, try again later")

                // Show the error dialog
                showErrorDialog(errorMsg ?: "There was error to login, try again later")
            }
            else -> {}
        }

    }

    /**
     * Callback called after selected the organization
     */
    private fun callbackFetchSwitchOrganization(result: ResultWrapper<User>) {

        // Close the popup
        when (result) {
            is ResultWrapper.Success -> {
                viewDataBinding?.layoutOverlay?.visibility = GONE
                PreferenceManager.user = result.value
                enableChooseOrganization(false)
                gotoMainActivity()
            }
            is ResultWrapper.GenericError -> {
                val errorMsg = result.error
                Log.e("SwitchOrganization", errorMsg ?: "There was error to switch an organization")

                // Show the error dialog
                showErrorDialog(errorMsg ?: "There was error to switch an organization")
            }
            else -> {}
        }
    }

    private fun getCurrentOrientation(): Int {
        return this.resources.configuration.orientation
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        initBackground(newConfig.orientation)
    }

    private fun initBackground(orientation: Int) {
        GlideApp.with(this).load(ResUtils.getLoginBackground(this, orientation))
            .into(viewDataBinding?.ivBackground!!)
    }

    private fun initEditText() {
        defaultTheme!!.loginForm.let {
            GlideApp.with(this).load(
                ResUtils.getNormalImageFromTheme(this, it?.username?.bgImg ?: "")
            )
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(viewDataBinding?.ivUsername!!)

            GlideApp.with(this).load(
                ResUtils.getNormalImageFromTheme(this, it?.password?.bgImg ?: "")
            )
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(viewDataBinding?.ivPassword!!)

            addEdittextObserver()
            initRemoveTextButtons()

            if (it?.username != null) {
                setEdittextStyle(it.username!!)
            }
        }
    }

    private fun addEdittextObserver() {
        addUsernameTextWatcher()
        addPasswordTextWatcher()
    }

    private fun addUsernameTextWatcher() {
        viewDataBinding?.apply {
            edittextPassword.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                    }

                    override fun afterTextChanged(textChanged: Editable?) {
                        strPassword = textChanged.toString().trim()
                        ivRemovePassword.visibility = if (strPassword.isEmpty()) GONE else VISIBLE
                    }

                }
            )
        }
    }

    private fun addPasswordTextWatcher() {
        viewDataBinding?.apply {
            edittextUserName.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                    }

                    override fun afterTextChanged(textChanged: Editable?) {
                        strUserName = textChanged.toString().trim()
                        ivRemoveUserName.visibility = if (strUserName.isEmpty()) GONE else VISIBLE
                    }

                }
            )
        }
    }

    private fun initRemoveTextButtons() {
        viewDataBinding?.apply {
            ivRemovePassword.setOnClickListener { edittextPassword.setText("") }
            ivUsername.setOnClickListener { edittextUserName.setText("") }

            tvSettings.setOnClickListener {
                val i = Intent(this@LoginActivity, UrlSettingsActivity::class.java)
                startActivityForResult(i, AppConstants.REQUEST_VALUE_URL_SETTINGS)
            }
        }
    }

    private fun initButton() {
        viewDataBinding?.ivLogin.let {
            Glide.with(this@LoginActivity).load(ResUtils.getLoginButtonImage(this))
                .into(it!!)

            it.setOnClickListener {
                KeyboardUtils.hideKeyboard(this@LoginActivity)
                val errorMessage =
                    CommonUtils.validateInfo(this@LoginActivity, strUserName, strPassword)
                if (errorMessage.isEmpty()) {
                    login()
                } else {
                    showErrorDialog(errorMessage)
                }
            }
        }
    }

    private fun initInvisibleView() {
        Glide.with(this@LoginActivity)
            .load(ResUtils.getToolbarBackgroundImage(this, popupForm!!.bgImg!!))
            .into(viewDataBinding?.ivToolbarColor!!)
    }

    private fun login() {
        popupToolbarColor = CommonUtils.getBackgroundColor(viewDataBinding?.ivToolbarColor!!)
        //   viewModel.callLogin(strUserName, getBase64(strPassword))
        viewModel.callLogin("411583", "a3dheXNyb290")
    }

    private fun initTabletBubbleView() {
        if (!isTabViewInitialized) {
            viewDataBinding?.layoutOrganization?.apply {

                tvClose.setTextAppearance(ResUtils.getTextViewStyle(CommonUtils.getDefaultTheme()?.popupForm?.button?.bold!!))
                tvTitle.setTextAppearance(ResUtils.getTextViewStyle(CommonUtils.getDefaultTheme()?.popupForm?.button?.bold!!))
                tvClose.setTextColor(Color.parseColor(CommonUtils.getDefaultTheme()?.popupForm?.button?.fgColor))
                tvTitle.setTextColor(Color.parseColor(CommonUtils.getDefaultTheme()?.popupForm?.button?.fgColor))

                tvTitle.textSize =
                    ResUtils.getTextSize(CommonUtils.getDefaultTheme()?.popupForm?.button!!)
                tvClose.textSize =
                    ResUtils.getTextSize(CommonUtils.getDefaultTheme()?.popupForm?.button!!)
                tvClose.setOnClickListener {
                    viewDataBinding?.layoutOverlay?.visibility = GONE
                    enableChooseOrganization(false)
                }
                initTabletBubbleViewToolbar()
            }
            isTabViewInitialized = true
        }
    }

    // Get Base64 string
    private fun getBase64(orgVal: String): String {
        return Base64.encodeToString(orgVal.toByteArray(charset("UTF-8")), Base64.NO_WRAP)
    }

    private fun initTabletBubbleViewToolbar() {
        if (popupToolbarColor.isNotEmpty()) {
            viewDataBinding?.layoutOrganization?.ivBackground?.setColorFilter(
                Color.parseColor(popupToolbarColor),
                PorterDuff.Mode.SRC_IN
            )
        }
    }

    private fun displayDialog(state: User?) {
        if (state != null) {
            if (isTablet()) {
                initTabletBubbleView()
                showOrganizationLayout(state)
            } else {
                with(OrganizationPicker) {
                    if (!isDialogRunning()) {
                        setOrganizationList(state.organizations ?: ArrayList())
                        openDialog(this@LoginActivity, popupToolbarColor, popupForm!!.fgColor!! , popupButtonForm ,
                            { organization -> onSelected(organization) }, {
                                //      enableChooseOrganization(false)
                                gotoMainActivity()
                            })
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppConstants.REQUEST_VALUE_URL_SETTINGS){
            if (resultCode == RESULT_OK){
                ApiClient.init(this@LoginActivity)
                viewModel.changeRepository()
            }
        }
    }

    private fun showOrganizationLayout(state: User?) {
        initRecyclerView()
        organizationAdapter.setItems(state!!.organizations ?: ArrayList())
        viewDataBinding?.layoutOverlay?.apply {
            visibility = if (visibility == VISIBLE) GONE else VISIBLE
        }
        organizationAdapter.listener = this

        initPopupToolbar()
    }

    private fun initPopupToolbar() {
        viewDataBinding?.layoutOrganization?.apply {
            tvClose.setTextColor(Color.parseColor(popupForm!!.fgColor))
            tvTitle.setTextColor(Color.parseColor(popupForm!!.fgColor))
            tvTitle.textSize = ResUtils.getTextSize(defaultTheme?.popupForm?.button!!)
            tvClose.textSize = ResUtils.getTextSize(defaultTheme?.popupForm?.button!!)
        }
    }

    private fun initRecyclerView() {
        viewDataBinding?.layoutOrganization?.recyclerView?.apply {
            layoutManager = LinearLayoutManager(this@LoginActivity)
            organizationAdapter = OrganizationAdapter()
            viewDataBinding?.layoutOrganization?.recyclerView?.adapter = organizationAdapter
        }
    }

    private fun setEdittextStyle(themeTextStyle: ThemeTextStyle) {
        viewDataBinding?.edittextUserName?.apply {
            setTextAppearance(ResUtils.getEdittextStyle(themeTextStyle.bold ?: false))
            setTextColor(Color.parseColor(themeTextStyle.fgColor))
            setHintTextColor(Color.parseColor(themeTextStyle.fgColor))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, ResUtils.getTextSize(themeTextStyle))
        }

        viewDataBinding?.edittextPassword?.apply {
            setTextAppearance(ResUtils.getEdittextStyle(themeTextStyle.bold ?: false))
            setTextColor(Color.parseColor(themeTextStyle.fgColor))
            setHintTextColor(Color.parseColor(themeTextStyle.fgColor))
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            setTextSize(TypedValue.COMPLEX_UNIT_SP, ResUtils.getTextSize(themeTextStyle))
        }
    }

    private fun enableChooseOrganization(enable: Boolean) {
        viewDataBinding?.apply {
            tvChooseOrganization.visibility = if (enable) VISIBLE else GONE
            ivLogin.visibility = if (enable) GONE else VISIBLE
        }
    }

    /**
     * Selected the organization
     */
    override fun onSelected(organization: Organization) {
        //   gotoMainActivity()
        viewModel.fetchSwitchOrganization(organization.id ?: "0")
    }

    /**
     * Go to Main page after switched organization
     */
    private fun gotoMainActivity() {
        OrganizationPicker.dismissDialog()
        Handler(Looper.getMainLooper()).postDelayed(
            { startMainActivity() }, 300
        )
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        overridePendingTransition(R.anim.fade_out, R.anim.fade_in)
        finish()
    }
}