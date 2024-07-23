package com.knowledgeways.idocs.ui.component.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Window
import androidx.databinding.DataBindingUtil
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.databinding.LayoutRejectionReasonPdfBinding
import com.knowledgeways.idocs.databinding.LayoutReplyPdfBinding
import com.knowledgeways.idocs.network.model.documentdetail.Transfer
import com.knowledgeways.idocs.utils.KeyboardUtils

class PDFRejectReasonDialog {

    var isDialogRunning = false
    var dialog: Dialog? = null

    lateinit var mBinding: LayoutRejectionReasonPdfBinding

    fun openDialog(
        context: Context,
    ){
        dialog = Dialog(context)

        val binding = DataBindingUtil.inflate<LayoutRejectionReasonPdfBinding>(
            LayoutInflater.from(context),
            R.layout.layout_rejection_reason_pdf,
            null,
            false
        )

        mBinding = binding
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setContentView(binding.root)

        binding.apply {
            layoutReply?.setOnClickListener {
                KeyboardUtils.hideKeyboard(context)
                dialog?.dismiss()
            }

        }
    }

    companion object {
        private var instance: PDFRejectReasonDialog? = null
        private val Instance: PDFRejectReasonDialog
            get() {
                if (instance == null) {
                    instance = PDFRejectReasonDialog()
                }
                return instance!!
            }

        fun openDialog(
            context: Context,
        ) {
            Instance.openDialog(context)
        }
    }
}
