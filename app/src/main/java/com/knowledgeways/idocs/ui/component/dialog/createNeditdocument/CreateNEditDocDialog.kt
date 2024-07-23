package com.knowledgeways.idocs.ui.component.dialog.createNeditdocument

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.Window
import androidx.databinding.DataBindingUtil
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.databinding.LayoutCreateEditDocumentBinding
import com.knowledgeways.idocs.ui.component.dialog.createoption.CreateOptionDialog
import com.knowledgeways.idocs.utils.CommonUtils
import com.knowledgeways.idocs.utils.PopupUtils
import com.knowledgeways.idocs.utils.ResUtils

class CreateNEditDocDialog {

    var isDialogRunning = false
    var dialog: Dialog? = null

    fun openDialog(
        context: Context, toolbarColor: String,
        toolbarTextColor: String,
        isCreate: Boolean,
        isInternal: Boolean
    ){
        dialog = Dialog(context)

        val binding = DataBindingUtil.inflate<LayoutCreateEditDocumentBinding>(
            LayoutInflater.from(context),
            R.layout.layout_create_edit_document,
            null,
            false
        )

        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setContentView(binding.root)

        binding?.apply {
            val clToolbarTextColor = Color.parseColor(toolbarTextColor)
            if (toolbarColor.isNotEmpty()) {
                val clToolbarColor = Color.parseColor(toolbarColor)
                ivBackground.setColorFilter(clToolbarColor, PorterDuff.Mode.SRC_IN)
                tvSave.backgroundTintList =
                    ColorStateList.valueOf(clToolbarColor)
                tvSaveNContinue.backgroundTintList =
                    ColorStateList.valueOf(clToolbarColor)
                tvClose.apply {
                    setTextColor(clToolbarColor)
                    dismissDialog()
                }

                ivClose!!.apply {
                    setColorFilter(clToolbarColor, PorterDuff.Mode.SRC_IN)
                    dismissDialog()
                }
            }

            tvSave.setTextColor(clToolbarTextColor)
            tvSaveNContinue.setTextColor(clToolbarTextColor)

            tvTitle.apply {
                setTextAppearance(ResUtils.getTextViewStyle(CommonUtils.getDefaultTheme()?.popupForm?.button?.bold!!))
                setTextColor(clToolbarTextColor)
                textSize =
                    ResUtils.getTextSize(CommonUtils.getDefaultTheme()?.popupForm?.button!!)
                text = context.resources.getString(if (isInternal) R.string.str_create_internal_outgoing else R.string.str_create_external_outgoing)
            }

            PopupUtils.setDefaultDialogProperty(dialog!!)
            dialog?.show()
        }
    }

    fun dismissDialog() {
        isDialogRunning = false
        dialog?.dismiss()
    }

    companion object {
        private var instance: CreateNEditDocDialog? = null
        private val Instance: CreateNEditDocDialog
            get() {
                if (instance == null) {
                    instance = CreateNEditDocDialog()
                }
                return instance!!
            }

        fun openDialog(
            context: Context,
            toolbarColor: String,
            toolbarTextColor: String,
            isCreate: Boolean,  // true: Create false: Edit
            isInternal: Boolean,
        ){
            if (!isDialogRunning()){
                Instance.openDialog(context, toolbarColor, toolbarTextColor , isCreate, isInternal)
            }
        }

        fun isDialogRunning(): Boolean {
            return Instance.isDialogRunning
        }

        fun dismissDialog() {
            return Instance.dismissDialog()
        }
    }
}