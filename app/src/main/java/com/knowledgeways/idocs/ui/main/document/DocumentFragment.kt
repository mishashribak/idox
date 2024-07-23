package com.knowledgeways.idocs.ui.main.document

import  android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.base.BaseFragment
import com.knowledgeways.idocs.databinding.FragmentDocumentBinding
import com.knowledgeways.idocs.db.PreferenceManager
import com.knowledgeways.idocs.di.Injectable
import com.knowledgeways.idocs.network.ResultWrapper
import com.knowledgeways.idocs.network.model.Box
import com.knowledgeways.idocs.network.model.DocID
import com.knowledgeways.idocs.network.model.Document
import com.knowledgeways.idocs.network.model.DownloadStatus
import com.knowledgeways.idocs.network.model.documentdetail.DocumentDetail
import com.knowledgeways.idocs.network.model.theme.Theme
import com.knowledgeways.idocs.ui.component.dialog.DialogOtp
import com.knowledgeways.idocs.ui.component.dialog.DialogPDFDownloadProgress
import com.knowledgeways.idocs.ui.main.MainActivity
import com.knowledgeways.idocs.ui.main.MainViewModel
import com.knowledgeways.idocs.ui.main.adapter.document.DocumentAdapter
import com.knowledgeways.idocs.ui.pdf.CustomPDFActivity
import com.knowledgeways.idocs.utils.AppConstants
import com.knowledgeways.idocs.utils.MessageEvent
import com.knowledgeways.idocs.utils.ResUtils
import com.pspdfkit.configuration.activity.PdfActivityConfiguration
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class DocumentFragment : BaseFragment<FragmentDocumentBinding?, MainViewModel>(), Injectable {

    override val layoutId: Int
        get() = R.layout.fragment_document

    override val viewModel: MainViewModel
        get() {
            return getViewModel(baseActivity, MainViewModel::class.java)
        }

    private var documentAdapter: DocumentAdapter = DocumentAdapter { document, position ->
        onDocumentSelected(document, position)
    }
    private var mDocumentList: ArrayList<Document> = ArrayList()
    private var defaultTheme: Theme? = null
    private lateinit var autoCompleteAdapter: ArrayAdapter<String>
    private var keywordList = ArrayList<String>()
    private var mDocument: Document? = null
    private lateinit var pdfConfig: PdfActivityConfiguration
    private var positionToUpdate = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        defaultTheme = viewModel.getDefaultTheme()
        pdfConfig = PdfActivityConfiguration.Builder(baseActivity).build()

        initUI()
        initObservers()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    private fun observeThemeData() {
        viewModel.isThemeChanged.observe(baseActivity) {
            defaultTheme = viewModel.getDefaultTheme()
            initSearchBar()
            documentAdapter.setItems(mDocumentList)
        }
    }

    private fun initUI() {
        initSearchBar()
        initRefreshLayout()
        initRecyclerView()
    }

    private fun initSearchBar() {
        viewDataBinding?.apply {
            ivBackgroundSearch.setColorFilter(
                Color.parseColor(defaultTheme?.documentList?.searchBar?.bgColor),
                PorterDuff.Mode.SRC_IN
            )

            edittextSearch.apply {
                setTextColor(Color.parseColor(defaultTheme?.documentList?.searchBar?.fgColor))
                textSize = ResUtils.getTextSize(defaultTheme?.documentList?.searchBar?.fontSize!!)
            }

            edittextSearch.setOnItemClickListener { parent, view, pos, id ->
            }


            viewDataBinding?.edittextSearch?.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                    }

                    override fun afterTextChanged(textChanged: Editable?) {
                        documentAdapter.setFilterString(textChanged.toString().trim())
                    }
                }
            )
        }
    }

    private fun initRefreshLayout() {
        viewDataBinding?.refreshLayout?.setOnRefreshListener {
            if (viewModel.selectedBox.value != null) {
                viewModel.pageNumber = 0
                viewModel.removeDocuments()
                getDocuments(viewModel.selectedBox.value!!)
            }
        }
    }

    private fun initRecyclerView() {
        viewDataBinding?.recyclerView?.apply {
            layoutManager = LinearLayoutManager(baseActivity)
            adapter = documentAdapter
            isNestedScrollingEnabled = true
            setHasFixedSize(true)

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager?

                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == documentAdapter.itemCount - 1) {
                        //bottom of list!
                        if (documentAdapter.itemCount % 10 == 0 && documentAdapter.itemCount != 0) {
                            viewModel.pageNumber += 1
                        }
                        loadMore()
                    }
                }
            })
        }
    }

    private fun initObservers() {
        viewModel.apply {
            selectedBox.observe(baseActivity) { box ->
                getDocuments(box)
            }

            documentList.observe(baseActivity) { documentList ->
                observeDocumentData(documentList)
            }

            observeThemeData()

            documentDetail.observe(baseActivity) { documentDetail ->
                observeDocumentDetail(documentDetail)
            }

            isTempFilesChanged.observe(baseActivity) {
                documentAdapter.notifyDataSetChanged()
            }
        }
        observeDownloadStatus()
    }

    private fun observeDocumentData(documentList: ResultWrapper<List<Document>>) {
        when (documentList) {
            is ResultWrapper.Loading -> {
                viewDataBinding?.refreshLayout?.isRefreshing = true
            }
            is ResultWrapper.GenericError -> {
                viewDataBinding?.refreshLayout?.isRefreshing = false
            }

            is ResultWrapper.Success -> {
                viewDataBinding?.refreshLayout?.isRefreshing = false

                if (mDocumentList.size % 10 != 0) {
                    val actualDocument = ArrayList<Document>()
                    for (i in 0..(mDocumentList.size / 10) * 10) {
                        actualDocument.add(mDocumentList[i])
                    }
                    mDocumentList.clear()
                    mDocumentList.addAll(actualDocument)
                }

                documentList.value.forEach {
                    mDocumentList.add(it)
                }
                keywordList.clear()
                mDocumentList.forEach {
                    if (it.from != null && it.from.isNotEmpty()) {
                        keywordList.add(it.from)
                    }
                    if (it.subject != null && it.subject.isNotEmpty()) {
                        keywordList.add(it.subject)
                    }
                }
                autoCompleteAdapter =
                    ArrayAdapter(baseActivity, R.layout.recycler_item_spinner_unit, keywordList)
                viewDataBinding?.edittextSearch?.setAdapter(autoCompleteAdapter)
                documentAdapter.setItems(mDocumentList)
                AppConstants.mDocumentList.clear()
                AppConstants.mDocumentList.addAll(mDocumentList)
            }
            else -> {}
        }
    }

    private fun getDocuments(box: Box) {
        if (viewModel.pageNumber == 0) mDocumentList.clear()
        viewModel.getDocumentList(box.boxId ?: "", viewModel.pageNumber)
    }

    private fun loadMore() {
        getDocuments(viewModel.selectedBox.value!!)
    }

    private fun onDocumentSelected(document: Document, position: Int) {
        mDocument = document
        viewModel.getDocumentDetail(document)
        positionToUpdate = position
    }

    private fun observeDocumentDetail(documentDetail: ResultWrapper<DocumentDetail>) {
        when (documentDetail) {
            is ResultWrapper.Loading -> {
                viewDataBinding?.layoutProgress?.visibility = VISIBLE
                //     viewDataBinding?.refreshLayout?.isRefreshing = true
            }
            is ResultWrapper.GenericError -> {
                viewDataBinding?.layoutProgress?.visibility = GONE
                if (documentDetail.code == 201) {
                    DialogOtp.openDialog(
                        baseActivity, (baseActivity as MainActivity).toolbarColor,
                        defaultTheme?.popupForm?.title?.bgColor ?: "", documentDetail.error ?: ""
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
                viewDataBinding?.layoutProgress?.visibility = GONE
                if (mDocument != null) {
                    if (DialogOtp.isDialogRunning()) DialogOtp.dismissDialog()

                    addOpenedDocument()
                    val intent = Intent(baseActivity, CustomPDFActivity::class.java)
                    intent.putExtra(AppConstants.KEYWORD_DOCUMENT_DETAIL, documentDetail.value)
                    intent.putExtra(AppConstants.KEYWORD_DOCUMENT, mDocument!!)
                    intent.putExtra(AppConstants.KEYWORD_TOOLBAR_COLOR,
                        (baseActivity as MainActivity).pdfToolbarColor)
                    intent.putExtra(AppConstants.KEYWORD_FROM_PDF_LIST,
                        true)
                    intent.putExtra(AppConstants.KEYWORD_PDF_SIZE,
                        mDocumentList.size)
                    intent.putExtra(AppConstants.KEYWORD_SELECTED_POSITION,
                        positionToUpdate)
                    intent.putExtra(AppConstants.KEYWORD_BACKGROUND_COLOR,
                        defaultTheme!!.viewer!!.bgColor)

                    startActivity(intent)
                }
            }

            else -> {}
        }
    }

    private fun addOpenedDocument() {
        val docIdList = PreferenceManager.documentList as ArrayList
        var isDocumentExisting = false
        for (docId in docIdList) {
            if (docId.transferId == mDocument!!.transferId
                && docId.docID == mDocument!!.documentId
            ) {
                isDocumentExisting = true
            }
        }
        if (!isDocumentExisting) {
            docIdList.add(DocID(mDocument!!.transferId!!, mDocument!!.documentId!!))
            PreferenceManager.documentList = docIdList
        }
    }

    private fun observeDownloadStatus() {
        viewModel.progressLiveData.observe(baseActivity) { state ->
            //    if (state.fileType == )
            if (viewModel.downloadingFile == 1) {
                updateDownloadStatus(state)
            }
        }
    }

    private fun updateDownloadStatus(status: DownloadStatus) {
        DialogPDFDownloadProgress.updateProgressStatus(status)
    }

    override fun onResume() {
        super.onResume()
        documentAdapter.setItems(mDocumentList)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        // Received broadcast message 'Order Changed'
        when (event.message) {
            AppConstants.MESSAGE_UNARCHIVE_DONE -> {
                viewModel.getBoxes()
                viewModel.getDocumentTotal()
                viewModel.pageNumber = 0
                viewModel.removeDocuments()
                getDocuments(viewModel.selectedBox.value!!)
            }
        }
    }
}