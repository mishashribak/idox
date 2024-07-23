package com.knowledgeways.idocs.ui.component.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Window
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.databinding.DialogOtpBinding
import com.knowledgeways.idocs.utils.CommonUtils
import com.knowledgeways.idocs.utils.PopupUtils
import com.knowledgeways.idocs.utils.ResUtils

class DialogOtp {

    var isDialogRunning = false
    var dialog: Dialog? = null
    var errorTextView: TextView ?= null

    fun openDialog(
        context: Context,
        toolbarColor: String,
        toolbarTextColor: String,
        message: String,
        onDismiss: (otpCode: String) -> Unit
    ) {
        dialog = Dialog(context)

        val binding = DataBindingUtil.inflate<DialogOtpBinding>(
            LayoutInflater.from(context),
            R.layout.dialog_otp,
            null,
            false
        )

        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setContentView(binding.root)

        binding.apply {

            errorTextView = tvMessageError

            tvTitle.apply {
                setTextAppearance(ResUtils.getTextViewStyle(CommonUtils.getDefaultTheme()?.popupForm?.button?.bold!!))
             //   setTextColor(Color.parseColor(toolbarTextColor))
            }

            tvMessage.apply {
                text = message
                setTextAppearance(ResUtils.getTextViewStyle(CommonUtils.getDefaultTheme()?.popupForm?.button?.bold!!))
         //       setTextColor(Color.parseColor(toolbarTextColor))
            }

            tvCancel.setOnClickListener {
                dismissDialog()
            }

            tvSubmit.apply {
                setTextAppearance(ResUtils.getTextViewStyle(CommonUtils.getDefaultTheme()?.popupForm?.button?.bold!!))

                setOnClickListener {
                    val otpValue = edittextOtp.text.toString().trim()
                    if (otpValue.isNotEmpty()) {
                        onDismiss(otpValue)
                    //    dialog?.dismiss()
                    }
                }
            }
        }

        dialog?.setOnDismissListener {
            isDialogRunning = false
        }

        isDialogRunning = true
        PopupUtils.setDefaultDialogProperty(dialog!!)
        dialog?.show()
    }

    fun setErrorMessage(errorMessage: String){
        errorTextView?.text = errorMessage
    }

    fun mDialogRunning(): Boolean{
        return isDialogRunning
    }

    fun dismissDialog(){
        errorTextView?.text = ""
        dialog?.dismiss()
    }

    companion object {
        private var instance: DialogOtp? = null
        private val Instance: DialogOtp
            get() {
                if (instance == null) {
                    instance = DialogOtp()
                }
                return instance!!
            }

        fun openDialog(
            context: Context,
            toolbarColor: String,
            toolbarTextColor: String,
            message: String,
            onDismiss: (mOtpCode: String) -> Unit,
        ) {
            Instance.openDialog(context, toolbarColor, toolbarTextColor, message) { mOtpCode ->
                onDismiss(mOtpCode)
            }
        }

        fun setErrorMessage(errorMessage: String){
            return Instance.setErrorMessage(errorMessage)
        }

        fun isDialogRunning(): Boolean{
            return  Instance.mDialogRunning()
        }

        fun dismissDialog(){
            Instance.dismissDialog()
        }
    }
}