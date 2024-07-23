package com.knowledgeways.idocs.ui.main.search

import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.base.BaseFragment
import com.knowledgeways.idocs.databinding.FragmentSearchBinding
import com.knowledgeways.idocs.db.PreferenceManager
import com.knowledgeways.idocs.di.Injectable
import com.knowledgeways.idocs.network.model.Category
import com.knowledgeways.idocs.network.model.theme.Theme
import com.knowledgeways.idocs.network.model.user.Organization
import com.knowledgeways.idocs.ui.component.dialog.documentType.CategoryAdapter
import com.knowledgeways.idocs.ui.component.dialog.documentType.CategoryPicker
import com.knowledgeways.idocs.ui.component.dialog.externalorganization.ExternalOrganizationPicker
import com.knowledgeways.idocs.ui.component.dialog.internalorganization.InternalOrganizationPicker
import com.knowledgeways.idocs.ui.component.dialog.organization.OrganizationAdapter
import com.knowledgeways.idocs.ui.main.MainActivity
import com.knowledgeways.idocs.ui.main.MainViewModel
import com.knowledgeways.idocs.utils.CommonUtils
import com.knowledgeways.idocs.utils.ResUtils
import java.text.SimpleDateFormat
import java.util.*


class SearchFragment : BaseFragment<FragmentSearchBinding?, MainViewModel>(), Injectable , OrganizationAdapter.OnOrganizationClickedListener,
CategoryAdapter.OnCategorySelectedListener{

    override val layoutId: Int
        get() = R.layout.fragment_search

    override val viewModel: MainViewModel
        get() {
            return getViewModel(baseActivity, MainViewModel::class.java)
        }

    private var defaultTheme: Theme ?= null

    lateinit var categoryAdapter: CategoryAdapter
    lateinit var externalOrganizationAdapter: OrganizationAdapter
    lateinit var internalOrganizationAdapter: OrganizationAdapter

    lateinit var fromDatePicker: DatePickerDialog
    lateinit var toDatePicker: DatePickerDialog

    var fromTime = 0L
    var toTime = 0L

    var currentOrganizationDialogType = 1

    var dateFormatToSave = SimpleDateFormat("yyyy-MM-dd")


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeThemeData()
        initValues()
        iniUI()
    }

    private fun getToday(){
        val date = Calendar.getInstance().time
        val df = SimpleDateFormat("MMM dd, yyyy")
        val formattedDate = df.format(date)

        val dateToSave = dateFormatToSave.format(date)
        viewModel.apply {
            dateFrom = dateToSave
            dateTo = dateToSave
            dateFromHijri = convertToHijri(Calendar.getInstance())
            dateToHijri = convertToHijri(Calendar.getInstance())
        }

        viewDataBinding?.apply {
            tvFromDate.text = formattedDate
            tvToDate.text = formattedDate
        }
    }

    private fun observeThemeData(){
        viewModel.isThemeChanged.observe(baseActivity){
            initValues()
            iniUI()
        }
    }

    private fun initValues(){
        defaultTheme = viewModel.getDefaultTheme()
    }

    private fun iniUI(){

        initFromDatePicker()
        initToDatePicker()
        if (baseActivity.isTablet()){
            initTapViewEvents()
        }

        viewDataBinding?.apply {
            layoutChooseType.setOnClickListener {
                if (baseActivity.isTablet()){
                    initParentTypeLayout()
                    viewDataBinding?.viewOverlay?.visibility = VISIBLE
                    viewDataBinding?.layoutParentDocumentType?.visibility = VISIBLE

                }else{
                    CategoryPicker.openDialog(baseActivity, (baseActivity as MainActivity).toolbarColor,
                        defaultTheme?.popupForm!!.title!!.fgColor ?: "", {
                            category ->  onSelected(category)
                        }, {

                        })
                }
            }

            tvExternalOrganization.setOnClickListener {
                currentOrganizationDialogType = 1
                if (baseActivity.isTablet()){
                    initExternalOrganizationLayout()
                    viewDataBinding?.layoutExternalOrganization?.visibility = VISIBLE

                }else{
                    ExternalOrganizationPicker.openDialog(baseActivity, (baseActivity as MainActivity).toolbarColor
                        , {
                          organization ->  onExternalOrganizationSelected(organization)
                        }, {
                            viewDataBinding?.viewOverlay?.visibility = GONE
                        })
                }
            }

            tvInternalOrganization.setOnClickListener {
                currentOrganizationDialogType = 2
                if (baseActivity.isTablet()){
                    initInternalOrganizationLayout()
                    viewDataBinding?.layoutInternalOrganization?.visibility = VISIBLE
                }else{
                    InternalOrganizationPicker.openDialog(baseActivity, (baseActivity as MainActivity).toolbarColor
                        , {
                                organization ->  onInternalOrganizationSelected(organization)
                        }, {
                            viewDataBinding?.viewOverlay?.visibility = GONE
                        })
                }
            }

            layoutFrom.setOnClickListener {
                hideOverLayouts()
                if (toTime != 0L){
                    fromDatePicker.datePicker.maxDate = toTime
                }
                fromDatePicker.show()
            }

            layoutTo.setOnClickListener {
                hideOverLayouts()
                if (fromTime != 0L){
                    toDatePicker.datePicker.minDate = fromTime
                }
                toDatePicker.show()
            }
        }
        getToday()
    }

    private fun initTapViewEvents(){
        viewDataBinding?.apply {

            viewOverlay?.setOnClickListener {
                viewOverlay.visibility = GONE
                layoutExternalOrganization?.visibility = GONE
                layoutInternalOrganization?.visibility = GONE
                layoutParentDocumentType?.visibility = GONE
            }

            edittextSearchKey.setOnFocusChangeListener { view, b ->
                if (b)  hideOverLayouts()
            }

            edittextReferenceNumber.setOnFocusChangeListener { view, b ->
                if (b)   hideOverLayouts()
            }

            layoutConfirm.setOnClickListener {
                hideOverLayouts()
            }

            layoutOrganization.setOnClickListener {
                hideOverLayouts()
            }

            tvOrganization.setOnClickListener {
                hideOverLayouts()
            }

            layoutDate.setOnClickListener {
                hideOverLayouts()
            }

            layoutReferenceNumber.setOnClickListener {
                hideOverLayouts()
            }

            layoutSubject.setOnClickListener {
                hideOverLayouts()
            }

            layoutDocumentType.setOnClickListener {
                hideOverLayouts()
            }

            tvDocumentDate.setOnClickListener {
                hideOverLayouts()
            }

            layoutContent.setOnClickListener {
                hideOverLayouts()
            }

            layoutParent.setOnScrollChangeListener { _, _, _, _, _ ->

            }

            tvCancel.setOnClickListener {
                onClearSelected()
            }

            tvSearch.setOnClickListener {
                onSearchClicked()
            }

            dialogInternalOrganizations?.edittextSearch?.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                    }

                    override fun afterTextChanged(textChanged: Editable?) {
                        if (::internalOrganizationAdapter.isInitialized){
                            internalOrganizationAdapter.setFilterString(textChanged.toString().trim())
                        }
                    }
                }
            )

            dialogExternalOrganizations?.edittextSearch?.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                    }

                    override fun afterTextChanged(textChanged: Editable?) {
                        if (::externalOrganizationAdapter.isInitialized){
                            externalOrganizationAdapter.setFilterString(textChanged.toString().trim())
                        }
                    }
                }
            )
        }
    }

    private fun initFromDatePicker(){
        val calendar = Calendar.getInstance()
        // Set Date-Format for Picker Dialog
        fromDatePicker = DatePickerDialog(
            baseActivity,
            R.style.DateTimePickerDialogStyle,
            { _, year, monthOfYear, dayOfMonth ->
                val newDate = Calendar.getInstance()
                newDate.set(year, monthOfYear, dayOfMonth)
                val sd = SimpleDateFormat("MMM dd, yyyy", Locale.US)
                val rawDate = newDate.time
                val fdate = sd.format(rawDate)
                val dateToSave = dateFormatToSave.format(rawDate)
                viewDataBinding?.tvFromDate?.text = fdate
                viewModel.dateFrom = dateToSave
                fromTime = newDate.timeInMillis
                viewModel.dateFromHijri = convertToHijri(newDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        fromDatePicker.setTitle("")
    }

    private fun initToDatePicker(){
        val calendar = Calendar.getInstance()
        // Set Date-Format for Picker Dialog
        toDatePicker = DatePickerDialog(
            baseActivity,
            R.style.DateTimePickerDialogStyle,
            { _, year, monthOfYear, dayOfMonth ->
                val newDate = Calendar.getInstance()
                newDate.set(year, monthOfYear, dayOfMonth)
                val sd = SimpleDateFormat("MMM dd, yyyy", Locale.US)
                val rawDate = newDate.time
                val fdate = sd.format(rawDate)
                viewDataBinding?.tvToDate?.text = fdate
                toTime = newDate.timeInMillis
                viewModel.dateToHijri = convertToHijri(newDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(
                Calendar.DAY_OF_MONTH
            )
        )

        // Customer should be more than 6 years old
        toDatePicker.setTitle("")
    }

    private fun hideOverLayouts(){
        viewDataBinding?.layoutParentDocumentType?.visibility = GONE
    }

    private fun initExternalOrganizationLayout(){
        viewDataBinding?.viewOverlay?.visibility = VISIBLE
        viewDataBinding?.dialogExternalOrganizations?.apply {
            ivBackground.setColorFilter(Color.parseColor((baseActivity as MainActivity).toolbarColor),
                PorterDuff.Mode.SRC_IN)

            recyclerView.layoutManager = LinearLayoutManager(baseActivity)
            recyclerView.isNestedScrollingEnabled = false
            initExternalOrganizationList()
        }
    }

    private fun initExternalOrganizationList(){
        externalOrganizationAdapter = OrganizationAdapter()
        externalOrganizationAdapter.listener = this
        viewDataBinding?.dialogExternalOrganizations?.recyclerView?.adapter = externalOrganizationAdapter
        externalOrganizationAdapter.setItems(PreferenceManager.externalOrganization)
    }

    private fun initInternalOrganizationLayout(){
        viewDataBinding?.viewOverlay?.visibility = VISIBLE
        viewDataBinding?.dialogInternalOrganizations?.apply {
            ivBackground.setColorFilter(Color.parseColor((baseActivity as MainActivity).toolbarColor),
                PorterDuff.Mode.SRC_IN)

            recyclerView.layoutManager = LinearLayoutManager(baseActivity)
            recyclerView.isNestedScrollingEnabled = false
            initInternalOrganizationList()
        }
    }

    private fun initInternalOrganizationList(){
        internalOrganizationAdapter = OrganizationAdapter()
        internalOrganizationAdapter.listener = this
        viewDataBinding?.dialogInternalOrganizations?.recyclerView?.adapter = internalOrganizationAdapter
        internalOrganizationAdapter.setItems(PreferenceManager.internalOrganization)
    }


    private fun initParentTypeLayout(){
        viewDataBinding?.dialogDocumentType?.apply {

            tvTitle.setTextAppearance(ResUtils.getTextViewStyle(CommonUtils.getDefaultTheme()?.popupForm?.button?.bold!!))
            tvTitle.setTextColor(Color.parseColor(defaultTheme?.popupForm!!.title!!.fgColor ?: ""))

            ivBackground.setColorFilter(Color.parseColor((baseActivity as MainActivity).toolbarColor),
                PorterDuff.Mode.SRC_IN)

            triangle?.setColorFilter(Color.parseColor((baseActivity as MainActivity).toolbarColor),
                PorterDuff.Mode.SRC_IN)

            recyclerView.layoutManager = LinearLayoutManager(baseActivity)
            recyclerView.isNestedScrollingEnabled = false

            initCategoryList()
        }
    }

    fun onExternalOrganizationSelected(organization: Organization){
        viewModel.selectedExternalOrganization = organization
        viewDataBinding?.tvExternalOrganization?.text = organization.name ?: ""
    }

    fun onInternalOrganizationSelected(organization: Organization){
        viewModel.selectedInternalOrganization = organization
        viewDataBinding?.tvInternalOrganization?.text = organization.name ?: ""
    }

    private fun initCategoryList(){
        categoryAdapter = CategoryAdapter()
        categoryAdapter.listener = this
        viewDataBinding?.dialogDocumentType?.recyclerView?.adapter = categoryAdapter
        categoryAdapter.setItems(PreferenceManager.categories)
    }


    override fun onSelected(organization: Organization) {
        if (currentOrganizationDialogType == 1) {
            onExternalOrganizationSelected(organization)
            viewDataBinding?.layoutExternalOrganization?.visibility = GONE
            viewDataBinding?.viewOverlay?.visibility = GONE
        }
        else if (currentOrganizationDialogType == 2) {
            onInternalOrganizationSelected(organization)
            viewDataBinding?.layoutInternalOrganization?.visibility = GONE
            viewDataBinding?.viewOverlay?.visibility = GONE
        }
    }

    override fun onSelected(category: Category) {
        viewModel.selectedCategory = category
        viewDataBinding?.tvChooseType?.text = category.label ?: ""
        if (baseActivity.isTablet()){
            viewDataBinding?.layoutParentDocumentType?.visibility = GONE
        }
        viewDataBinding?.viewOverlay?.visibility = GONE
    }

    private fun onClearSelected(){
        viewDataBinding?.apply {
            edittextSearchKey.setText("")
            tvChooseType.text = ""
            edittextReferenceNumber.setText("")
            getToday()
            tvExternalOrganization.text = ""
            tvInternalOrganization.text = ""
        }
    }

    private fun onSearchClicked(){
        viewDataBinding?.apply {
            viewModel.searchKey = edittextSearchKey.text.toString().trim()
            viewModel.referenceNumber = edittextReferenceNumber.text.toString().trim()
        }

        (baseActivity as MainActivity).onSearchClicked()
    }

    private fun convertToHijri(calendar: Calendar): String{
        val gregorianCalendar  = GregorianCalendar(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH))
        val uCal: Calendar = UmmalquraCalendar()
        uCal.time = gregorianCalendar.time

        val year = uCal.get(Calendar.YEAR)
        val month = uCal.get(Calendar.MONTH)
        val day = uCal.get(Calendar.DAY_OF_MONTH)

        return "$year-${convertDateFormat(month)}-${convertDateFormat(day)}"
    }

    private fun convertDateFormat(value: Int): String{
        return if (value < 10) "0$value"
        else value.toString()
    }

    override fun onDestroy() {
        viewModel.apply {
            selectedCategory = null
            selectedInternalOrganization  = null
            selectedExternalOrganization = null
            searchKey = ""
            referenceNumber = ""
        }

        super.onDestroy()
    }
}