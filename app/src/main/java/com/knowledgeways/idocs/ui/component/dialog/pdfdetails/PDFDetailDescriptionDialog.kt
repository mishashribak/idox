package com.knowledgeways.idocs.ui.component.dialog.pdfdetails

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Window
import androidx.databinding.DataBindingUtil
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.databinding.LayoutPdfDetailsDescriptionBinding
import com.knowledgeways.idocs.utils.CommonUtils
import com.knowledgeways.idocs.utils.PopupUtils
import com.knowledgeways.idocs.utils.ResUtils

class PDFDetailDescriptionDialog {

    var isDialogRunning = false
    var dialog: Dialog? = null

    fun openDialog(
        context: Context,
        toolbarColor: String,
        description: String
    ){
        dialog = Dialog(context)

        val binding = DataBindingUtil.inflate<LayoutPdfDetailsDescriptionBinding>(
            LayoutInflater.from(context),
            R.layout.layout_pdf_details_description,
            null,
            false
        )

        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setContentView(binding.root)

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

            tvDescription.text = description

            tvClose.setOnClickListener { dismissDialog() }
        }

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

    companion object {
        private var instance: PDFDetailDescriptionDialog? = null
        private val Instance: PDFDetailDescriptionDialog
            get() {
                if (instance == null) {
                    instance = PDFDetailDescriptionDialog()
                }
                return instance!!
            }

        fun openDialog(
            context: Context,
            toolbarColor: String,
            documentDetailId: String
        ){
            Instance.openDialog(context, toolbarColor, documentDetailId)
        }

        fun dismissDialog() {
            Instance.dismissDialog()
        }
    }
}