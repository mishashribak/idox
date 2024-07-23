package com.knowledgeways.idocs.ui.pdf

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PointF
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.annotation.Nullable
import androidx.annotation.UiThread
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.google.android.material.badge.BadgeDrawable
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.base.BaseActivity
import com.knowledgeways.idocs.databinding.ActivityCustomPdfactivityBinding
import com.knowledgeways.idocs.db.PreferenceManager
import com.knowledgeways.idocs.network.ResultWrapper
import com.knowledgeways.idocs.network.model.DocExternalUser
import com.knowledgeways.idocs.network.model.DownloadStatus
import com.knowledgeways.idocs.network.model.ExternalUser
import com.knowledgeways.idocs.network.model.Link
import com.knowledgeways.idocs.network.model.documentdetail.DocumentDetail
import com.knowledgeways.idocs.network.model.user.DocOrganization
import com.knowledgeways.idocs.network.model.user.Organization
import com.knowledgeways.idocs.ui.component.dialog.DialogOtp
import com.knowledgeways.idocs.ui.component.dialog.DialogPDFDownloadProgress
import com.knowledgeways.idocs.ui.component.dialog.PDFRejectReasonDialog
import com.knowledgeways.idocs.ui.component.dialog.forward.DocORGAdapter
import com.knowledgeways.idocs.ui.component.dialog.forward.DocSelectedOrgAdapter
import com.knowledgeways.idocs.ui.component.dialog.forward.DocSelectedUserAdapter
import com.knowledgeways.idocs.ui.component.dialog.forward.DocUserAdapter
import com.knowledgeways.idocs.ui.component.dialog.forward.PDFForwardDialog
import com.knowledgeways.idocs.ui.component.dialog.pdf.DialogMorePDFToolbar
import com.knowledgeways.idocs.ui.component.dialog.pdf.pdfnav.DialogMorePDFNavBar
import com.knowledgeways.idocs.ui.component.dialog.pdfattachments.PDFAttachmentDialog
import com.knowledgeways.idocs.ui.component.dialog.pdfdetails.PDFDetailDialog
import com.knowledgeways.idocs.ui.component.dialog.pdflinks.PDFLinksDialog
import com.knowledgeways.idocs.ui.component.dialog.pdfreply.PDFReplyDialog
import com.knowledgeways.idocs.ui.component.dialog.userNorgdetail.ActionsAdapter
import com.knowledgeways.idocs.ui.component.dialog.userNorgdetail.PriorityAdapter
import com.knowledgeways.idocs.utils.AppConstants
import com.knowledgeways.idocs.utils.AppConstants.BASE_URL
import com.knowledgeways.idocs.utils.CommonUtils
import com.knowledgeways.idocs.utils.ConverterUtils
import com.knowledgeways.idocs.utils.FileUtils
import com.knowledgeways.idocs.utils.KeyboardUtils
import com.knowledgeways.idocs.utils.MessageEvent
import com.knowledgeways.idocs.utils.PopupUtils
import com.knowledgeways.idocs.utils.ResUtils
import com.pspdfkit.annotations.Annotation
import com.pspdfkit.annotations.AnnotationFlags
import com.pspdfkit.annotations.AnnotationType
import com.pspdfkit.annotations.LinkAnnotation
import com.pspdfkit.annotations.actions.ActionType
import com.pspdfkit.annotations.actions.UriAction
import com.pspdfkit.configuration.PdfConfiguration
import com.pspdfkit.configuration.page.PageScrollDirection
import com.pspdfkit.document.PdfDocument
import com.pspdfkit.document.processor.PdfProcessor
import com.pspdfkit.document.processor.PdfProcessorTask
import com.pspdfkit.document.search.SearchResult
import com.pspdfkit.listeners.DocumentListener
import com.pspdfkit.listeners.OnDocumentLongPressListener
import com.pspdfkit.ui.PdfFragment
import com.pspdfkit.ui.drawable.PdfDrawable
import com.pspdfkit.ui.drawable.PdfDrawableProvider
import com.pspdfkit.ui.inspector.annotation.AnnotationCreationInspectorController
import com.pspdfkit.ui.inspector.annotation.AnnotationEditingInspectorController
import com.pspdfkit.ui.inspector.annotation.DefaultAnnotationCreationInspectorController
import com.pspdfkit.ui.inspector.annotation.DefaultAnnotationEditingInspectorController
import com.pspdfkit.ui.outline.DefaultBookmarkAdapter
import com.pspdfkit.ui.outline.DefaultOutlineViewListener
import com.pspdfkit.ui.search.SearchResultHighlighter
import com.pspdfkit.ui.search.SimpleSearchResultListener
import com.pspdfkit.ui.special_mode.controller.AnnotationCreationController
import com.pspdfkit.ui.special_mode.controller.AnnotationEditingController
import com.pspdfkit.ui.special_mode.controller.TextSelectionController
import com.pspdfkit.ui.special_mode.manager.AnnotationManager.OnAnnotationCreationModeChangeListener
import com.pspdfkit.ui.special_mode.manager.AnnotationManager.OnAnnotationEditingModeChangeListener
import com.pspdfkit.ui.special_mode.manager.TextSelectionManager
import com.pspdfkit.ui.toolbar.AnnotationCreationToolbar
import com.pspdfkit.ui.toolbar.AnnotationEditingToolbar
import com.pspdfkit.ui.toolbar.TextSelectionToolbar
import com.pspdfkit.utils.PdfUtils
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import kotlinx.coroutines.runBlocking
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.util.EnumSet
import javax.inject.Inject


class CustomPDFActivity : BaseActivity<ActivityCustomPdfactivityBinding, PDFViewModel>(),
    HasAndroidInjector, DocumentListener, OnDocumentLongPressListener,
    OnAnnotationCreationModeChangeListener, DialogMorePDFToolbar.OnSelectedListener,
    OnAnnotationEditingModeChangeListener, TextSelectionManager.OnTextSelectionModeChangeListener {

    override val layoutId: Int
        get() = R.layout.activity_custom_pdfactivity

    override val viewModel: PDFViewModel
        get() {
            return getViewModel(PDFViewModel::class.java)
        }

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    override fun androidInjector() = dispatchingAndroidInjector

    private var mDocumentDetail: DocumentDetail? = null
    private lateinit var config: PdfConfiguration
    private var toolbarColor = ""
    private var backgroundColor = ""
    private var rootLinkId = ""

    private lateinit var pdfLocation: Uri
    lateinit var customTestDrawableProvider: PdfDrawableProvider

    private lateinit var pdfFragment: PdfFragment

    private lateinit var highlighter: SearchResultHighlighter

    private var annotationCreationToolbar: AnnotationCreationToolbar? = null
    private var textSelectionToolbar: TextSelectionToolbar? = null
    private var annotationEditingToolbar: AnnotationEditingToolbar? = null

    private var annotationCreationActive = false

    private var annotationEditingInspectorController: AnnotationEditingInspectorController? = null
    private var annotationCreationInspectorController: AnnotationCreationInspectorController? = null
    private var fromAttachment = false
    private var mPdfDocument: PdfDocument? = null
    private var attachmentDocumentId = ""
    private var attachmentId = ""
    private var fromPDFList = false
    private var pdfSize = 0
    private var selectedPosition = -1
    val handler: Handler = Handler()

    var docId = "0"

    private var docOrgAdapter = DocORGAdapter( false,{ org ->
        initOrgDetailLayout(false,org)
        onOrgDetailClicked()
    }, {})


    var docUserAdapter = DocUserAdapter(false, { user ->
        initUserDetailLayout(false, user)
        onOrgDetailClicked()
    }, {})

    var favoriteOrgAdapter = DocORGAdapter(true,
        { org ->
            initOrgDetailLayout(true, org)
            onOrgDetailClicked()
        }, { org -> onDeleteOrgFromFavorite(org) }
    )


    var favoriteSisterOrgAdapter = DocORGAdapter ( true,
        {org ->
            initOrgDetailLayout(true, org)
            onOrgDetailClicked()},{org ->  onDeleteSisterOrgFromFavorite(org)}
    )

    var sisterOrgAdapter = DocORGAdapter ( false,
        { org ->
            initOrgDetailLayout(false, org)
            onOrgDetailClicked()
        },{}
    )

    var favoriteUserAdapter = DocUserAdapter (true,{ user ->
        initUserDetailLayout(true, user)
        onOrgDetailClicked()
    },{user->onDeleteUserFromFavorite(user)})

    var selectedOrgAdapter = DocSelectedOrgAdapter { organization ->
        onDeleteSelectedOrgClicked(organization)
    }

    var selectedSisterOrgAdapter = DocSelectedOrgAdapter { organization ->
        onDeleteSelectedSisterOrgClicked(organization)
    }

    var selectedUserAdapter = DocSelectedUserAdapter { organization ->
        onDeleteSelectedUserClicked(organization)
    }

    var priorityAdapter = PriorityAdapter()
    var actionsAdapter = ActionsAdapter()

    private var forwardLayoutPosition = 0

    private var screenHeight = 0

    private val imageUrls by lazy {
        listOf(
            ResUtils.getToolbarIcon(
                this@CustomPDFActivity,
                this@CustomPDFActivity.resources.getString(R.string.str_document_forward_favorite_icon)
            ),
            ResUtils.getToolbarIcon(
                this@CustomPDFActivity,
                this@CustomPDFActivity.resources.getString(R.string.str_document_forward_organizations_icon)
            ),
            ResUtils.getToolbarIcon(
                this@CustomPDFActivity,
                this@CustomPDFActivity.resources.getString(R.string.str_document_forward_employee_icon)
            ),
            ResUtils.getToolbarIcon(
                this@CustomPDFActivity,
                this@CustomPDFActivity.resources.getString(R.string.str_document_forward_sister_org_icon)
            ),
        )
    }
    val activeColor by lazy {
        ContextCompat.getColor(
            this@CustomPDFActivity,
            com.knowledgeways.idocs.R.color.color_text_bottom_bar
        )
    }

    val inactiveColor by lazy {
        ContextCompat.getColor(
            this@CustomPDFActivity,
            com.knowledgeways.idocs.R.color.color_grey_500
        )
    } // Replace with your inactive color resource }

    val colorStateList by lazy {
        ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked)
            ), intArrayOf(
                activeColor,
                inactiveColor
            )
        )
    }

    private var badgeOrg: BadgeDrawable? = null
    private var badgeSisterOrg: BadgeDrawable?= null
    private var badgeUser: BadgeDrawable?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        EventBus.getDefault().register(this)
        screenHeight = displayMetrics.heightPixels
        annotationCreationToolbar = AnnotationCreationToolbar(this)
        textSelectionToolbar = TextSelectionToolbar(this)
        annotationEditingToolbar = AnnotationEditingToolbar(this)

        // Use this if you want to use annotation inspector with annotation creation and editing
        // toolbars.
        annotationEditingInspectorController =
            DefaultAnnotationEditingInspectorController(
                this,
                viewDataBinding?.inspectorCoordinatorLayout!!
            )
        annotationCreationInspectorController =
            DefaultAnnotationCreationInspectorController(
                this,
                viewDataBinding?.inspectorCoordinatorLayout!!
            )

        extractArguments()
        initObservers()
        initUI()
    }

    private fun initObservers() {
        viewModel.apply {
            pdfHistory.observe(this@CustomPDFActivity) {
                observePDFHistory()
            }

            pdfMetaData.observe(this@CustomPDFActivity) {
                observePDFMetaData()
            }

            pdfLinks.observe(this@CustomPDFActivity) {
                observePDFLinks()
            }

            pdfAttachments.observe(this@CustomPDFActivity) {
                observePDFAttachments()
            }

            documentDetail.observe(this@CustomPDFActivity) { documentDetail ->
                observeDocumentDetail(documentDetail)
            }

            linkedPDFMetaData.observe(this@CustomPDFActivity) {
                observeLinkedPDFMetaData()
            }

            mDocumentDetail.observe(this@CustomPDFActivity) { documentDetail ->
                observeMDetail(documentDetail)
            }
            publishResult.observe(this@CustomPDFActivity) {
                observePublishDetail()
            }
            docUserResult.observe(this@CustomPDFActivity) {
                setFavoriteUserDataToView(docUserResult.value!!)
            }
            allDocUserResult.observe(this@CustomPDFActivity) {
                setUserDataToView(allDocUserResult.value!!)
            }
            sisterOrgResult.observe(this@CustomPDFActivity) {
                setFavoriteSisterDataToView(sisterOrgResult.value!!)
            }
            allSisterOrgResult.observe(this@CustomPDFActivity) {
                setSisterOrgDataToView(allSisterOrgResult.value!!)
            }
            orgListResult.observe(this@CustomPDFActivity) {
                setFavoriteOrgDataToView(orgListResult.value!!)
            }
            allOrgListResult.observe(this@CustomPDFActivity) {
                setOrgDataToView(allOrgListResult.value!!)
            }
            removeDataResult.observe(this@CustomPDFActivity){
                when(selectedDeletingElement){
                    1->{
                        updateFavoriteUserList()
                    }
                    2->{
                        updateFavoriteOrgList()
                    }
                    3->{
                        updateFavoriteSisterOrgList()
                    }
                }
            }
            addDataResult.observe(this@CustomPDFActivity){
                viewModel.getDocumentRelatedData(this@CustomPDFActivity.mDocumentDetail!!.documentId)
            }

            sendResult.observe(this@CustomPDFActivity){

            }

            unarchiveResult.observe(this@CustomPDFActivity){
                observeUnarchive(unarchiveResult.value?:"")
            }

            shareResult.observe(this@CustomPDFActivity){
                observeShareResult(shareResult.value?: "")
            }

            followPDFResult.observe(this@CustomPDFActivity){
                observeFollowupResult(followPDFResult.value ?:"")
            }

            favoriteMarkResult.observe(this@CustomPDFActivity){
                observeFavoriteResult(favoriteMarkResult.value ?:"")
            }

            markUnreadResult.observe(this@CustomPDFActivity){
                observeUnreadResult(markUnreadResult.value ?: "")
            }

            deleteDocumentResult.observe(this@CustomPDFActivity){
                observeDeleteResult(deleteDocumentResult.value ?: "")
            }

            trashResult.observe(this@CustomPDFActivity){
                observeTrashResult(trashResult.value ?: "")
            }

            unTrashResult.observe(this@CustomPDFActivity){
                observeUntrashResult(unTrashResult.value ?: "")
            }
        }
    }

    private fun observeMDetail(documentDetail: ResultWrapper<DocumentDetail>) {
        when (documentDetail) {
            is ResultWrapper.Loading -> {
                viewDataBinding?.layoutProgress?.visibility = VISIBLE
                //     viewDataBinding?.refreshLayout?.isRefreshing = true
            }

            is ResultWrapper.GenericError -> {
                viewDataBinding?.layoutProgress?.visibility = GONE
                if (documentDetail.code == 201) {
                    DialogOtp.openDialog(
                        this@CustomPDFActivity,
                        PreferenceManager.toolbarColor,
                        CommonUtils.getDefaultTheme()?.popupForm?.title?.bgColor ?: "",
                        documentDetail.error ?: ""
                    ) { mOtpCode ->
                        viewModel.verifyOTPForMDetail(mOtpCode)
                    }
                } else if (documentDetail.code == 202) {
                    if (DialogOtp.isDialogRunning()) {
                        DialogOtp.setErrorMessage(documentDetail.error ?: "")
                    }
                }
                //    viewDataBinding?.refreshLayout?.isRefreshing = false
            }

            is ResultWrapper.Success -> {
                viewDataBinding?.layoutProgress?.visibility = GONE
                mDocumentDetail = documentDetail.value
                initUI()
            }

            else -> {}
        }
    }

    private fun observePDFMetaData() {
        PDFDetailDialog.updateDetails(
            this,
            viewModel.pdfDetails!!,
            viewModel.pdfMetaData.value!!,
            viewModel.pdfMetaDataKeyList
        )
    }

    private fun observeLinkedPDFMetaData() {
        viewModel.getDocumentDetail(viewModel.linkedPDFDetails!!)
    }

    private fun observePDFHistory() {
        PDFDetailDialog.updateHistoryView(this, viewModel.pdfHistory.value!!)
    }

    private fun observePDFLinks() {
        PDFLinksDialog.setLinkList(viewModel.pdfLinks.value!!)
    }

    private fun observePDFAttachments() {
        val linkList = viewModel.pdfAttachments
        PDFAttachmentDialog.setLinkList(linkList.value!!)
    }

    private fun observeDocumentDetail(documentDetail: ResultWrapper<DocumentDetail>) {
        when (documentDetail) {
            is ResultWrapper.Loading -> {
                //     viewDataBinding?.refreshLayout?.isRefreshing = true
            }

            is ResultWrapper.GenericError -> {
                if (documentDetail.code == 201) {
                    DialogOtp.openDialog(
                        this@CustomPDFActivity,
                        PreferenceManager.toolbarColor,
                        CommonUtils.getDefaultTheme()?.popupForm?.title?.bgColor ?: "",
                        documentDetail.error ?: ""
                    ) { mOtpCode ->
                        viewModel.verityOTP(mOtpCode)
                    }
                } else if (documentDetail.code == 202) {
                    if (DialogOtp.isDialogRunning()) {
                        DialogOtp.setErrorMessage(documentDetail.error ?: "")
                    }
                }
                //    viewDataBinding?.refreshLayout?.isRefreshing = false
            }

            is ResultWrapper.Success -> {

                if (DialogOtp.isDialogRunning()) DialogOtp.dismissDialog()


                //     addOpenedDocument()
                openPDFAgain(documentDetail.value, false, "", "")
                if (PDFLinksDialog.isDialogRunning()) {
                    PDFLinksDialog.dismissDialog()
                }

            }

            else -> {}
        }
    }

    private fun observePublishDetail() {

    }

    fun openPDFAgain(
        documentDetail: DocumentDetail,
        fromAttachment: Boolean,
        attachmentDocumentId: String,
        attachmentId: String
    ) {
        val intent = Intent(this@CustomPDFActivity, CustomPDFActivity::class.java)
        intent.putExtra(AppConstants.KEYWORD_DOCUMENT_DETAIL, documentDetail)
        intent.putExtra(
            AppConstants.KEYWORD_TOOLBAR_COLOR,
            toolbarColor
        )
        intent.putExtra(
            AppConstants.KEYWORD_TOOLBAR_COLOR,
            toolbarColor
        )
        intent.putExtra(
            AppConstants.KEYWORD_BACKGROUND_COLOR,
            CommonUtils.getDefaultTheme()!!.viewer!!.bgColor
        )
        intent.putExtra(
            AppConstants.KEYWORD_ROOT_LINK_ID,
            viewModel.linkedPDFDetails?.documentId ?: ""
        )
        intent.putExtra(
            AppConstants.KEYWORD_FROM_ATTACHMENT,
            fromAttachment
        )
        intent.putExtra(
            AppConstants.KEYWORD_ATTACHMENT_DOCUMENT_ID,
            if (fromAttachment) attachmentDocumentId else ""
        )
        intent.putExtra(
            AppConstants.KEYWORD_ATTACHMENT_ID,
            if (fromAttachment) attachmentId else ""
        )
        startActivity(intent)
    }


    private fun extractArguments() {
        viewModel.initViewModel(this)
        mDocumentDetail = CommonUtils.getSerializable(
            this,
            AppConstants.KEYWORD_DOCUMENT_DETAIL,
            DocumentDetail::class.java
        )
        toolbarColor = intent.getStringExtra(AppConstants.KEYWORD_TOOLBAR_COLOR) ?: ""
        backgroundColor = intent.getStringExtra(AppConstants.KEYWORD_BACKGROUND_COLOR) ?: ""
        rootLinkId = intent.getStringExtra(AppConstants.KEYWORD_ROOT_LINK_ID) ?: ""
        fromAttachment = intent.getBooleanExtra(AppConstants.KEYWORD_FROM_ATTACHMENT, false)
        attachmentDocumentId =
            intent.getStringExtra(AppConstants.KEYWORD_ATTACHMENT_DOCUMENT_ID) ?: ""
        attachmentId = intent.getStringExtra(AppConstants.KEYWORD_ATTACHMENT_ID) ?: ""
        fromPDFList = intent.getBooleanExtra(AppConstants.KEYWORD_FROM_PDF_LIST, false)
        pdfSize = intent.getIntExtra(AppConstants.KEYWORD_PDF_SIZE, 0)
        selectedPosition = intent.getIntExtra(AppConstants.KEYWORD_SELECTED_POSITION, -1)
    }

    private fun initUI() {
        config = PdfConfiguration.Builder()
            .scrollDirection(PageScrollDirection.VERTICAL)
            .build()

        initPrevNextButton()

        initWaterMark()
        checkLocalStorage()
        observeDownloadStatus()
        initIcons()
        initTitle()

        initForwardNQuickFowrard()
    }

    private fun initPrevNextButton() {
        val mVisibility = if (fromPDFList) VISIBLE else GONE
        viewDataBinding?.apply {
            ivNext.visibility = mVisibility
            ivPrev.visibility = mVisibility
        }

        initClickableNextNPrevButton()
    }

    private fun disableNEnableButtons() {
        viewDataBinding?.apply {
            if (fromPDFList) {
                if (selectedPosition == 0) {
                    ivPrev.isClickable = false
                    ivPrev.isFocusable = false
                    ivNext.isClickable = true
                    ivNext.isFocusable = true

                } else if (pdfSize <= selectedPosition + 1) {
                    ivNext.isClickable = false
                    ivNext.isFocusable = false
                    ivPrev.isClickable = true
                    ivPrev.isFocusable = true
                }
            }
        }
    }


    private fun initClickableNextNPrevButton() {
        viewDataBinding?.apply {
            if (fromPDFList) {
                disableNEnableButtons()

                ivNext.setOnClickListener {
                    if (selectedPosition < AppConstants.mDocumentList.size - 1 && selectedPosition != -1) {
                        selectedPosition += 1
                        if (pdfSize > selectedPosition) {
                            viewModel.getDocumentDetail(AppConstants.mDocumentList[selectedPosition])
                        }
                        disableNEnableButtons()
                    }
                }

                ivPrev.setOnClickListener {
                    if (selectedPosition != -1 && selectedPosition > 0) {
                        selectedPosition -= 1
                        viewModel.getDocumentDetail(AppConstants.mDocumentList[selectedPosition])
                        disableNEnableButtons()
                    }
                }
            }
        }
    }

    fun checkLocalStorage() {
        if (FileUtils.isPDFExisting(
                this,
                if (fromAttachment) attachmentDocumentId else
                    mDocumentDetail!!.documentId,
                if (fromAttachment) attachmentId else
                    mDocumentDetail!!.transfer.transferId
            )
        ) {
            setFragment()
        } else {
            downloadAndOpenDocument()
        }
    }

    private fun downloadAndOpenDocument() {
        viewModel.downloadPDFFromServer(
            this,
            mDocumentDetail!!
        ) { fileLocation ->
            pdfLocation = fileLocation
        }

        viewModel.initializeProgressData()


        DialogPDFDownloadProgress.openDialog(this) {
            handler.postDelayed({ // Do something after 1s = 1000ms
                setFragment()
            }, 1000)
        }
    }

    private fun setFragment() {
        runBlocking {
            pdfFragment = createFragment(
                Uri.fromFile(
                    File(
                        FileUtils.getPDFFileName(
                            this@CustomPDFActivity,
                            mDocumentDetail!!.documentId!!,
                            mDocumentDetail!!.transfer.transferId!!
                        )
                    )
                )
            )

            initSearchButton()
            initOutlineView()
            initThumbnailView()

            pdfFragment.apply {
                addDocumentListener(this@CustomPDFActivity)
                addDocumentListener(viewDataBinding?.modularSearchView!!)
                addDocumentListener(viewDataBinding?.thumbnailGrid!!)
                addOnAnnotationCreationModeChangeListener(this@CustomPDFActivity)
                addOnAnnotationEditingModeChangeListener(this@CustomPDFActivity)
                addOnTextSelectionModeChangeListener(this@CustomPDFActivity)
                addDrawableProvider(customTestDrawableProvider)
            }
        }
    }

    private fun initWaterMark() {
        //   val drawables = Collections.singletonList(RectDrawable(RectF(0f, 100f, 100f, 0f)))
        with(mDocumentDetail!!) {
            val waterMarkString = watermark.text

            val rect = CustomDrawable(
                waterMarkString,
                ConverterUtils.spToPx(watermark.textSize.toFloat(), this@CustomPDFActivity).toInt()
            ).calculatePageRect(
                waterMarkString
            )
            val height = (rect.height() * 3)
            val width = (rect.width())

            // This is the drawable provider. You only need to implement a single method.
            customTestDrawableProvider = object : PdfDrawableProvider() {
                override fun getDrawablesForPage(
                    context: Context,
                    document: PdfDocument,
                    pageIndex: Int
                ): List<PdfDrawable> {
                    // Return the same drawable for every requested page.

                    val drawableList: MutableList<PdfDrawable> = ArrayList()

                    for (i in 0 until document.getPageSize(0).height.toInt() step height) {
                        for (j in 0 until document.getPageSize(0).width.toInt() step width) {
                            drawableList.add(
                                RectDrawable(
                                    waterMarkString,
                                    PointF(j.toFloat(), i.toFloat()),
                                    watermark.fgColor,
                                    ConverterUtils.spToPx(
                                        watermark.textSize.toFloat(),
                                        this@CustomPDFActivity
                                    ),
                                    0.7
                                )
                            )
                        }
                    }
                    return drawableList
                }
            }
        }

    }

    private fun createFragment(
        documentUri: Uri
    ): PdfFragment {
        val fragment = PdfFragment.newInstance(documentUri, config)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment, fragment)
            .commit()

        return fragment
    }

    private fun observeDownloadStatus() {
        viewModel.progressLiveData.observe(this) { state ->
            updateDownloadStatus(state)
        }
    }

    private fun updateDownloadStatus(status: DownloadStatus) {
        DialogPDFDownloadProgress.updateProgressStatus(status)
    }

    private fun initIcons() {
        initIconVisibility()
        initIconResources()
    }

    private fun initIconVisibility() {
        initToolbarIconVisibility()
        initBottomBarIconVisibility()
    }

    private fun initIconResources() {
        initToolbarIcons()
        initBottomBarIcons()
        initToolbarClickListeners()
    }

    private fun initToolbarIconVisibility() {
        with(mDocumentDetail!!.acl) {
            viewDataBinding?.layoutToolbar!!.apply {
                layoutBack.visibility =
                    if (rootLinkId.isEmpty() && !fromAttachment) GONE else VISIBLE
                layoutClose.visibility =
                    if (rootLinkId.isEmpty() && !fromAttachment) VISIBLE else GONE
                layoutDetails.visibility = if (documentInfo && !fromAttachment) VISIBLE else GONE
                layoutLinks.visibility = if (documentLinks && !fromAttachment) VISIBLE else GONE
                layoutAttachments.visibility =
                    if (documentAttachments && !fromAttachment) VISIBLE else GONE

                if (documentAttachments && !fromAttachment) {
                    tvAttachmentCount.apply {
                        if (mDocumentDetail!!.attachmentsTotal > 0) {
                            text = mDocumentDetail!!.attachmentsTotal.toString()
                            visibility = VISIBLE
                        } else visibility = GONE
                    }
                }

                if (documentLinks && !fromAttachment) {
                    tvLinkCount.apply {
                        if (mDocumentDetail!!.linksTotal > 0) {
                            text = mDocumentDetail!!.linksTotal.toString()
                            visibility = VISIBLE
                        } else visibility = GONE
                    }
                }

                layoutThumbnails?.visibility =
                    if (documentThumbnails && !fromAttachment) VISIBLE else GONE
                layoutPrint?.visibility = if (documentPrint && !fromAttachment) VISIBLE else GONE
                layoutBookmark?.visibility =
                    if (documentBookmarks && !fromAttachment) VISIBLE else GONE
                //       layoutSignature?.visibility = if (documentAnnotation && !fromAttachment) VISIBLE else GONE
                layoutSignature?.visibility =
                    if (documentAnnotation && !fromAttachment) GONE else GONE
                layoutSearch?.visibility = if (documentSearch && !fromAttachment) VISIBLE else GONE
                layoutMore?.visibility = if (!fromAttachment) VISIBLE else GONE
            }
        }
    }

    private fun initBottomBarIconVisibility() {
        with(mDocumentDetail!!.acl) {
            viewDataBinding?.layoutBottomBar!!.apply {
                layoutReply.visibility = if (documentReply) VISIBLE else GONE
                layoutArchive.visibility = if (documentArchive) VISIBLE else GONE
                layoutForward.visibility = if (documentStandardForward) VISIBLE else GONE
                layoutQuickForward.visibility = if (documentQuickForward) VISIBLE else GONE
                layoutPublish.visibility = if (documentPublish) VISIBLE else GONE
                layoutDocumentDcc.visibility = if (documentDCC) VISIBLE else GONE
                layoutSignReject.visibility = if (documentSignReject) VISIBLE else GONE
                layoutSignApprove.visibility = if (documentSignApprove) VISIBLE else GONE
            }
        }
    }

    private fun initToolbarClickListeners() {
        viewDataBinding?.layoutToolbar?.apply {
            layoutDetails.setOnClickListener {
                PDFDetailDialog.openDialog(this@CustomPDFActivity, PreferenceManager.toolbarColor,
                    { onDetailClicked() }, { onHistoryClicked() })
            }

            layoutLinks.setOnClickListener {
                viewModel.getLinks(
                    mDocumentDetail!!.boxId,
                    mDocumentDetail!!.transfer.documentId
                )
                PDFLinksDialog.openDialog(
                    this@CustomPDFActivity,
                    PreferenceManager.toolbarColor
                ) { link ->
                    onLinkItemClicked(link)
                }
            }

            layoutAttachments.setOnClickListener {
                viewModel.getAttachments(
                    mDocumentDetail!!.boxId!!,
                    mDocumentDetail!!.transfer!!.documentId!!
                )
                PDFAttachmentDialog.openDialog(
                    this@CustomPDFActivity,
                    PreferenceManager.toolbarColor
                ) { link ->
                    onAttachmentItemClicked(link)
                }
            }

            layoutSearch?.setOnClickListener {
                openSearch()
            }

            layoutThumbnails?.setOnClickListener {
                openThumbnailPage()
            }

            layoutBookmark?.setOnClickListener {
                openBookmarkDialog()
            }

            layoutSignature?.setOnClickListener {
                openSignatureDialog()
            }

            layoutPrint?.setOnClickListener {
                openPrintDialog()
            }
        }
    }

    private fun openSearch() {
        if (viewDataBinding?.outlineView?.isShown == true) {
            viewDataBinding?.outlineView?.hide()
        }
        if (viewDataBinding?.thumbnailGrid?.isShown == true) {
            viewDataBinding?.thumbnailGrid?.hide()
        }
        if (annotationCreationActive) {
            pdfFragment.exitCurrentlyActiveMode()
        }
        viewDataBinding?.modularSearchView?.show()
    }

    private fun openThumbnailPage() {

        if (viewDataBinding?.modularSearchView?.isShown == true) {
            viewDataBinding?.modularSearchView?.hide()
        }
        if (viewDataBinding?.outlineView?.isShown == true) {
            viewDataBinding?.outlineView?.hide()
        }
        if (annotationCreationActive) {
            pdfFragment.exitCurrentlyActiveMode()
        }
        viewDataBinding?.thumbnailGrid?.show()
    }

    private fun openBookmarkDialog() {
        if (viewDataBinding?.modularSearchView?.isShown == true) {
            viewDataBinding?.modularSearchView?.hide()
        }
        if (viewDataBinding?.thumbnailGrid?.isShown == true) {
            viewDataBinding?.thumbnailGrid?.hide()
        }
        if (annotationCreationActive) {
            pdfFragment.exitCurrentlyActiveMode()
        }
        viewDataBinding?.outlineView?.show()
    }

    private fun openSignatureDialog() {
        if (viewDataBinding?.modularSearchView?.isShown == true) {
            viewDataBinding?.modularSearchView?.hide()
        }
        if (viewDataBinding?.thumbnailGrid?.isShown == true) {
            viewDataBinding?.thumbnailGrid?.hide()
        }
        if (viewDataBinding?.outlineView?.isShown == true) {
            viewDataBinding?.outlineView?.hide()
        }
        if (annotationCreationActive) {
            pdfFragment.exitCurrentlyActiveMode()
        } else {
            pdfFragment.enterAnnotationCreationMode()
        }
    }

    private fun onLinkItemClicked(link: Link) {
        viewModel.getLinkedPDFMetaData(
            mDocumentDetail!!.boxId!!,
            link.destinationDocumentId,
            mDocumentDetail!!.documentId!!
        )
    }

    private fun onAttachmentItemClicked(link: Link) {
        PDFAttachmentDialog.dismissDialog()
        viewModel.downloadPDFAttachmentsFromServer(
            this@CustomPDFActivity,
            mDocumentDetail!!.boxId, link.documentId ?: "", link.attachmentId ?: ""
        ) {
            viewModel.downloadPDFAttachmentsFromServer(
                this@CustomPDFActivity,
                mDocumentDetail!!.boxId,
                link.documentId ?: "",
                link.attachmentId ?: ""
            ) { fileLocation ->
                pdfLocation = fileLocation
                openPDFAgain(
                    mDocumentDetail!!,
                    true,
                    link.documentId ?: "",
                    link.attachmentId ?: ""
                )
            }
        }
    }

    private fun onDetailClicked() {
        viewModel.getPDFMetaData(mDocumentDetail!!.boxId!!, mDocumentDetail!!.documentId!!)
    }

    private fun onHistoryClicked() {
        viewModel.getPDFHistory(
            mDocumentDetail!!.boxId!!,
            mDocumentDetail!!.documentId!!,
        )
    }

    private fun initToolbarIcons() {
        viewDataBinding?.layoutToolbar?.apply {

            layoutToolbar.setBackgroundColor(Color.parseColor(toolbarColor))

            with(CommonUtils) {
                loadIconToImageView(
                    this@CustomPDFActivity,
                    ResUtils.getToolbarIcon(
                        this@CustomPDFActivity, resources.getString(R.string.pdf_close_icon_name)
                    ),
                    ivClose
                )

                loadIconToImageView(
                    this@CustomPDFActivity,
                    ResUtils.getToolbarIcon(
                        this@CustomPDFActivity, resources.getString(R.string.pdf_details_icon_name)
                    ),
                    ivDetails
                )

                loadIconToImageView(
                    this@CustomPDFActivity,
                    ResUtils.getToolbarIcon(
                        this@CustomPDFActivity, resources.getString(R.string.pdf_links_icon_name)
                    ),
                    ivLinks
                )

                loadIconToImageView(
                    this@CustomPDFActivity,
                    ResUtils.getToolbarIcon(
                        this@CustomPDFActivity,
                        resources.getString(R.string.pdf_attachment_icon_name)
                    ),
                    ivAttachments
                )

                loadIconToImageView(
                    this@CustomPDFActivity,
                    ResUtils.getToolbarIcon(
                        this@CustomPDFActivity, resources.getString(R.string.pdf_search_icon_name)
                    ),
                    ivSearch
                )

                loadIconToImageView(
                    this@CustomPDFActivity,
                    ResUtils.getToolbarIcon(
                        this@CustomPDFActivity,
                        resources.getString(R.string.pdf_signature_icon_name)
                    ),
                    ivSignature
                )

                loadIconToImageView(
                    this@CustomPDFActivity,
                    ResUtils.getToolbarIcon(
                        this@CustomPDFActivity, resources.getString(R.string.pdf_bookmark_icon_name)
                    ),
                    ivBookmark
                )

                loadIconToImageView(
                    this@CustomPDFActivity,
                    ResUtils.getToolbarIcon(
                        this@CustomPDFActivity, resources.getString(R.string.pdf_print_icon_name)
                    ),
                    ivPrint
                )

                loadIconToImageView(
                    this@CustomPDFActivity,
                    ResUtils.getToolbarIcon(
                        this@CustomPDFActivity,
                        resources.getString(R.string.pdf_thumbnails_icon_name)
                    ),
                    ivThumbnails
                )

                loadIconToImageView(
                    this@CustomPDFActivity,
                    ResUtils.getToolbarIcon(
                        this@CustomPDFActivity, resources.getString(R.string.pdf_more_icon_name)
                    ),
                    ivMore
                )
            }

            layoutMore?.setOnClickListener {
                DialogMorePDFToolbar.openDialog(
                    this@CustomPDFActivity,
                    mDocumentDetail!!
                )
            }

            DialogMorePDFToolbar.setSelectedListener(this@CustomPDFActivity)
            layoutClose.setOnClickListener { finish() }
            layoutBack.setOnClickListener { finish() }
        }
    }

    private fun initBottomBarIcons() {
        viewDataBinding?.layoutBottomBar?.apply {
            with(CommonUtils) {
                loadIconToImageView(
                    this@CustomPDFActivity,
                    ResUtils.getToolbarIcon(
                        this@CustomPDFActivity,
                        resources.getString(R.string.pdf_document_reply_icon_name)
                    ),
                    ivReply
                )

                loadIconToImageView(
                    this@CustomPDFActivity,
                    ResUtils.getToolbarIcon(
                        this@CustomPDFActivity,
                        resources.getString(R.string.pdf_document_archive_icon_name)
                    ),
                    ivArchive
                )

                loadIconToImageView(
                    this@CustomPDFActivity,
                    ResUtils.getToolbarIcon(
                        this@CustomPDFActivity,
                        resources.getString(R.string.pdf_document_standard_forward_icon_name)
                    ),
                    ivForward
                )

                loadIconToImageView(
                    this@CustomPDFActivity,
                    ResUtils.getToolbarIcon(
                        this@CustomPDFActivity,
                        resources.getString(R.string.pdf_document_quick_forward_icon_name)
                    ),
                    ivQuickForward
                )

                loadIconToImageView(
                    this@CustomPDFActivity,
                    ResUtils.getToolbarIcon(
                        this@CustomPDFActivity,
                        resources.getString(R.string.pdf_document_publish_icon_name)
                    ),
                    ivPublish
                )

                loadIconToImageView(
                    this@CustomPDFActivity,
                    ResUtils.getToolbarIcon(
                        this@CustomPDFActivity,
                        resources.getString(R.string.pdf_document_publish_icon_name)
                    ),
                    ivDcc
                )

                loadIconToImageView(
                    this@CustomPDFActivity,
                    ResUtils.getToolbarIcon(
                        this@CustomPDFActivity,
                        resources.getString(R.string.pdf_document_sign_reject_icon_name)
                    ),
                    ivSignReject
                )

                loadIconToImageView(
                    this@CustomPDFActivity,
                    ResUtils.getToolbarIcon(
                        this@CustomPDFActivity,
                        resources.getString(R.string.pdf_document_sign_approve_icon_name)
                    ),
                    ivSignApprove
                )

                loadIconToImageView(
                    this@CustomPDFActivity,
                    ResUtils.getToolbarIcon(
                        this@CustomPDFActivity,
                        resources.getString(R.string.pdf_signature_icon_name)
                    ),
                    ivSignature
                )

                loadIconToImageView(
                    this@CustomPDFActivity,
                    ResUtils.getToolbarIcon(
                        this@CustomPDFActivity, resources.getString(R.string.pdf_more_icon_name)
                    ),
                    ivMore
                )
            }

            layoutMore.setOnClickListener {
                DialogMorePDFNavBar.openDialog(this@CustomPDFActivity, mDocumentDetail!!)
            }

            layoutSignature.setOnClickListener {
                openSignatureDialog()
            }

            layoutReply.setOnClickListener {
                val intArray: IntArray = intArrayOf(0, 0)
                layoutReply.getLocationInWindow(intArray)
                showReplyDialog(if (intArray.isNotEmpty()) intArray[0] else 0)
            }

            layoutSignReject.setOnClickListener {
                val intArray: IntArray = intArrayOf(0, 0)
                layoutSignReject.getLocationInWindow(intArray)
                showRejectDialog(if (intArray.isNotEmpty()) intArray[0] else 0)
            }

            layoutForward.setOnClickListener {
                val intArray: IntArray = intArrayOf(0, 0)
                layoutForward.getLocationInWindow(intArray)
                showForwardDialog(if (intArray.isNotEmpty()) intArray[0] else 0, false)
            }

            layoutQuickForward.setOnClickListener {
                val intArray: IntArray = intArrayOf(0, 0)
                layoutQuickForward.getLocationInWindow(intArray)
                showForwardDialog(if (intArray.isNotEmpty()) intArray[0] else 0, true)
            }

            layoutSignApprove.setOnClickListener {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this@CustomPDFActivity)
                builder.setTitle("") // Set the dialog title
                    .setMessage(this@CustomPDFActivity.resources.getString(R.string.str_you_want_sign_doc)) // Set the dialog message
                    .setPositiveButton(
                        "Yes"
                    ) { dialog, which ->
                        // Perform action for Option 1
                    }
                    .setNegativeButton(
                        "No"
                    ) { dialog, which ->
                        // Perform action for Option 2
                    }
                    .show()
            }

            layoutPublish.setOnClickListener {
                viewModel.publishPDF(
                    mDocumentDetail!!.boxId,
                    mDocumentDetail!!.transfer.documentId,
                    mDocumentDetail!!.transfer.transferId,
                    File(
                        FileUtils.getPDFFileName(
                            this@CustomPDFActivity,
                            mDocumentDetail!!.transfer.documentId,
                            mDocumentDetail!!.transfer.transferId
                        )
                    )
                )
            }

            layoutArchive.setOnClickListener {
                viewModel.publishPDF(
                    mDocumentDetail!!.boxId,
                    mDocumentDetail!!.transfer.documentId,
                    mDocumentDetail!!.transfer.transferId,
                    File(
                        FileUtils.getPDFFileName(
                            this@CustomPDFActivity,
                            mDocumentDetail!!.documentId!!,
                            mDocumentDetail!!.transfer.transferId!!
                        )
                    )
                )
            }
        }
    }

    private fun showReplyDialog(position: Int) {
        if (isTablet()) {
            initReplyLayout(position)
            showReplyLayout()
        } else {
            PDFReplyDialog.openDialog(this@CustomPDFActivity, mDocumentDetail?.transfer) {

            }
        }
        hasDocumentAnnotations()
        saveAllAnnotations()
    }

    private fun showRejectDialog(position: Int) {
        if (isTablet()) {
            initRejectReasonLayout(position)
            showRejectLayout()
        } else {
            PDFRejectReasonDialog.openDialog(this@CustomPDFActivity)
        }
    }

    private fun showForwardDialog(position: Int, isQuickForward: Boolean) {
        docId = mDocumentDetail?.documentId ?: "0"
        viewModel.getDocumentRelatedData(mDocumentDetail?.documentId ?: "0")
        if (isTablet()) {
            initForwardLayout(position, isQuickForward)
            showForwardLayout()
        } else {
            PDFForwardDialog.openDialog(
                this@CustomPDFActivity,
                imageUrls,
                colorStateList,
                isQuickForward,
                { org -> onAddOrgToSelectedList(org) },
                { org -> onAddOrgToFavoriteClicked(org) }
            )
            setSelectedDataToView()
        }
    }

    private fun showReplyLayout() {
        viewDataBinding?.layoutOverlayReplyPdf?.visibility = VISIBLE
    }

    private fun initReplyLayout(position: Int) {
        viewDataBinding?.layoutReplyPdf?.apply {
            val params = triangle?.layoutParams as LayoutParams

            params.setMargins(position, 0, 0, 0)
            triangle.layoutParams = params

            layoutOutside?.setOnClickListener {
                KeyboardUtils.hideKeyboard(this@CustomPDFActivity)
                viewDataBinding?.layoutOverlayReplyPdf?.visibility = GONE
            }

            tvTo?.text = mDocumentDetail?.transfer?.replyName ?: ""
            layoutReply.setOnClickListener {
                KeyboardUtils.hideKeyboard(this@CustomPDFActivity)
                viewDataBinding?.layoutOverlayReplyPdf?.visibility = GONE
            }
        }
    }

    private fun showRejectLayout() {
        viewDataBinding?.layoutOverlayRejectReason?.visibility = VISIBLE
    }

    private fun initRejectReasonLayout(position: Int) {

        viewDataBinding?.layoutRejectReason?.apply {
            val params = triangle?.layoutParams as LayoutParams

            params.setMargins(position, 0, 0, 0)
            triangle.layoutParams = params

            layoutOutside?.setOnClickListener {
                KeyboardUtils.hideKeyboard(this@CustomPDFActivity)
                viewDataBinding?.layoutOverlayRejectReason?.visibility = GONE
            }

            layoutReject?.setOnClickListener {
                KeyboardUtils.hideKeyboard(this@CustomPDFActivity)
                viewDataBinding?.layoutOverlayRejectReason?.visibility = GONE
            }
        }
    }

    private fun showForwardLayout() {
        viewDataBinding?.layoutOverlayForward?.visibility = VISIBLE
    }

    private fun initForwardLayout(position: Int, isQuickForward: Boolean) {
        viewDataBinding?.layoutForward?.apply {
            val params = triangle?.layoutParams as LayoutParams

            forwardLayoutPosition = position

            params.setMargins(position, 0, 0, 0)
            triangle.layoutParams = params

            layoutOutside?.setOnClickListener {
                KeyboardUtils.hideKeyboard(this@CustomPDFActivity)
                viewDataBinding?.layoutOverlayForward?.visibility = GONE
            }

            bottomNavBar?.itemTextColor = colorStateList
            bottomNavBar?.itemIconTintList = colorStateList

            if (PreferenceManager.toolbarColor.isNotEmpty()) {
                val colorStateList =
                    ColorStateList.valueOf(Color.parseColor(PreferenceManager.toolbarColor))
                layoutToolbar?.backgroundTintList = colorStateList
            }
            tvClose?.setTextAppearance(ResUtils.getTextViewStyle(CommonUtils.getDefaultTheme()?.popupForm?.button?.bold!!))
            tvClose?.setTextColor(Color.parseColor(CommonUtils.getDefaultTheme()?.popupForm?.button?.fgColor))
            tvTitle?.setTextAppearance(ResUtils.getTextViewStyle(CommonUtils.getDefaultTheme()?.popupForm?.button?.bold!!))
            tvTitle?.setTextColor(Color.parseColor(CommonUtils.getDefaultTheme()?.popupForm?.button?.fgColor))
            tvTitle?.textSize =
                ResUtils.getTextSize(CommonUtils.getDefaultTheme()?.popupForm?.button!!)
            tvClose?.textSize =
                ResUtils.getTextSize(CommonUtils.getDefaultTheme()?.popupForm?.button!!)

            tvClose?.setOnClickListener {
                viewDataBinding?.layoutOverlayForward?.visibility = GONE
            }

            tvSend?.setOnClickListener {
                sendDocument()
            }

            bottomNavBar?.itemTextColor = colorStateList
            bottomNavBar?.itemIconTintList = colorStateList

            for (i in 0 until bottomNavBar?.menu!!.size()) {
                val menuItem: MenuItem = bottomNavBar.menu.getItem(i)
                val imageUrl = imageUrls[i]

                // Load the image using Glide
                Glide.with(this@CustomPDFActivity)
                    .load(imageUrl)
                    .into(object : CustomTarget<Drawable?>() {
                        override fun onResourceReady(
                            resource: Drawable,
                            transition: com.bumptech.glide.request.transition.Transition<in Drawable?>?
                        ) {
                            menuItem.icon = resource
                        }

                        override fun onLoadCleared(@Nullable placeholder: Drawable?) {
                            // This method can be left empty
                        }
                    })
            }

            bottomNavBar.menu.getItem(2).isVisible = !isQuickForward
            bottomNavBar.menu.getItem(3).isVisible = !isQuickForward

            bottomNavBar.setOnNavigationItemSelectedListener { item -> // Handle the item selection
                when (item.itemId) {
                    R.id.favorite -> {
                        layoutOverlayFavorite?.visibility = View.VISIBLE
                        layoutOverlayEmployees?.visibility = View.GONE
                        layoutOverlayOrganizations?.visibility = View.GONE
                        layoutOverlaySisterOrganizations?.visibility = View.GONE
                    }

                    R.id.organization -> {
                        layoutOverlayFavorite?.visibility = View.GONE
                        layoutOverlayEmployees?.visibility = View.GONE
                        layoutOverlayOrganizations?.visibility = View.VISIBLE
                        layoutOverlaySisterOrganizations?.visibility = View.GONE
                    }

                    R.id.employee -> {
                        layoutOverlayFavorite?.visibility = View.GONE
                        layoutOverlayEmployees?.visibility = View.VISIBLE
                        layoutOverlayOrganizations?.visibility = View.GONE
                        layoutOverlaySisterOrganizations?.visibility = View.GONE
                    }

                    R.id.sister_organization -> {
                        layoutOverlayFavorite?.visibility = View.GONE
                        layoutOverlayEmployees?.visibility = View.GONE
                        layoutOverlayOrganizations?.visibility = View.GONE
                        layoutOverlaySisterOrganizations?.visibility = View.VISIBLE
                    }
                }
                // Return true to indicate that the item selection has been handled
                true
            }
            badgeOrg = bottomNavBar.getOrCreateBadge(R.id.organization)
            badgeOrg?.backgroundColor =
                ContextCompat.getColor(this@CustomPDFActivity, R.color.color_red)
            badgeOrg?.isVisible = true

            layoutOrganizations?.apply {

                rvSelected.apply {
                    adapter = selectedOrgAdapter
                    layoutManager = LinearLayoutManager(context)
                    isNestedScrollingEnabled = false
                    setHasFixedSize(true)
                }

                recyclerView.apply {
                    adapter = docOrgAdapter
                    layoutManager = LinearLayoutManager(context)
                    isNestedScrollingEnabled = false
                    setHasFixedSize(true)
                }
            }

            layoutFavorite?.apply {
                listEmployee.apply {
                    adapter = favoriteUserAdapter
                    layoutManager = LinearLayoutManager(context)
                    isNestedScrollingEnabled = false
                    setHasFixedSize(true)
                }

                listOrg.apply {
                    adapter = favoriteOrgAdapter
                    layoutManager = LinearLayoutManager(context)
                    isNestedScrollingEnabled = false
                    setHasFixedSize(true)
                }

                listSisterOrg.apply {
                    adapter = favoriteSisterOrgAdapter
                    layoutManager = LinearLayoutManager(context)
                    isNestedScrollingEnabled = false
                    setHasFixedSize(true)
                }
            }

            layoutSisterOrganizations?.apply {

                badgeSisterOrg = bottomNavBar.getOrCreateBadge(R.id.sister_organization)
                badgeSisterOrg?.backgroundColor =
                    ContextCompat.getColor(this@CustomPDFActivity, R.color.color_red)
                badgeSisterOrg?.isVisible = true

                rvSelected.apply {
                    adapter = selectedSisterOrgAdapter
                    layoutManager = LinearLayoutManager(context)
                    isNestedScrollingEnabled = false
                    setHasFixedSize(true)
                }

                recyclerView.apply {
                    adapter = sisterOrgAdapter
                    layoutManager = LinearLayoutManager(context)
                    isNestedScrollingEnabled = false
                    setHasFixedSize(true)
                }
            }

            layoutEmployees?.apply {

                badgeUser = bottomNavBar.getOrCreateBadge(R.id.employee)
                badgeUser?.backgroundColor =
                    ContextCompat.getColor(this@CustomPDFActivity, R.color.color_red)
                badgeUser?.isVisible = true

                rvSelected.apply {
                    adapter = selectedUserAdapter
                    layoutManager = LinearLayoutManager(context)
                    isNestedScrollingEnabled = false
                    setHasFixedSize(true)
                }

                recyclerView.apply {
                    adapter = docUserAdapter
                    layoutManager = LinearLayoutManager(context)
                    isNestedScrollingEnabled = false
                    setHasFixedSize(true)
                }
            }

            tvClose?.setOnClickListener {
                viewDataBinding?.layoutOverlayForward?.visibility = GONE
            }
        }
    }

    private fun initTitle() {
        "doc-${mDocumentDetail!!.documentId}-${mDocumentDetail!!.transfer!!.transferId}".also {
            viewDataBinding?.layoutToolbar?.tvTitle?.text = it
            viewDataBinding?.tvTitle?.text = it
        }
    }

    override fun onDocumentLongPress(
        document: PdfDocument,
        pageIndex: Int,
        event: MotionEvent?,
        pagePosition: PointF?,
        longPressedAnnotation: Annotation?
    ): Boolean {
        // This code showcases how to handle long click gesture on the document links.
        pdfFragment.view?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)

        if (longPressedAnnotation is LinkAnnotation) {
            val action = longPressedAnnotation.action
            if (action?.type == ActionType.URI) {
                val uri = (action as UriAction).uri ?: return true
                //      Toast.makeText(this@CustomPDFActivity, uri, Toast.LENGTH_LONG).show()
                return true
            }
        }
        return false
    }


    @UiThread
    override fun onDocumentLoaded(document: PdfDocument) {
        mPdfDocument = document
        pdfFragment.addDocumentListener(viewDataBinding?.modularSearchView!!)
        viewDataBinding?.modularSearchView?.setDocument(document, config)
        viewDataBinding?.outlineView?.setDocument(document, config)
        viewDataBinding?.thumbnailGrid?.setDocument(document, config)
    }

    override fun onBackPressed() {
        when {
            viewDataBinding?.modularSearchView?.isShown == true -> {
                viewDataBinding?.modularSearchView?.hide()
                return
            }

            viewDataBinding?.outlineView?.isShown == true -> {
                viewDataBinding?.outlineView?.hide()
                return
            }

            viewDataBinding?.thumbnailGrid?.isShown == true -> {
                viewDataBinding?.thumbnailGrid?.hide()
                return
            }

            else -> super.onBackPressed()
        }
    }

    private fun initSearchButton() {
        // The search result highlighter will highlight any selected result.
        highlighter = SearchResultHighlighter(this).also {
            pdfFragment.addDrawableProvider(it)
        }


        viewDataBinding?.modularSearchView?.setSearchViewListener(object :
            SimpleSearchResultListener() {
            override fun onMoreSearchResults(results: List<SearchResult>) {
                highlighter.addSearchResults(results)
            }

            override fun onSearchCleared() {
                highlighter.clearSearchResults()
            }

            override fun onSearchResultSelected(result: SearchResult?) {
                // Pass on the search result to the highlighter. If 'null' the highlighter will clear any selection.
                highlighter.setSelectedSearchResult(result)
                if (result != null) {
                    pdfFragment.scrollTo(
                        PdfUtils.createPdfRectUnion(result.textBlock.pageRects),
                        result.pageIndex,
                        250,
                        false
                    )
                }
            }
        })
    }

    private fun initOutlineView() {
        viewDataBinding?.outlineView?.apply {
            val outlineViewListener = DefaultOutlineViewListener(pdfFragment)
            setOnAnnotationTapListener(outlineViewListener)
            setOnOutlineElementTapListener(outlineViewListener)
            setBookmarkAdapter(DefaultBookmarkAdapter(pdfFragment))
        }
    }

    private fun initThumbnailView() {
        viewDataBinding?.thumbnailGrid?.apply {
            setOnPageClickListener { view, pageIndex ->
                pdfFragment.pageIndex = pageIndex
                view.hide()
            }
        }
    }

    override fun onEnterAnnotationCreationMode(controller: AnnotationCreationController) {
        // When entering the annotation creation mode we bind the creation inspector to the provided
        // controller.
        // Controller handles request for toggling annotation inspector.

        // When entering the annotation creation mode we bind the creation inspector to the provided
        // controller.
        // Controller handles request for toggling annotation inspector.
        annotationCreationInspectorController!!.bindAnnotationCreationController(controller)


        // When entering the annotation creation mode we bind the toolbar to the provided
        // controller, and
        // issue the coordinator layout to animate the toolbar in place.
        // Whenever the user presses an action, the toolbar forwards this command to the controller.
        // Instead of using the `AnnotationEditingToolbar` you could use a custom UI that operates
        // on the controller.
        // Same principle is used on all other toolbars.

        // When entering the annotation creation mode we bind the toolbar to the provided
        // controller, and
        // issue the coordinator layout to animate the toolbar in place.
        // Whenever the user presses an action, the toolbar forwards this command to the controller.
        // Instead of using the `AnnotationEditingToolbar` you could use a custom UI that operates
        // on the controller.
        // Same principle is used on all other toolbars.
        annotationCreationToolbar!!.bindController(controller)
        viewDataBinding?.toolbarCoordinatorLayout?.displayContextualToolbar(
            annotationCreationToolbar!!,
            true
        )


        annotationCreationActive = true
    }

    override fun onChangeAnnotationCreationMode(p0: AnnotationCreationController) {


    }

    override fun onExitAnnotationCreationMode(controller: AnnotationCreationController) {

        // Once we're done with editing, unbind the controller from the toolbar, and remove it from
        // the
        // toolbar coordinator layout (with animation in this case).
        // Same principle is used on all other toolbars.
        viewDataBinding?.toolbarCoordinatorLayout?.removeContextualToolbar(true)
        annotationCreationToolbar!!.unbindController()
        annotationCreationActive = false

        // Also unbind the annotation creation controller from the inspector controller.

        // Also unbind the annotation creation controller from the inspector controller.
        annotationCreationInspectorController!!.unbindAnnotationCreationController()
    }

    override fun onEnterAnnotationEditingMode(controller: AnnotationEditingController) {
        annotationEditingInspectorController!!.bindAnnotationEditingController(controller)

        annotationEditingToolbar!!.bindController(controller)
        viewDataBinding?.toolbarCoordinatorLayout?.displayContextualToolbar(
            annotationEditingToolbar!!,
            true
        )

    }

    override fun onChangeAnnotationEditingMode(p0: AnnotationEditingController) {
    }

    override fun onExitAnnotationEditingMode(p0: AnnotationEditingController) {
        viewDataBinding?.toolbarCoordinatorLayout?.removeContextualToolbar(true)
        annotationEditingToolbar!!.unbindController()

        annotationEditingInspectorController!!.unbindAnnotationEditingController()
    }

    override fun onEnterTextSelectionMode(controller: TextSelectionController) {
        textSelectionToolbar!!.bindController(controller)
        viewDataBinding?.toolbarCoordinatorLayout?.displayContextualToolbar(
            textSelectionToolbar!!,
            true
        )
    }

    override fun onExitTextSelectionMode(controller: TextSelectionController) {
        viewDataBinding?.toolbarCoordinatorLayout?.removeContextualToolbar(true)
        textSelectionToolbar!!.unbindController()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        pdfFragment.removeOnAnnotationCreationModeChangeListener(this)
        pdfFragment.removeOnAnnotationEditingModeChangeListener(this)
        pdfFragment.removeOnTextSelectionModeChangeListener(this)
    }

    private fun openPrintDialog() {
        setFlagsOnAllAnnotations(EnumSet.of(AnnotationFlags.PRINT, AnnotationFlags.NOZOOM))
    }

    /** Set flags on all annotations in document.  */
    private fun setFlagsOnAllAnnotations(flags: EnumSet<AnnotationFlags>) {
        pdfFragment.document!!
            .annotationProvider
            .getAllAnnotationsOfTypeAsync(
                EnumSet.allOf(
                    AnnotationType::class.java
                )
            )
            .subscribe { annotation: Annotation ->
                annotation.flags = flags
                pdfFragment.notifyAnnotationHasChanged(annotation)
            }
    }

    override fun onSearchClicked() {
        openSearch()
    }

    override fun onThumbnailClicked() {
        openThumbnailPage()
    }

    override fun onOutlineClicked() {
        openBookmarkDialog()
    }

    override fun onPrintClicked() {
        openPrintDialog()
    }

    override fun onSignatureClicked() {

        openSignatureDialog()
    }

    private fun saveAllAnnotations() {
        val task = PdfProcessorTask.fromDocument(mPdfDocument!!)
            .changeAllAnnotations(PdfProcessorTask.AnnotationProcessingMode.FLATTEN)
        PdfProcessor.processDocumentAsync(
            task, File(
                FileUtils.getTempFileName(
                    this@CustomPDFActivity,
                    mDocumentDetail?.documentId ?: "", mDocumentDetail!!.transfer.transferId
                )
            )
        )
    }

    private fun hasDocumentAnnotations() {
        val annotation = mPdfDocument!!.annotationProvider.getAnnotationsAsync(0)
        if (annotation.isEmpty.blockingGet()) {

        } else {
        }
    }

    private fun setOrgDataToView(docOrg: DocOrganization) {
        if (isTablet()) {
            docOrgAdapter.setItems(docOrg.orgData)
        } else {
            if (PDFForwardDialog.isDialogRunning()) {
                PDFForwardDialog.setOrgDataToView(docOrg)
            }
        }
    }

    private fun setFavoriteOrgDataToView(docOrg: DocOrganization) {
        if (isTablet()) {
            favoriteOrgAdapter.setItems(docOrg.orgData)
        } else {
            if (PDFForwardDialog.isDialogRunning()) {
                PDFForwardDialog.setFavoriteOrgDataToView(docOrg)
            }
        }
    }

    private fun setSisterOrgDataToView(sisterOrg: DocOrganization) {
        if (isTablet()) {
            sisterOrgAdapter.setItems(sisterOrg.orgData)
        } else {
            if (PDFForwardDialog.isDialogRunning()) {
                PDFForwardDialog.setSisterOrgDataToView(sisterOrg)
            }
        }
    }

    private fun setFavoriteSisterDataToView(sisterOrg: DocOrganization) {
        if (isTablet()) {
            favoriteSisterOrgAdapter.setItems(sisterOrg.orgData)
        } else {
            if (PDFForwardDialog.isDialogRunning()) {
                PDFForwardDialog.setFavoriteSisterOrgDataToView(sisterOrg)
            }
        }
    }

    private fun setFavoriteUserDataToView(docExternalUser: DocExternalUser) {
        if (isTablet()) {
            favoriteUserAdapter.setItems(docExternalUser.orgData)
        } else {
            if (PDFForwardDialog.isDialogRunning()) {
                PDFForwardDialog.setFavoriteUserDataToView(docExternalUser)
            }
        }
    }

    private fun setUserDataToView(docExternalUser: DocExternalUser) {
        if (isTablet()) {
            docUserAdapter.setItems(docExternalUser.orgData)
        } else {
            if (PDFForwardDialog.isDialogRunning()) {
                PDFForwardDialog.setUserDataToView(docExternalUser)
            }
        }
    }

    private fun initOrgDetailLayout(isFavorite: Boolean, org: Organization) {
        var isNew = false
        var selectedOrg = getSelectedOrgFromList(org.id ?: "0")

        if (selectedOrg == null) {
            isNew = true
            selectedOrg = org
        }

        viewDataBinding?.layoutOrgUserDetail?.apply {
            val params = triangle?.layoutParams as LayoutParams

            params.setMargins(forwardLayoutPosition, 0, 0, 0)
            triangle.layoutParams = params

            var forwardType = 0 // 0: To 1: CC

            viewDataBinding?.layoutOrgUserDetail.apply {

                if (PreferenceManager.toolbarColor.isNotEmpty()) {
                    val colorStateList =
                        ColorStateList.valueOf(Color.parseColor(PreferenceManager.toolbarColor))
                    layoutToolbar.backgroundTintList = colorStateList
                }
                tvAdd.setTextAppearance(ResUtils.getTextViewStyle(CommonUtils.getDefaultTheme()?.popupForm?.button?.bold!!))
                tvAdd.setTextColor(Color.parseColor(CommonUtils.getDefaultTheme()?.popupForm?.button?.fgColor))
                tvTitle.setTextAppearance(ResUtils.getTextViewStyle(CommonUtils.getDefaultTheme()?.popupForm?.button?.bold!!))
                tvTitle.setTextColor(Color.parseColor(CommonUtils.getDefaultTheme()?.popupForm?.button?.fgColor))
                tvTitle.textSize =
                    ResUtils.getTextSize(CommonUtils.getDefaultTheme()?.popupForm?.button!!)
                tvAdd.textSize =
                    ResUtils.getTextSize(CommonUtils.getDefaultTheme()?.popupForm?.button!!)

                tvAdd.setOnClickListener {
                    viewDataBinding?.layoutOverlayDetail?.visibility = GONE
                }

                ivTickCc.visibility = if (selectedOrg.forwardType == 1) VISIBLE else GONE
                ivTickTo.visibility = if (selectedOrg.forwardType == 1) GONE else VISIBLE

                layoutTo.setOnClickListener {
                    forwardType = 0
                    ivTickTo.visibility = VISIBLE
                    ivTickCc.visibility = GONE
                }

                layoutCc.setOnClickListener {
                    forwardType = 1
                    ivTickTo.visibility = GONE
                    ivTickCc.visibility = VISIBLE
                }

                tvName.text = org.name ?: ""

                rvPriority.layoutManager = LinearLayoutManager(this@CustomPDFActivity)
                priorityAdapter.setItems(PreferenceManager.priorities)
                rvPriority.adapter = priorityAdapter

                var priorityPosition = -1

                if (!isNew){
                    for (i in PreferenceManager.priorities.indices){
                        if (PreferenceManager.priorities[i].label ==selectedOrg.priority?.label){
                            priorityPosition = i
                        }
                    }
                    if (priorityPosition != -1){
                        priorityAdapter.setSelection(priorityPosition)
                    }
                }

                rvAction.layoutManager = LinearLayoutManager(this@CustomPDFActivity)

                if (!isNew){
                    val actionsList = PreferenceManager.actions as ArrayList
                    for (i in actionsList.indices){
                        for(action in selectedOrg.actionList?:ArrayList()){
                            if (action.label == actionsList[i].label){
                                actionsList[i].selected = true
                            }
                        }
                    }
                    actionsAdapter.setItems(actionsList)
                }else{
                    actionsAdapter.setItems(PreferenceManager.actions)
                }

                rvAction.adapter = actionsAdapter

                tvAdd.setOnClickListener {
                    if (priorityAdapter.getSelectedValue() == null){
                        PopupUtils.showAlert(this@CustomPDFActivity, "Priority selection required")
                    }else if (actionsAdapter.getSelectedList().isEmpty()){
                        PopupUtils.showAlert(this@CustomPDFActivity, "Actions selection required")
                    }else{
                        selectedOrg.actionList = actionsAdapter.getSelectedList()
                        selectedOrg.priority = priorityAdapter.getSelectedValue()
                        selectedOrg.forwardType = forwardType
                        selectedOrg.note = etNote.text.toString().trim()
                        onAddOrgToSelectedList(selectedOrg)
                        viewDataBinding?.layoutOverlayDetail?.visibility = GONE
                    }
                }

                tvAddFavorite.visibility = if (isFavorite) GONE else VISIBLE
                tvAddFavorite.setOnClickListener {
                    if (org.type == AppConstants.TYPE_INTERNAL){
                        onAddOrgToFavoriteClicked(org)
                    }else{
                        onAddSisterOrgToFavoriteClicked(org)
                    }
                    viewDataBinding?.layoutOverlayDetail?.visibility = GONE
                }


                tvBack.setOnClickListener {
                    viewDataBinding?.layoutOverlayDetail?.visibility = GONE
                }
            }
        }
    }

    private fun initUserDetailLayout(isFavorite: Boolean, user: ExternalUser) {
        var isNew = false
        var selectedOrg = getSelectedUserFromList(user.userId ?: "0")

        if (selectedOrg == null) {
            isNew = true
            selectedOrg = user
        }

        var forwardType = 0 // 0: To 1: CC

        viewDataBinding?.layoutOrgUserDetail?.apply {
            val params = triangle?.layoutParams as LayoutParams

            params.setMargins(forwardLayoutPosition, 0, 0, 0)
            triangle.layoutParams = params

            viewDataBinding?.layoutOrgUserDetail.apply {

                if (PreferenceManager.toolbarColor.isNotEmpty()) {
                    val colorStateList =
                        ColorStateList.valueOf(Color.parseColor(PreferenceManager.toolbarColor))
                    layoutToolbar.backgroundTintList = colorStateList
                }
                tvAdd.setTextAppearance(ResUtils.getTextViewStyle(CommonUtils.getDefaultTheme()?.popupForm?.button?.bold!!))
                tvAdd.setTextColor(Color.parseColor(CommonUtils.getDefaultTheme()?.popupForm?.button?.fgColor))
                tvTitle.setTextAppearance(ResUtils.getTextViewStyle(CommonUtils.getDefaultTheme()?.popupForm?.button?.bold!!))
                tvTitle.setTextColor(Color.parseColor(CommonUtils.getDefaultTheme()?.popupForm?.button?.fgColor))
                tvTitle.textSize =
                    ResUtils.getTextSize(CommonUtils.getDefaultTheme()?.popupForm?.button!!)
                tvAdd.textSize =
                    ResUtils.getTextSize(CommonUtils.getDefaultTheme()?.popupForm?.button!!)

                tvAdd.setOnClickListener {
                    viewDataBinding?.layoutOverlayDetail?.visibility = GONE
                }

                ivTickCc.visibility = if (selectedOrg.forwardType == 1) View.VISIBLE else View.GONE
                ivTickTo.visibility = if (selectedOrg.forwardType == 1) View.GONE else View.VISIBLE

                layoutTo.setOnClickListener {
                    forwardType = 0
                    ivTickTo.visibility = VISIBLE
                    ivTickCc.visibility = GONE
                }

                layoutCc.setOnClickListener {
                    forwardType = 1
                    ivTickTo.visibility = GONE
                    ivTickCc.visibility = VISIBLE
                }

                tvName.text = user.fullName ?: ""

                rvPriority.layoutManager = LinearLayoutManager(this@CustomPDFActivity)
                priorityAdapter.setItems(PreferenceManager.priorities)
                rvPriority.adapter = priorityAdapter

                rvAction.layoutManager = LinearLayoutManager(this@CustomPDFActivity)
                actionsAdapter.setItems(PreferenceManager.actions)
                rvAction.adapter = actionsAdapter

                tvAdd.setOnClickListener {
                    if (priorityAdapter.getSelectedValue() == null){
                        PopupUtils.showAlert(this@CustomPDFActivity, "Priority selection required")
                    }else if (actionsAdapter.getSelectedList().isEmpty()){
                        PopupUtils.showAlert(this@CustomPDFActivity, "Actions selection required")
                    }else{
                        selectedOrg.actionList = actionsAdapter.getSelectedList()
                        selectedOrg.priorityValue = priorityAdapter.getSelectedValue()
                        selectedOrg.forwardType = forwardType
                        selectedOrg.note = etNote.text.toString().trim()
                       onAddUserToSelectedList(selectedOrg)
                        viewDataBinding?.layoutOverlayDetail?.visibility = GONE
                    }
                }
                tvAddFavorite.visibility = if (isFavorite) View.GONE else View.VISIBLE

                tvAddFavorite.setOnClickListener {
                    onAddUserToFavoriteList(user)
                    viewDataBinding?.layoutOverlayDetail?.visibility = GONE
                }

                tvBack.setOnClickListener {
                    viewDataBinding?.layoutOverlayDetail?.visibility = GONE
                }
            }
        }
    }


    private fun onOrgDetailClicked() {
        viewDataBinding?.layoutOverlayDetail?.visibility = VISIBLE
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        // Received broadcast message 'Order Changed'
        when (event.message) {
            AppConstants.MESSAGE_SELECTED_USER_CHANGED -> {
                if (isTablet()) {
                    val orgList =   viewModel.getDocSelectedUserList(mDocumentDetail?.documentId ?: "0")
                    selectedUserAdapter.setItems(orgList)
                    badgeUser?.number = orgList.size
                    badgeUser?.isVisible = orgList.isNotEmpty()
                } else {
                    if (PDFForwardDialog.isDialogRunning()) {
                        PDFForwardDialog.setSelectedUserDataToView(
                            viewModel.getDocSelectedUserList(
                                mDocumentDetail?.documentId ?: "0"
                            )
                        )
                    }
                }
                initForwardNQuickFowrard()
            }

            AppConstants.MESSAGE_SELECTED_ORG_CHANGED -> {
                if (isTablet()) {
                    val orgList =   viewModel.getDocSelectedOrgList(mDocumentDetail?.documentId ?: "0")
                    selectedOrgAdapter.setItems(orgList)
                    badgeOrg?.number = orgList.size
                    badgeOrg?.isVisible = orgList.isNotEmpty()
                } else {
                    if (PDFForwardDialog.isDialogRunning()) {
                        PDFForwardDialog.setSelectedOrgDataToView(
                            viewModel.getDocSelectedOrgList(
                                mDocumentDetail?.documentId ?: "0"
                            )
                        )
                    }
                }
                initForwardNQuickFowrard()
            }

            AppConstants.MESSAGE_SELECTED_SISTER_ORG_CHANGED -> {
                if (isTablet()) {
                    val orgList =   viewModel.getDocSelectedSisterOrgList(mDocumentDetail?.documentId ?: "0")
                    selectedSisterOrgAdapter.setItems(orgList)
                    badgeSisterOrg?.number = orgList.size
                    badgeSisterOrg?.isVisible = orgList.isNotEmpty()
                } else {
                    if (PDFForwardDialog.isDialogRunning()) {
                        PDFForwardDialog.setSelectedSisterOrgDataToView(
                            viewModel.getDocSelectedSisterOrgList(
                                mDocumentDetail?.documentId ?: "0"
                            )
                        )
                    }
                }
                initForwardNQuickFowrard()
            }

        }
    }

    private fun initForwardNQuickFowrard(){
        val totalCount = getTotalSelectionCount()
        viewDataBinding?.layoutBottomBar?.tvForwardCount?.apply {
            text = totalCount.toString()
            visibility = if (totalCount == 0) VISIBLE else VISIBLE
        }

        val totalQuickCount = getTotalQuickSelectionCount()
        viewDataBinding?.layoutBottomBar?.tvQuickForwardCount?.apply {
            text = totalQuickCount.toString()
            visibility = if (totalQuickCount == 0) VISIBLE else VISIBLE
        }
    }

    fun getTotalSelectionCount(): Int {
        return viewModel.getDocSelectedUserList(mDocumentDetail?.documentId ?: "0").size +   viewModel.getDocSelectedOrgList(mDocumentDetail?.documentId ?: "0").size +   viewModel.getDocSelectedSisterOrgList(mDocumentDetail?.documentId ?: "0").size
    }

    fun getTotalQuickSelectionCount(): Int{
       return viewModel.getDocSelectedOrgList(mDocumentDetail?.documentId ?: "0").size
    }

    private fun setSelectedDataToView() {
        if (PDFForwardDialog.isDialogRunning()) {
            PDFForwardDialog.apply {
                val docId = mDocumentDetail?.documentId ?: "0"
                setSelectedOrgDataToView(viewModel.getDocSelectedOrgList(docId))
                setSelectedSisterOrgDataToView(viewModel.getDocSelectedSisterOrgList(docId))
                setSelectedUserDataToView(viewModel.getDocSelectedUserList(docId))
            }
        }
    }

    fun onAddOrgToSelectedList(org: Organization) {
        viewModel.addOrgToSelectedList(mDocumentDetail?.documentId ?: "0", org)
    }

    fun onAddOrgToFavoriteClicked(org: Organization) {
        viewModel.addOrgToFavorite(org)
    }

    fun onAddSisterOrgToFavoriteClicked(org: Organization) {
        viewModel.addSisterOrgToFavorite(org)
    }

    fun onDeleteSelectedOrgClicked(org: Organization) {
        viewModel.deleteOrgFromSelectedList(mDocumentDetail?.documentId ?: "0", org)
    }

    fun onAddUserToSelectedList(user: ExternalUser){
        viewModel.addUserToSelectedList(mDocumentDetail?.documentId ?: "0", user)
    }

    fun onAddUserToFavoriteList(user: ExternalUser){
        viewModel.addUserToFavorite(user)
    }

    fun onDeleteSelectedSisterOrgClicked(org: Organization) {
        viewModel.deleteSisterOrgFromList(mDocumentDetail?.documentId ?: "0", org)
    }

    fun onDeleteSelectedUserClicked(user: ExternalUser) {
        viewModel.deleteUserFromSelectedList(mDocumentDetail?.documentId ?: "0", user)
    }

    fun getSelectedOrgFromList(orgId: String): Organization? {
        return viewModel.findOrgFromSelectedList(docId, orgId)
    }

    fun getSelectedUserFromList(userId: String) : ExternalUser?{
        return viewModel.findUserFromSelectedList(docId, userId)
    }

    fun onDeleteOrgFromFavorite(org: Organization) {
        viewModel.removeOrgFromFavorite(org)
    }

    fun onDeleteSisterOrgFromFavorite(org: Organization){
        viewModel.removeSisterOrgToFavorite(org)
    }

    fun onDeleteUserFromFavorite(user:ExternalUser){
        viewModel.removeUserFromFavorite(user)
    }

    fun sendDocument(){
        viewModel.sendDocument(this@CustomPDFActivity, mDocumentDetail!!.documentId ?: "",    mDocumentDetail!!.boxId!!,
            mDocumentDetail!!.transfer.transferId,       File(
                FileUtils.getPDFFileName(
                    this@CustomPDFActivity,
                    mDocumentDetail!!.transfer.documentId,
                    mDocumentDetail!!.transfer.transferId
                )
            ))
    }

    fun updateFavoriteUserList(){
/*        if (viewModel.selectedDeletingUser != null){
            if (isTablet()){
                favoriteUserAdapter.removeElement(viewModel.selectedDeletingUser!!)
            }else{
                if (PDFForwardDialog.isDialogRunning()){
                    PDFForwardDialog.deleteUserElement(viewModel.selectedDeletingUser!!)
                }
            }
        }*/
        viewModel.getDocumentUsers(mDocumentDetail!!.documentId)
    }

    fun updateFavoriteOrgList(){
 /*       if (viewModel.selectedDeletingOrg != null){
            if (isTablet()){
                favoriteOrgAdapter.removeElement(viewModel.selectedDeletingOrg!!)
            }else{
                if (PDFForwardDialog.isDialogRunning()){
                    PDFForwardDialog.deleteOrgElement(viewModel.selectedDeletingOrg!!)
                }
            }
        }*/
        viewModel.getOrganizations(mDocumentDetail!!.documentId)
    }

    fun updateFavoriteSisterOrgList(){
        viewModel.getSisterOrganizations(mDocumentDetail!!.documentId)
    /*    if (viewModel.selectedDeletingOrg != null){
            if (isTablet()){
                favoriteSisterOrgAdapter.removeElement(viewModel.selectedDeletingOrg!!)
            }else{
                if (PDFForwardDialog.isDialogRunning()){
                    PDFForwardDialog.deleteSisterOrgElement(viewModel.selectedDeletingOrg!!)
                }
            }
        }*/
    }

    fun onNavMoreClicked(content: String){
        when(content){
            this@CustomPDFActivity.resources.getString(R.string.str_unread)->{
                unreadDocument()
            }
            this@CustomPDFActivity.resources.getString(R.string.str_share)->{
                if (fromAttachment) shareAttachedDocument() else shareMainDocument()
            }
            this@CustomPDFActivity.resources.getString(R.string.str_follow_up)->{
                followUpDocument()
            }
            this@CustomPDFActivity.resources.getString(R.string.str_favorite)->{
                favoriteDocument()
            }
            this@CustomPDFActivity.resources.getString(R.string.str_edit)->{

            }
            this@CustomPDFActivity.resources.getString(R.string.str_delete)->{
                deleteDocument()
            }
            this@CustomPDFActivity.resources.getString(R.string.str_delete)->{
                trashDocument()
            }
            this@CustomPDFActivity.resources.getString(R.string.str_un_trash)->{
                unTrashDocument()
            }
            this@CustomPDFActivity.resources.getString(R.string.str_reply_to)->{

            }
            this@CustomPDFActivity.resources.getString(R.string.str_linked_to)->{

            }
            this@CustomPDFActivity.resources.getString(R.string.str_dcc)->{

            }
            this@CustomPDFActivity.resources.getString(R.string.str_unarchive)->{
                onUnarchiveDocument()
            }
            this@CustomPDFActivity.resources.getString(R.string.str_sign_request)->{

            }
        }

    }

    fun onUnarchiveDocument(){
        viewModel.unArchive(mDocumentDetail!!.documentId, mDocumentDetail!!.boxId, mDocumentDetail!!.transfer.transferId ?: "",
            File(
                FileUtils.getPDFFileName(
                    this@CustomPDFActivity,
                    mDocumentDetail!!.documentId,
                    mDocumentDetail!!.transfer.transferId
                )
            )
        )
    }

    fun shareMainDocument(){
        viewModel.shareMainDocument(mDocumentDetail!!.documentId,
            File(
                FileUtils.getPDFFileName(
                    this@CustomPDFActivity,
                    mDocumentDetail!!.documentId,
                    mDocumentDetail!!.transfer.transferId
                )
            )
        )
    }

    fun shareAttachedDocument(){
        viewModel.shareAttachedPDF(mDocumentDetail!!.documentId, attachmentId,
            File(
                FileUtils.getPDFFileName(
                    this@CustomPDFActivity,
                    mDocumentDetail!!.documentId,
                    mDocumentDetail!!.transfer.transferId
                )
            )
        )
    }

    fun followUpDocument(){
        viewModel.followUpDocument(mDocumentDetail!!.documentId, mDocumentDetail!!.boxId,
            File(
                FileUtils.getPDFFileName(
                    this@CustomPDFActivity,
                    mDocumentDetail!!.documentId,
                    mDocumentDetail!!.transfer.transferId
                )
            )
        )
    }

    fun favoriteDocument(){
        viewModel.favoriteDocument(mDocumentDetail!!.documentId, mDocumentDetail!!.boxId,
            File(
                FileUtils.getPDFFileName(
                    this@CustomPDFActivity,
                    mDocumentDetail!!.documentId,
                    mDocumentDetail!!.transfer.transferId
                )
            )
        )
    }

    fun unreadDocument(){
        viewModel.unReadDocument(mDocumentDetail!!.documentId, mDocumentDetail!!.boxId,
            File(
                FileUtils.getPDFFileName(
                    this@CustomPDFActivity,
                    mDocumentDetail!!.documentId,
                    mDocumentDetail!!.transfer.transferId
                )
            )
        )
    }

    fun deleteDocument(){
        viewModel.deleteDocument(mDocumentDetail!!.documentId, mDocumentDetail!!.boxId,
            File(
                FileUtils.getPDFFileName(
                    this@CustomPDFActivity,
                    mDocumentDetail!!.documentId,
                    mDocumentDetail!!.transfer.transferId
                )
            )
        )
    }

    fun trashDocument(){
        viewModel.trashDocument(mDocumentDetail!!.documentId, mDocumentDetail!!.boxId,
            File(
                FileUtils.getPDFFileName(
                    this@CustomPDFActivity,
                    mDocumentDetail!!.documentId,
                    mDocumentDetail!!.transfer.transferId
                )
            )
        )
    }

    fun unTrashDocument(){
        viewModel.unTrashDocument(mDocumentDetail!!.documentId, mDocumentDetail!!.boxId,
            File(
                FileUtils.getPDFFileName(
                    this@CustomPDFActivity,
                    mDocumentDetail!!.documentId,
                    mDocumentDetail!!.transfer.transferId
                )
            )
        )
    }

    private fun observeUnarchive(result: String){
        if (result == "success"){
            finish()
            FileUtils.deleteRecursive(
                File(
                FileUtils.getPDFFileName(
                    this@CustomPDFActivity,
                    mDocumentDetail!!.documentId,
                    mDocumentDetail!!.transfer.transferId
                )
            ))
            EventBus.getDefault().post(MessageEvent(AppConstants.MESSAGE_UNARCHIVE_DONE))
        }else{
            if (result.isNotEmpty()) Toast.makeText(this@CustomPDFActivity, result, LENGTH_LONG).show()
        }
    }

    private fun observeShareResult(result: String){
        if (result == "success"){
            val pdfURL =
                "${BASE_URL}boxes/${mDocumentDetail!!.boxId}/documents/${mDocumentDetail!!.boxId}/${mDocumentDetail!!.transfer.transferId}/file"
            PopupUtils.openShareDialog(this@CustomPDFActivity, pdfURL)
        }else{
            if (result.isNotEmpty()) Toast.makeText(this@CustomPDFActivity, result, LENGTH_LONG).show()
        }
    }

    private fun observeFollowupResult(result: String){
        if (result == "success"){
            mDocumentDetail!!.acl.documentFollowup = !mDocumentDetail!!.acl.documentFollowup
            initBottomBarIconVisibility()
            initToolbarIconVisibility()
        }else{
            if (result.isNotEmpty()) Toast.makeText(this@CustomPDFActivity, result, LENGTH_LONG).show()
        }
    }


    private fun observeFavoriteResult(result: String){
        if (result == "success"){
            mDocumentDetail!!.acl.documentFavorite = !mDocumentDetail!!.acl.documentFavorite
        }else{
            if (result.isNotEmpty()) Toast.makeText(this@CustomPDFActivity, result, LENGTH_LONG).show()
        }
    }

    private fun observeUnreadResult(result: String){
        if (result == "success"){
            finish()
            FileUtils.deleteRecursive(
                File(
                    FileUtils.getPDFFileName(
                        this@CustomPDFActivity,
                        mDocumentDetail!!.documentId,
                        mDocumentDetail!!.transfer.transferId
                    )
                ))
            EventBus.getDefault().post(MessageEvent(AppConstants.MESSAGE_UNARCHIVE_DONE))
        }else{
            if (result.isNotEmpty()) Toast.makeText(this@CustomPDFActivity, result, LENGTH_LONG).show()
        }
    }

    private fun observeDeleteResult(result: String){
        if (result == "success"){
            finish()
            FileUtils.deleteRecursive(
                File(
                    FileUtils.getPDFFileName(
                        this@CustomPDFActivity,
                        mDocumentDetail!!.documentId,
                        mDocumentDetail!!.transfer.transferId
                    )
                ))
            EventBus.getDefault().post(MessageEvent(AppConstants.MESSAGE_UNARCHIVE_DONE))
        }else{
            if (result.isNotEmpty()) Toast.makeText(this@CustomPDFActivity, result, LENGTH_LONG).show()
        }
    }

    private fun observeTrashResult(result: String){
        if (result == "success"){
            finish()
            FileUtils.deleteRecursive(
                File(
                    FileUtils.getPDFFileName(
                        this@CustomPDFActivity,
                        mDocumentDetail!!.documentId,
                        mDocumentDetail!!.transfer.transferId
                    )
                ))
            EventBus.getDefault().post(MessageEvent(AppConstants.MESSAGE_UNARCHIVE_DONE))
        }else{
            if (result.isNotEmpty()) Toast.makeText(this@CustomPDFActivity, result, LENGTH_LONG).show()
        }
    }

    private fun observeUntrashResult(result: String){
        if (result == "success"){
            finish()
            FileUtils.deleteRecursive(
                File(
                    FileUtils.getPDFFileName(
                        this@CustomPDFActivity,
                        mDocumentDetail!!.documentId,
                        mDocumentDetail!!.transfer.transferId
                    )
                ))
            EventBus.getDefault().post(MessageEvent(AppConstants.MESSAGE_UNARCHIVE_DONE))
        }else{
            if (result.isNotEmpty()) Toast.makeText(this@CustomPDFActivity, result, LENGTH_LONG).show()
        }
    }
}
