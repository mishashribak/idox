package com.knowledgeways.idocs.ui.component.dialog.pdf

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import androidx.databinding.DataBindingUtil
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.databinding.LayoutMorePdfToolbarBinding
import com.knowledgeways.idocs.network.model.documentdetail.DocumentDetail
import com.knowledgeways.idocs.ui.component.dialog.settings.SettingsDialog
import com.knowledgeways.idocs.utils.PopupUtils

class DialogMorePDFToolbar {

    var isDialogRunning = false
    var dialog: Dialog? = null
    var listener: OnSelectedListener?= null

    fun openDialog(context: Context, mDocumentDetail: DocumentDetail){
        dialog = Dialog(context)

        val binding = DataBindingUtil.inflate<com.knowledgeways.idocs.databinding.LayoutMorePdfToolbarBinding>(
            LayoutInflater.from(context),
            R.layout.layout_more_pdf_toolbar,
            null,
            false
        )
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setContentView(binding.root)

        binding!!.apply {
            with(mDocumentDetail.acl) {
                layoutThumbnails.visibility = if (documentThumbnails) View.VISIBLE else View.GONE
                layoutPrint.visibility = if (documentPrint) View.VISIBLE else View.GONE
                layoutBookmark.visibility = if (documentBookmarks) View.VISIBLE else View.GONE
         //       layoutSignature.visibility = if (documentAnnotation) View.VISIBLE else View.GONE
                layoutSignature.visibility = if (documentAnnotation) View.GONE else View.GONE
                layoutSearch.visibility = if (documentSearch) View.VISIBLE else View.GONE
            }

            layoutCancel.setOnClickListener {
                dialog?.dismiss()
            }

            layoutBookmark.setOnClickListener {
                dismissDialog()
                listener?.onOutlineClicked()
            }

            layoutSearch.setOnClickListener {
                dismissDialog()
                listener?.onSearchClicked()
            }

            layoutPrint.setOnClickListener {
                dismissDialog()
                listener?.onPrintClicked()
            }

            layoutSignature.setOnClickListener {
                dismissDialog()
                listener?.onSignatureClicked()
            }

            layoutThumbnails.setOnClickListener {
                dismissDialog()
                listener?.onThumbnailClicked()
            }
        }

        dialog?.setOnDismissListener {
            isDialogRunning = false
        }
        isDialogRunning = true
        PopupUtils.setDefaultDialogProperty(dialog!!)
        dialog?.show()
    }

    fun mDialogRunning(): Boolean{
        return isDialogRunning
    }

    fun dismissDialog(){
        dialog?.dismiss()
    }

    companion object {
        private var instance: DialogMorePDFToolbar? = null
        private val Instance: DialogMorePDFToolbar
            get() {
                if (instance == null) {
                    instance = DialogMorePDFToolbar()
                }
                return instance!!
            }

        fun openDialog(
            context: Context, mDocumentDetail: DocumentDetail,

        ) {
            Instance.openDialog(context, mDocumentDetail)
        }

        fun isDialogRunning(): Boolean{
            return  Instance.mDialogRunning()
        }

        fun dismissDialog(){
            Instance.dismissDialog()
        }

        fun setSelectedListener(listener: OnSelectedListener){
            Instance.listener = listener
        }
    }

    interface OnSelectedListener{
        fun onSearchClicked()
        fun onThumbnailClicked()
        fun onOutlineClicked()
        fun onPrintClicked()
        fun onSignatureClicked()

    }
}
