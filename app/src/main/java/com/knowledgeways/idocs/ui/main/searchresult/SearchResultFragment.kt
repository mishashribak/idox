package com.knowledgeways.idocs.ui.main.searchresult

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.base.BaseFragment
import com.knowledgeways.idocs.databinding.FragmentDocumentBinding
import com.knowledgeways.idocs.di.Injectable
import com.knowledgeways.idocs.network.ResultWrapper
import com.knowledgeways.idocs.network.model.Document
import com.knowledgeways.idocs.network.model.theme.Theme
import com.knowledgeways.idocs.ui.main.MainViewModel
import com.knowledgeways.idocs.ui.main.adapter.document.DocumentAdapter
import com.knowledgeways.idocs.utils.ResUtils

class SearchResultFragment  : BaseFragment<FragmentDocumentBinding?, MainViewModel>(), Injectable {

    override val layoutId: Int
        get() = R.layout.fragment_document

    override val viewModel: MainViewModel
        get() {
            return getViewModel(baseActivity, MainViewModel::class.java)
        }

    private var documentAdapter: DocumentAdapter = DocumentAdapter(){
        document, position ->
    }
    private var mDocumentList: ArrayList<Document> = ArrayList()
    private var defaultTheme: Theme? = null
    private lateinit var autoCompleteAdapter: ArrayAdapter<String>
    private var keywordList = ArrayList<String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        defaultTheme = viewModel.getDefaultTheme()

        viewModel.searchPageNumber = 0
        getDocuments()

        initUI()
        initObservers()

    }

    private fun observeThemeData(){
        viewModel.isThemeChanged.observe(baseActivity){
            defaultTheme = viewModel.getDefaultTheme()
            initSearchBar()
            documentAdapter.setItems(mDocumentList)
        }
    }

    private fun initUI(){
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
                //    if (keywordList.isNotEmpty()){
                //      documentAdapter.setFilterString(keywordList[pos])
                //    }
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
                viewModel.searchPageNumber = 0
                getDocuments()
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


                    if ( linearLayoutManager?.findLastCompletelyVisibleItemPosition() == documentAdapter.itemCount - 1) {
                        //bottom of list!
                        if (documentAdapter.itemCount % 10 == 0 && documentAdapter.itemCount != 0) {
                            viewModel.searchPageNumber += 1
                        }
                        loadMore()
                    }
                }
            })
        }
    }

    private fun initObservers() {
        viewModel.apply {
            searchResult.observe(baseActivity) { documentList ->
                observeDocumentData(documentList)
            }

            observeThemeData()
        }
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
                    if (it.from != null && it.from.isNotEmpty()){
                        keywordList.add(it.from)
                    }
                    if (it.subject != null && it.subject.isNotEmpty()){
                        keywordList.add(it.subject)
                    }
                }
                autoCompleteAdapter = ArrayAdapter(baseActivity, R.layout.recycler_item_spinner_unit, keywordList)
                viewDataBinding?.edittextSearch?.setAdapter(autoCompleteAdapter)
                documentAdapter.setItems(mDocumentList)
            }
            else -> {}
        }
    }

    private fun getDocuments() {
        if (viewModel.pageNumber == 0) mDocumentList.clear()
        viewModel.searchDocuments(viewModel.searchPageNumber)
    }

    private fun loadMore() {
        getDocuments()
    }
}