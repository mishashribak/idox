package com.knowledgeways.idocs.ui.component.dialog.pdfreply

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Window
import androidx.databinding.DataBindingUtil
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.databinding.LayoutReplyPdfBinding
import com.knowledgeways.idocs.network.model.documentdetail.Transfer
import com.knowledgeways.idocs.utils.PopupUtils

class PDFReplyDialog {

    var isDialogRunning = false
    var dialog: Dialog? = null
    lateinit var mReplyClicked :() -> Unit

    lateinit var mBinding: LayoutReplyPdfBinding


    fun openDialog(
        context: Context,
        transferModel: Transfer?,
        onReplyClicked: () -> Unit
    ){
        dialog = Dialog(context)
        mReplyClicked = onReplyClicked

        val binding = DataBindingUtil.inflate<LayoutReplyPdfBinding>(
            LayoutInflater.from(context),
            R.layout.layout_reply_pdf,
            null,
            false
        )

        mBinding = binding
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setContentView(binding.root)

        binding.apply {
      //      tvClose?.setTextAppearance(ResUtils.getTextViewStyle(CommonUtils.getDefaultTheme()?.popupForm?.button?.bold!!))
     //       tvClose?.setTextColor(Color.parseColor(CommonUtils.getDefaultTheme()?.popupForm?.button?.fgColor))
    //        tvTitle?.setTextAppearance(ResUtils.getTextViewStyle(CommonUtils.getDefaultTheme()?.popupForm?.button?.bold!!))
    //        tvTitle?.setTextColor(Color.parseColor(CommonUtils.getDefaultTheme()?.popupForm?.button?.fgColor))
    //        tvTitle?.textSize =
    //            ResUtils.getTextSize(CommonUtils.getDefaultTheme()?.popupForm?.button!!)

            tvTo?.text = transferModel?.replyName ?: ""
      //      tvClose?.textSize =
     //           ResUtils.getTextSize(CommonUtils.getDefaultTheme()?.popupForm?.button!!)

            tvClose?.setOnClickListener {
                dialog?.dismiss()
            }

            layoutReply.setOnClickListener {
                dialog?.dismiss()
            }
        }

        dialog?.setOnDismissListener {
            isDialogRunning = false
        }

        isDialogRunning = true
        PopupUtils.setDefaultDialogProperty(dialog!!)
        dialog?.show()
    }

    companion object {
        private var instance: PDFReplyDialog? = null
        private val Instance: PDFReplyDialog
            get() {
                if (instance == null) {
                    instance = PDFReplyDialog()
                }
                return instance!!
            }

        fun openDialog(
            context: Context,
            transferModel: Transfer?,
            onLinkClicked: () -> Unit
        ) {
            Instance.openDialog(context, transferModel) {
                onLinkClicked()
            }
        }
    }
}