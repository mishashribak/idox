package com.knowledgeways.idocs.ui.component.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.databinding.LayoutDownloadProgressBinding
import com.knowledgeways.idocs.network.model.DownloadStatus
import com.knowledgeways.idocs.utils.PopupUtils

class DialogPDFDownloadProgress {
    var isDialogRunning = false
    var dialog: Dialog? = null

    var mTvProgress: TextView? = null
    var mTvDescription: TextView? = null
    var mProgressBar: ProgressBar? = null
    var mContext: Context? = null
    var mCountDownTimer: CountDownTimer? = null
    var selfProgress = 0

    fun openDialog(
        context: Context,
        onDismiss: () -> Unit,
    ) {
        dialog = Dialog(context)
        mContext = context

        val binding = DataBindingUtil.inflate<LayoutDownloadProgressBinding>(
            LayoutInflater.from(context),
            R.layout.layout_download_progress,
            null,
            false
        )

        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setContentView(binding.root)

        binding?.apply {
            mTvProgress = tvProgress
            mTvDescription = tvDescription
            mProgressBar = progressHorizontal
            progressHorizontal.visibility = View.VISIBLE
            progressDefault.visibility = View.GONE
        }

        initCountDownTimer()
        isDialogRunning = true

        dialog?.setOnDismissListener {
            onDismiss()
            isDialogRunning = false
        }
        PopupUtils.setDefaultDialogProperty(dialog!!)
        dialog?.show()
    }

    fun initCountDownTimer() {
        if (mCountDownTimer == null) {
            mCountDownTimer = object : CountDownTimer(120 * 1000, 1200) {
                override fun onTick(millisUntilFinished: Long) {
                    selfProgress += 1
                    mProgressBar?.progress = selfProgress
                    "${selfProgress}%".also { mTvProgress?.text = it }
                }

                override fun onFinish() {
                    //Do what you want

                }
            }
            mCountDownTimer!!.start()
        }
    }

    fun updateProgressStatus(status: DownloadStatus) {
        val currentProgress =
            (mProgressBar?.progress ?: 0) + ((status.progress * (100 - (mProgressBar?.progress
                ?: 0))) / 100)
        mProgressBar?.progress = currentProgress
        "${currentProgress}%".also { mTvProgress?.text = it }
        if (currentProgress == 100) dismissDialog()
    }

    fun dismissDialog() {
        mCountDownTimer?.cancel()
        isDialogRunning = false
        dialog?.dismiss()
    }

    companion object {
        private var instance: DialogPDFDownloadProgress? = null
        private val Instance: DialogPDFDownloadProgress
            get() {
                if (instance == null) {
                    instance = DialogPDFDownloadProgress()
                }
                return instance!!
            }

        fun openDialog(
            context: Context,
            onDismiss: () -> Unit
        ) {
            Instance.openDialog(context) {
                onDismiss()
            }
        }

        fun updateProgressStatus(status: DownloadStatus) {

            Instance.updateProgressStatus(status)
        }
    }
}