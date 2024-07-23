package com.knowledgeways.idocs.ui.component.dialog.createoption

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import androidx.databinding.DataBindingUtil
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.databinding.LayoutDocumentCreateOptionBinding
import com.knowledgeways.idocs.db.PreferenceManager
import com.knowledgeways.idocs.ui.component.dialog.createNeditdocument.CreateNEditDocDialog
import com.knowledgeways.idocs.utils.PopupUtils

class CreateOptionDialog {

    var isDialogRunning = false
    var dialog: Dialog? = null

    fun openDialog(
        context: Context,
        toolbarColor: String,
        toolbarTextColor: String,
        isCreate: Boolean,
        callInternalOutgoing :() -> Unit,
        callExternalOutgoing :() -> Unit,
    ){
        dialog = Dialog(context)

        val binding = DataBindingUtil.inflate<LayoutDocumentCreateOptionBinding>(
            LayoutInflater.from(context),
            R.layout.layout_document_create_option,
            null,
            false
        )

        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setContentView(binding.root)

        binding.apply {

            tvInternal.visibility =  if (PreferenceManager.user?.privileges?.createInternalOutgoing == true) View.VISIBLE else View.GONE
            tvExternal.visibility = if (PreferenceManager.user?.privileges?.createExternalOutgoing == true) View.VISIBLE else View.GONE

            tvInternal.setOnClickListener {
                callInternalOutgoing()
           //     CreateNEditDocDialog.openDialog(context, toolbarColor, toolbarTextColor,  isCreate, true)
            }

            tvExternal.setOnClickListener {
                callExternalOutgoing()
            //    CreateNEditDocDialog.openDialog(context, toolbarColor, toolbarTextColor, isCreate, false)
            }
        }

        dialog?.setOnDismissListener {
            isDialogRunning = false
        }

        PopupUtils.setDefaultDialogProperty(dialog!!)
        dialog?.show()
    }


    fun dismissDialog() {
        isDialogRunning = false
        dialog?.dismiss()
    }

    companion object {
        private var instance: CreateOptionDialog? = null
        private val Instance: CreateOptionDialog
            get() {
                if (instance == null) {
                    instance = CreateOptionDialog()
                }
                return instance!!
            }

        fun openDialog(
            context: Context,
            toolbarColor: String,
            toolbarTextColor: String,
            isCreate: Boolean,
            callInternalOutgoing :() -> Unit,
            callExternalOutgoing :() -> Unit,
        ){
            if (!isDialogRunning()){
                Instance.openDialog(context, toolbarColor, toolbarTextColor , isCreate, {callInternalOutgoing()}, {callExternalOutgoing()})
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