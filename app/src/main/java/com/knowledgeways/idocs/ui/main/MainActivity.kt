package com.knowledgeways.idocs.ui.main

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.google.gson.JsonObject
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.base.BaseActivity
import com.knowledgeways.idocs.databinding.ActivityMainBinding
import com.knowledgeways.idocs.db.PreferenceManager
import com.knowledgeways.idocs.network.ResultWrapper
import com.knowledgeways.idocs.network.model.Box
import com.knowledgeways.idocs.network.model.DownloadStatus
import com.knowledgeways.idocs.network.model.DrawerItemColor
import com.knowledgeways.idocs.network.model.OutgoingResponse
import com.knowledgeways.idocs.network.model.theme.Theme
import com.knowledgeways.idocs.ui.component.dialog.DialogProgress
import com.knowledgeways.idocs.ui.component.dialog.DialogThemeDownloadProgress
import com.knowledgeways.idocs.ui.component.dialog.createNeditdocument.CreateNEditDocDialog
import com.knowledgeways.idocs.ui.component.dialog.createoption.CreateOptionDialog
import com.knowledgeways.idocs.ui.component.dialog.settings.SettingsDialog
import com.knowledgeways.idocs.ui.component.dialog.theme.ThemeManager
import com.knowledgeways.idocs.ui.login.LoginActivity
import com.knowledgeways.idocs.ui.main.adapter.box.BoxAdapter
import com.knowledgeways.idocs.ui.main.document.DocumentFragment
import com.knowledgeways.idocs.ui.main.search.SearchFragment
import com.knowledgeways.idocs.ui.main.searchresult.SearchResultFragment
import com.knowledgeways.idocs.utils.*
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import javax.inject.Inject
import kotlin.collections.ArrayList


class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>(),
    HasAndroidInjector, SettingsDialog.OnSelectedListener, ThemeManager.OnSelectedListener {

    override val layoutId: Int
        get() = R.layout.activity_main

    override val viewModel: MainViewModel
        get() {
            return getViewModel(MainViewModel::class.java)
        }

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    override fun androidInjector() = dispatchingAndroidInjector

    private lateinit var toggle: ActionBarDrawerToggle

    private val scope = MainScope()

    private var _drawerItemColorData = MutableLiveData<List<DrawerItemColor>>()
    private val drawerItemColorData: LiveData<List<DrawerItemColor>>
        get() = _drawerItemColorData

    private val colorList = ArrayList<DrawerItemColor>()
    lateinit var boxAdapter: BoxAdapter
    lateinit var defaultTheme: Theme
    var toolbarColor = ""
    var pdfToolbarColor = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FileUtils.createPDFFolder(this)
        initValues()
        initUI()
        initObservers()

        viewModel.initViewModel(this)
    }

    private fun initValues() {
        if (viewModel.getDefaultTheme() == null) finish()
        defaultTheme = viewModel.getDefaultTheme()!!
        SettingsDialog.setSelectedListener(this)
        ThemeManager.setSelectedListener(this)
    }

    private fun initUI() {
        initContentUI()
        initNavBarUI()
        initFloatingActionButton()
        addFragment(R.id.fragment, DocumentFragment())
    }

    private fun initFloatingActionButton() {
        viewDataBinding?.ivFloatingButton?.visibility =
            if (PreferenceManager.user?.privileges?.createInternalOutgoing == false && PreferenceManager.user?.privileges?.createExternalOutgoing == false)
                GONE else VISIBLE

        viewDataBinding?.ivFloatingButton?.setOnClickListener {
            val intArray: IntArray = intArrayOf(0, 0)
            viewDataBinding?.ivFloatingButton?.getLocationInWindow(intArray)
            showCreateNEditDialog(if (intArray.isNotEmpty()) intArray[0] else 0)
        }
    }

    private fun showCreateNEditDialog(position: Int) {
        if (isTablet()) {
            initCreateNEditDialog(position)
            showCreateNEditLayout()
        } else {
            CreateOptionDialog.openDialog(
                this@MainActivity,
                toolbarColor,
                defaultTheme.popupForm?.title?.fgColor ?: "",
                true,
                { callInternalOutgoing() },
                { callExternalOutgoing() })
            //  CreateNEditDocDialog.openDialog(this@MainActivity, toolbarColor, toolbarColor, true)
        }
    }

    fun callInternalOutgoing() {
        viewModel.internalOutgoing()
    }

    fun callExternalOutgoing() {
        viewModel.externalOutgoing()
    }

    private fun initCreateNEditDialog(position: Int) {
        viewDataBinding?.createOption?.apply {
            val params = triangle?.layoutParams as ConstraintLayout.LayoutParams
            params.setMargins(position, 0, 0, 0)
            triangle.layoutParams = params

            tvInternal.visibility =
                if (PreferenceManager.user?.privileges?.createInternalOutgoing == true) VISIBLE else GONE
            tvExternal.visibility =
                if (PreferenceManager.user?.privileges?.createExternalOutgoing == true) VISIBLE else GONE

            tvInternal.setOnClickListener {

            }

            tvExternal.setOnClickListener {

            }
        }
    }

    private fun showCreateNEditLayout() {
        viewDataBinding?.layoutCreateOption?.visibility = VISIBLE
    }

    private fun initContentUI() {
        initToggleButton()
        initToolbar()
        viewDataBinding?.tvTitle?.apply {
            setTextAppearance(ResUtils.getTextViewStyle(CommonUtils.getDefaultTheme()?.popupForm?.title?.bold!!))
            setTextColor(Color.parseColor(defaultTheme.popupForm?.title?.fgColor))
            textSize =
                ResUtils.getTextSize(CommonUtils.getDefaultTheme()?.popupForm?.title!!)
        }

        viewDataBinding?.tvTitleSearchResult?.apply {
            setTextAppearance(ResUtils.getTextViewStyle(CommonUtils.getDefaultTheme()?.popupForm?.title?.bold!!))
            setTextColor(Color.parseColor(defaultTheme.popupForm?.title?.fgColor))
            textSize =
                ResUtils.getTextSize(CommonUtils.getDefaultTheme()?.popupForm?.title!!)
        }
    }

    private fun initNavBarUI() {
        observeColorData()
        initColorArray()
        initNavHeader()
        initRecyclerView()
        initLayoutColors()
        initNavFooter()
    }

    private fun initColorArray() {
        colorList.clear()
        colorList.add(DrawerItemColor("", ""))
        colorList.add(DrawerItemColor("", ""))
        colorList.add(DrawerItemColor("", ""))

        _drawerItemColorData.value = colorList
    }

    private fun initNavHeader() {
        initLogo()
        initDateLayout()
    }

    private fun initLogo() {
        viewDataBinding?.navHeader?.apply {

            GlideApp.with(this@MainActivity).asBitmap()
                .load(
                    ResUtils.getNormalImageFromTheme(
                        this@MainActivity,
                        "upper-logo@2x.png"
                    )
                )
                .listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Bitmap>?,
                        isFirstResource: Boolean,
                    ): Boolean {

                        return false
                    }

                    override fun onResourceReady(
                        resource: Bitmap?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Bitmap>?,
                        dataSource: com.bumptech.glide.load.DataSource?,
                        isFirstResource: Boolean,
                    ): Boolean {
                        ivProfile.setImageBitmap(resource)
                        return false
                    }
                }).into(viewDataBinding?.ivLogoTemp!!)

            tvUserName.apply {
                text = """${PreferenceManager.prefix} ${PreferenceManager.fullName}"""
                setTextAppearance(
                    ResUtils.getTextViewStyle(
                        defaultTheme.userInfo?.username?.bold ?: false
                    )
                )
                textSize = ResUtils.getTextSize(defaultTheme.userInfo?.username?.fontSize!!)
                setTextColor(Color.parseColor(defaultTheme.userInfo?.username?.fgColor ?: ""))
            }

            tvOrganizationName.apply {
                text = viewModel.getSelectedOrganization()?.name
                setTextAppearance(
                    ResUtils.getTextViewStyle(
                        defaultTheme.userInfo?.organization?.bold ?: false
                    )
                )
                textSize = ResUtils.getTextSize(defaultTheme.userInfo?.organization?.fontSize!!)
                setTextColor(Color.parseColor(defaultTheme.userInfo?.organization?.fgColor ?: ""))
            }
        }
    }

    private fun initDateLayout() {
        viewDataBinding?.navHeader?.apply {
            ivDateBackground.setColorFilter(
                Color.parseColor(defaultTheme.dateInfo?.bgColor),
                PorterDuff.Mode.SRC_IN
            )
            GlideApp.with(this@MainActivity)
                .load(
                    ResUtils.getIconFromTheme(
                        this@MainActivity,
                        "calander.png"
                    )
                )
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(ivCalendar)

            tvDate.apply {
                setTextAppearance(ResUtils.getTextViewStyle(defaultTheme.dateInfo?.bold!!))
                textSize = ResUtils.getTextSize(defaultTheme.dateInfo?.fontSize!!)
                setTextColor(Color.parseColor(defaultTheme.dateInfo?.fgColor ?: ""))
                text = PreferenceManager.loggedInDate
            }
        }
    }

    private fun observeColorData() {
        drawerItemColorData.observe(this) { colorData ->
            checkColorData(colorData)
        }

    }

    private fun checkColorData(colorData: List<DrawerItemColor>) {
        var allColorFilled = true
        if (colorData.size != 3) allColorFilled = false
        colorData.forEach {
            if (CommonUtils.isColorItemEmpty(it)) {
                allColorFilled = false
                return
            }
        }

        if (allColorFilled) {
            boxAdapter.setColorList(drawerItemColorData.value!!)
        }
    }


    private fun initToggleButton() {
        viewDataBinding?.apply {
            toggle = object : ActionBarDrawerToggle(
                this@MainActivity,
                layoutDrawer,
                R.string.str_nav_bar_open,
                R.string.str_nav_bar_close
            ) {

                override fun onDrawerOpened(drawerView: View) {
                    super.onDrawerOpened(drawerView)
                }

                override fun onDrawerClosed(drawerView: View) {
                    super.onDrawerClosed(drawerView)
                }

                override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                    super.onDrawerSlide(drawerView, slideOffset)

                    if (isTablet()) { // If Device is Tablet, push the view to right
                        layoutContent.translationX = slideOffset * drawerView.width
                    }
                }
            }
            layoutDrawer.apply {
                addDrawerListener(toggle)
                setScrimColor(
                    ContextCompat.getColor(
                        this@MainActivity,
                        android.R.color.transparent
                    )
                )
            }
        }
    }

    private fun initToolbar() {
        viewDataBinding?.ivMenu?.setOnClickListener {
            openDrawer()
        }
    }

    private fun initRecyclerView() {
        if (!::boxAdapter.isInitialized) {
            boxAdapter = BoxAdapter(colorList) { box ->
                onBoxSelected(box)
            }
        }

        viewDataBinding?.navBody?.rvDrawerItems?.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            isNestedScrollingEnabled = false
            setHasFixedSize(true)
            adapter = boxAdapter
        }
    }

    private fun onBoxSelected(box: Box) {
        viewDataBinding?.tvTitleSearchResult?.visibility = GONE
        if (box.boxId == AppConstants.BOX_ID_SEARCH) {
            viewDataBinding?.tvTitle?.text = resources.getString(R.string.str_search)
            if (supportFragmentManager.findFragmentById(R.id.fragment) !is SearchFragment &&
                supportFragmentManager.findFragmentById(R.id.fragment) !is SearchResultFragment
            ) {
                addFragment(R.id.fragment, SearchFragment())
            } else if (supportFragmentManager.findFragmentById(R.id.fragment) is SearchResultFragment) {
                popFragment()
            }
        } else {
            viewModel.pageNumber = 0
            viewDataBinding?.tvTitle?.text = box.title ?: ""
            viewModel.selectedBox.value = box
            if (supportFragmentManager.findFragmentById(R.id.fragment) is SearchFragment
            ) {
                popFragment()
            } else if (supportFragmentManager.findFragmentById(R.id.fragment) is SearchResultFragment) {
                popFragment()
                popFragment()
            }
        }
    }

    private fun isDrawerOpened(): Boolean {
        return viewDataBinding?.layoutDrawer?.isDrawerOpen(GravityCompat.START) ?: false
    }

    private fun openDrawer() {
        viewDataBinding?.layoutDrawer?.openDrawer(GravityCompat.START, true)
    }

    private fun closeDrawer() {
        viewDataBinding?.layoutDrawer?.closeDrawer(GravityCompat.START, true)
    }

    private fun initObservers() {
        observeBoxData()
        observerLogoutResult()
        observeDownloadStatus()
        observerSyncDataResult()
        observeOutgoingData()
    }

    private fun observeOutgoingData() {
        viewModel.externalOutgoingLiveData.observe(this) { resultValue ->
            observeExternalOutgoingData(resultValue)
        }

        viewModel.internalOutgoingLiveData.observe(this) { resultValue ->
            observeInternalOutgoingData(resultValue)

        }
    }

    private fun observeInternalOutgoingData(data: ResultWrapper<OutgoingResponse>) {
        when (data) {
            is ResultWrapper.Success -> {
                viewDataBinding?.progressBar?.visibility = GONE
                CreateNEditDocDialog.openDialog(
                    this@MainActivity,
                    toolbarColor,
                    defaultTheme.popupForm?.title?.fgColor ?: "",
                    true,
                    isInternal = true
                )
            }

            is ResultWrapper.Loading -> {
                viewDataBinding?.progressBar?.visibility = VISIBLE
            }
            is ResultWrapper.GenericError -> {
                viewDataBinding?.progressBar?.visibility = GONE
            }

            else -> {
                viewDataBinding?.progressBar?.visibility = GONE
            }
        }

    }

    private fun observeExternalOutgoingData(data: ResultWrapper<OutgoingResponse>){
        when (data) {
            is ResultWrapper.Success -> {
                viewDataBinding?.progressBar?.visibility = GONE
                CreateNEditDocDialog.openDialog(
                    this@MainActivity,
                    toolbarColor,
                    defaultTheme.popupForm?.title?.fgColor ?: "",
                    true,
                    isInternal = false
                )
            }

            is ResultWrapper.Loading -> {
                viewDataBinding?.progressBar?.visibility = VISIBLE
            }
            is ResultWrapper.GenericError -> {
                viewDataBinding?.progressBar?.visibility = GONE
            }

            else -> {
                viewDataBinding?.progressBar?.visibility = GONE
            }
        }
    }

    private fun observerSyncDataResult() {
        viewModel.syncResultLiveData.observe(this) { resultValue ->
            if (resultValue == false) {
                DialogProgress.openDialog(this)
            } else if (resultValue) {
                if (DialogProgress.isDialogRunning()) {
                    DialogProgress.dismissDialog()
                }
                boxAdapter.setSelection(0)
            }
            //   viewDataBinding?.progressBar?.visibility = if (resultValue > 4) GONE else VISIBLE
        }
    }

    private fun observeBoxData() {
        viewModel.documentTotalLiveData.observe(this) { response ->
            viewModel.boxLiveData.observe(this) { boxList ->
                if (boxList.isNotEmpty()) {
                    setAdapter(boxList, response)
                }
            }
        }
    }

    private fun observerLogoutResult() {
        viewModel.logoutResult.observe(this) { result ->
            if (result.lowercase().contains("ok")) {
                gotoLoginActivity()
            }
        }
    }

    private fun setAdapter(boxList: List<Box>, jsonObject: JsonObject) {
        val selectedID = boxAdapter.getSelectedId()
        boxAdapter.setItems(boxList, jsonObject)
        if (selectedID == -1 && boxList.isNotEmpty()) {
            boxAdapter.setSelection(0)
            onBoxSelected(boxList[0])
        } else if (selectedID != -1) {
            boxAdapter.setSelection(selectedID)
            onBoxSelected(boxList[selectedID])
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (::toggle.isInitialized && toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initLayoutColors() {
        with(CommonUtils) {
            viewDataBinding?.apply {
                getColorFromImageView(
                    this@MainActivity,
                    ivDrawerBackground, "menubox-pattern@2x.png"
                ) { colorString ->
                    initDrawerBackgroundColor(colorString)
                }
                getColorFromImageView(
                    this@MainActivity,
                    ivDrawerUpperItemBackground, "menu-up-cell@2x.png"
                ) { colorString ->
                    initDeselectedItemColor(0, colorString)
                }

                getColorFromImageView(
                    this@MainActivity,
                    ivDrawerUpperItemSelectedBackground, "menu-up-cell-selected@2x.png"
                ) { colorString ->
                    initSelectedItemColor(0, colorString)
                }

                getColorFromImageView(
                    this@MainActivity,
                    ivDrawerMidItemBackground, "menu-mid-cell@2x.png"
                ) { colorString ->
                    initDeselectedItemColor(1, colorString)
                }

                getColorFromImageView(
                    this@MainActivity,
                    ivDrawerMidItemSelectedBackground, "menu-mid-cell-selected@2x.png"
                ) { colorString ->
                    initSelectedItemColor(1, colorString)
                }

                getColorFromImageView(
                    this@MainActivity,
                    ivDrawerLowItemBackground, "menu-bottom-cell@2x.png"
                ) { colorString ->
                    initDeselectedItemColor(2, colorString)
                }

                getColorFromImageView(
                    this@MainActivity,
                    ivDrawerLowItemSelectedBackground, "menu-bottom-cell-selected@2x.png"
                ) { colorString ->
                    initSelectedItemColor(2, colorString)
                }

                getColorFromImageView(
                    this@MainActivity,
                    ivToolbarColor, defaultTheme.popupForm!!.title!!.bgImg!!
                ) { colorString ->
                    toolbarColor = colorString
                    PreferenceManager.toolbarColor = colorString
                    viewDataBinding?.ivToolbarBackground?.setBackgroundColor(
                        Color.parseColor(
                            colorString
                        )
                    )
                }

                getColorFromImageView(
                    this@MainActivity,
                    ivBackgroundViewer, defaultTheme.viewer!!.titleBarBgImg!!
                ) { colorString ->
                    pdfToolbarColor = colorString
                }
            }
        }
    }

    private fun initDrawerBackgroundColor(colorString: String) {
        viewDataBinding?.layoutNav?.setBackgroundColor(Color.parseColor(colorString))
    }

    private fun initDeselectedItemColor(position: Int, colorString: String) {
        val color = colorList[position]
        color.colorDeselected = colorString
        colorList[position] = color

        _drawerItemColorData.value = colorList
    }

    private fun initSelectedItemColor(position: Int, colorString: String) {
        val color = colorList[position]
        color.colorSelected = colorString
        colorList[position] = color

        _drawerItemColorData.value = colorList
    }

    private fun initNavFooter() {
        viewDataBinding?.navFooter?.apply {
            GlideApp.with(this@MainActivity).asBitmap()
                .load(
                    ResUtils.getNormalImageFromTheme(
                        this@MainActivity,
                        defaultTheme.controlPanel?.others?.bgImg ?: ""
                    )
                )
                .listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Bitmap>?,
                        isFirstResource: Boolean,
                    ): Boolean {

                        return false
                    }

                    override fun onResourceReady(
                        resource: Bitmap?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Bitmap>?,
                        dataSource: com.bumptech.glide.load.DataSource?,
                        isFirstResource: Boolean,
                    ): Boolean {
                        ivBackground.setImageBitmap(resource)
                        return false
                    }
                }).into(viewDataBinding?.ivLogoutTempBackground!!)

            GlideApp.with(this@MainActivity).asBitmap()
                .load(
                    ResUtils.getNormalImageFromTheme(
                        this@MainActivity,
                        defaultTheme.controlPanel?.logout?.bgImg ?: ""
                    )
                )
                .listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Bitmap>?,
                        isFirstResource: Boolean,
                    ): Boolean {

                        return false
                    }

                    override fun onResourceReady(
                        resource: Bitmap?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Bitmap>?,
                        dataSource: com.bumptech.glide.load.DataSource?,
                        isFirstResource: Boolean,
                    ): Boolean {
                        ivBackgroundLogout.setImageBitmap(resource)
                        return false
                    }
                }).into(viewDataBinding?.ivLogoutBackgroundIcon!!)
        }

        initFooterIcons()
    }

    private fun initFooterIcons() {
        viewDataBinding?.navFooter?.apply {
            GlideApp.with(this@MainActivity)
                .load(
                    ResUtils.getNormalImageFromTheme(
                        this@MainActivity,
                        defaultTheme.controlPanel?.logout?.icon ?: ""
                    )
                )
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(ivLogout)

            ivLogout.setOnClickListener {
                viewModel.logout()
            }

            GlideApp.with(this@MainActivity)
                .load(
                    ResUtils.getNormalImageFromTheme(
                        this@MainActivity,
                        defaultTheme.controlPanel?.others?.refreshIcon ?: ""
                    )
                )
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(ivRefresh)

            ivRefresh.setOnClickListener {
                //    viewModel.getBoxes()
                //
                viewModel.getSyncData(true)
            }

            ivSettings.setOnClickListener {
                SettingsDialog.openDialog(
                    this@MainActivity,
                    toolbarColor,
                    defaultTheme.popupForm!!.title!!.fgColor ?: ""
                ) {

                }
            }

            GlideApp.with(this@MainActivity)
                .load(
                    ResUtils.getNormalImageFromTheme(
                        this@MainActivity,
                        defaultTheme.controlPanel?.others?.settingsIcon ?: ""
                    )
                )
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(ivSettings)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    private fun gotoLoginActivity() {
        startActivity(Intent(this, LoginActivity::class.java))
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }

    override fun onTempFileDeleted() {
        FileUtils.deleteAllPDF(this)
        PreferenceManager.documentList = ArrayList()
        viewModel.updateTempFile()
    }

    override fun onSyncUserDataSelected() {
        SettingsDialog.dismissDialog()
        viewModel.getSyncData(true)
    }

    override fun onThemeManagerSelected() {
        SettingsDialog.dismissDialog()
        ThemeManager.openDialog(
            this@MainActivity,
            toolbarColor,
            defaultTheme.popupForm?.title?.bgColor ?: ""
        ) {

        }
    }

    override fun onPageSizeSelected(pageSize: Int) {

    }

    override fun onNightModeSelected(boolean: Boolean) {

    }

    override fun onScrollingModeSelected(mode: Int) {

    }

    override fun onPageTurnOptionSelected(value: Int) {

    }

    override fun onPageFlippingSelected(value: Int) {

    }

    override fun onDeleteOfflineAction() {

    }

    override fun onDownloadClicked(theme: Theme) {
        //       ThemeManager.dismissDialog()
        viewModel.initializeProgressData()
        viewModel.downloadFileImages(this, theme.themeId ?: "", AppConstants.FILE_TYPE_IMAGE)

        DialogThemeDownloadProgress.openDialog(this@MainActivity) {
            /*    ThemeManager.openDialog(this@MainActivity,
                    toolbarColor,
                    defaultTheme.popupForm?.title?.bgColor ?: ""){
                }*/
            ThemeManager.resetThemeAdapter()
        }
    }

    override fun onDeleteClicked(theme: Theme) {
        FileUtils.deleteTheme(this, theme)
        ThemeManager.resetThemeAdapter()
    }

    override fun onCheckSelected(theme: Theme) {

    }

    override fun onEditSelected(theme: Theme) {
        val updatedThemeList = ArrayList<Theme>()
        val themeList = ConverterUtils.getThemeList() ?: ArrayList()
        for (mTheme in themeList) {
            if (mTheme.themeId == theme.themeId) {
                mTheme.default = true
                mTheme.mIsDefault = true
            } else {
                mTheme.default = false
                mTheme.mIsDefault = false
            }
            updatedThemeList.add(mTheme)
        }
        PreferenceManager.themeListString =
            ConverterUtils.convertThemeListToString(updatedThemeList)
        ThemeManager.resetThemeAdapter()
        initValues()
        initUI()
        viewModel._isThemeChanged.postValue((viewModel._isThemeChanged.value ?: 0) + 1)
        viewModel.getBoxes()
    }

    override fun onSettingsClicked() {
        SettingsDialog.openDialog(
            this@MainActivity,
            toolbarColor,
            defaultTheme.popupForm!!.title!!.fgColor ?: ""
        ) {
        }
    }

    private fun observeDownloadStatus() {
        viewModel.progressLiveData.observe(this) { state ->
            //    if (state.fileType == )
            if (viewModel.downloadingFile == 0) {
                updateDownloadStatus(state)
            }
        }
    }

    private fun updateDownloadStatus(status: DownloadStatus) {
        DialogThemeDownloadProgress.updateProgressStatus(status)
    }

    override
    fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v: View? = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm: InputMethodManager? =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                    imm?.hideSoftInputFromWindow(v.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onBackPressed() {
        if (isDrawerOpened()) {
            closeDrawer()
        } else {

            val imm =
                getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            if (imm.isAcceptingText) {
                KeyboardUtils.hideKeyboard(this)
            } else {
                if (supportFragmentManager.findFragmentById(R.id.fragment) is SearchFragment
                ) {
                    popFragment()
                } else if (supportFragmentManager.findFragmentById(R.id.fragment) is DocumentFragment) {
                    finish()
                }
            }
        }
    }

    fun onSearchClicked() {
        viewDataBinding?.tvTitleSearchResult?.visibility = VISIBLE
        addFragment(R.id.fragment, SearchResultFragment())
    }

}
