package com.knowledgeways.idocs.utils

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog


object PopupUtils {

    fun showAlert(context: Context, message: String){
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setMessage(message)
            .setCancelable(false)
            .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, id ->
                //do things
            })
        val alert: AlertDialog = builder.create()
        alert.show()
    }

    fun openShareDialog(mContext: Context, shareValue : String){
        val share = Intent(Intent.ACTION_SEND)
        share.type = "text/plain"
        share.putExtra(Intent.EXTRA_TEXT, shareValue)
        mContext.startActivity(Intent.createChooser(share, "Share Text"))
    }

    fun setDefaultDialogProperty(dialog: Dialog) {
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window?.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.MATCH_PARENT
        dialog.window?.attributes = lp
        dialog.setCanceledOnTouchOutside(true)


        dialog.setCancelable(true)
    }
}