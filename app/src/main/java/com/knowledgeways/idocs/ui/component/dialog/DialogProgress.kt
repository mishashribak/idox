package com.knowledgeways.idocs.ui.component.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Window
import androidx.databinding.DataBindingUtil
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.databinding.LayoutProgressBinding
import com.knowledgeways.idocs.databinding.LayoutThemeManagerBinding
import com.knowledgeways.idocs.ui.component.dialog.theme.ThemeManager
import com.knowledgeways.idocs.utils.PopupUtils

class DialogProgress {

    var isDialogRunning = false
    var dialog: Dialog? = null

    fun openDialog(
        context: Context,
    ){
        dialog = Dialog(context)

        val binding = DataBindingUtil.inflate<LayoutProgressBinding>(
            LayoutInflater.from(context),
            R.layout.layout_progress,
            null,
            false
        )

        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setContentView(binding.root)
        dialog?.setCancelable(false)

        isDialogRunning = true

        PopupUtils.setDefaultDialogProperty(dialog!!)
        dialog?.show()
    }

    fun dismissDialog(){
        isDialogRunning = false
        dialog?.dismiss()
    }

    companion object {
        private var instance: DialogProgress? = null
        private val Instance: DialogProgress
            get() {
                if (instance == null) {
                    instance = DialogProgress()
                }
                return instance!!
            }

        fun openDialog(
            context: Context,
        ) {
            if (!Instance.isDialogRunning){
                Instance.openDialog(context)
            }
        }

        fun dismissDialog(){
            Instance.dismissDialog()
        }

        fun isDialogRunning():Boolean{
            return Instance.isDialogRunning
        }
    }
}