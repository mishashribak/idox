package com.knowledgeways.idocs.ui.component.dialog.pdfdetails

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.Window
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.databinding.LayoutPdfDetailsBinding
import com.knowledgeways.idocs.db.PreferenceManager
import com.knowledgeways.idocs.network.model.PDFDetails
import com.knowledgeways.idocs.network.model.PDFHistory
import com.knowledgeways.idocs.utils.CommonUtils
import com.knowledgeways.idocs.utils.PopupUtils
import com.knowledgeways.idocs.utils.ResUtils

class PDFDetailDialog {

    var isDialogRunning = false
    var dialog: Dialog? = null

    lateinit var mBinding: LayoutPdfDetailsBinding

    fun openDialog(
        context: Context,
        toolbarColor: String,
        onDetailClicked: () -> Unit,
        onHistoryClicked: () -> Unit
    ) {
        dialog = Dialog(context)

        val binding = DataBindingUtil.inflate<LayoutPdfDetailsBinding>(
            LayoutInflater.from(context),
            R.layout.layout_pdf_details,
            null,
            false
        )

        mBinding = binding
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setContentView(binding.root)

        var currentTab  = 0

        binding.apply {

            if (toolbarColor.isNotEmpty()) {
                ivBackground.setColorFilter(Color.parseColor(toolbarColor), PorterDuff.Mode.SRC_IN)
            }
            tvClose.setTextAppearance(ResUtils.getTextViewStyle(CommonUtils.getDefaultTheme()?.popupForm?.button?.bold!!))
            tvClose.setTextColor(Color.parseColor(CommonUtils.getDefaultTheme()?.popupForm?.button?.fgColor))
            tvTitle.setTextAppearance(ResUtils.getTextViewStyle(CommonUtils.getDefaultTheme()?.popupForm?.button?.bold!!))
            tvTitle.setTextColor(Color.parseColor(CommonUtils.getDefaultTheme()?.popupForm?.button?.fgColor))
            tvTitle.textSize =
                ResUtils.getTextSize(CommonUtils.getDefaultTheme()?.popupForm?.button!!)
            tvClose.textSize =
                ResUtils.getTextSize(CommonUtils.getDefaultTheme()?.popupForm?.button!!)
            triangle?.setColorFilter(Color.parseColor(toolbarColor),
                PorterDuff.Mode.SRC_IN)
            layoutTopBar.apply {
                tvDetails.setOnClickListener {
                    if (currentTab != 0){
                        tvDetails.background =
                            ContextCompat.getDrawable(context, R.drawable.bg_round_grizzle_8dp)
                        tvHistory.background = null
                        details.visibility = GONE
                        layoutProgress.visibility = VISIBLE
                        history.visibility = GONE
                        onDetailClicked()
                        currentTab = 0
                    }
                }

                tvHistory.setOnClickListener {
                    if (currentTab != 1){
                        tvDetails.background = null
                        tvHistory.background =
                            ContextCompat.getDrawable(context, R.drawable.bg_round_grizzle_8dp)

                        details.visibility = GONE
                        layoutProgress.visibility = VISIBLE
                        history.visibility = GONE
                        onHistoryClicked()
                        currentTab = 1
                    }
                }

                tvClose.setOnClickListener {
                    dismissDialog()
                }
            }
        }

        onDetailClicked()

        dialog?.setOnDismissListener {
            isDialogRunning = false
        }

        isDialogRunning = true
        PopupUtils.setDefaultDialogProperty(dialog!!)
        dialog?.show()
    }

    fun dismissDialog() {
        dialog?.dismiss()
    }

    fun updateHistoryView(context: Context, historyList: List<PDFHistory>){
        val historyAdapter = PDFHistoryAdapter(){ position ->
            onHistoryItemClicked(context, historyList[position].description)
        }
        mBinding.apply {
            history.visibility = VISIBLE
            layoutProgress.visibility = GONE
            layoutHistory.recyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = historyAdapter
                historyAdapter.setItems(historyList)
            }
        }
    }

    private fun onHistoryItemClicked(context: Context, pdfDetailId: String){
        PDFDetailDescriptionDialog.openDialog(context, PreferenceManager.toolbarColor , pdfDetailId)
    }

    fun updateDetailView(
        context: Context,
        pdfDetails: PDFDetails,
        pdfProperties: HashMap<String, String>,
        pdfMetaDataKeyList: List<String>
    ) {
        val detailAdapter  = PDFDetailAdapter()
        mBinding.apply {
            details.visibility = VISIBLE
            layoutProgress.visibility = GONE

            pdfDetails.apply {
                """${category ?: ""} ${status ?: ""}""".also { layoutDetails.tvTitle.text = it }
            }

            layoutDetails.recyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = detailAdapter
                detailAdapter.setValue(pdfProperties, pdfMetaDataKeyList)
            }
        }
    }

    companion object {
        private var instance: PDFDetailDialog? = null
        private val Instance: PDFDetailDialog
            get() {
                if (instance == null) {
                    instance = PDFDetailDialog()
                }
                return instance!!
            }

        fun openDialog(
            context: Context,
            toolbarColor: String,
            onDetailClicked: () -> Unit,
            onHistoryClicked: () -> Unit
        ) {
            Instance.openDialog(context, toolbarColor,  {onDetailClicked()}, {onHistoryClicked()})
        }

        fun updateDetails(
            context: Context,
            pdfDetails: PDFDetails,
            pdfProperties: HashMap<String, String>,
            pdfMetaDataKeyList: List<String>
        ) {
            if (Instance.isDialogRunning)
                Instance.updateDetailView(context, pdfDetails ,pdfProperties, pdfMetaDataKeyList)
        }

        fun updateHistoryView(
            context: Context,
            history: List<PDFHistory>
        ){
            if (Instance.isDialogRunning)
                Instance.updateHistoryView(context, history)
        }

        fun dismissDialog() {
            Instance.dismissDialog()
        }
    }
}