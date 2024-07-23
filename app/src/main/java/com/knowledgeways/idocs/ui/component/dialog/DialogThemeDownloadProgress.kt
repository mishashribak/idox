package com.knowledgeways.idocs.ui.component.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.Window
import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.databinding.LayoutDownloadProgressBinding
import com.knowledgeways.idocs.network.model.DownloadStatus
import com.knowledgeways.idocs.utils.AppConstants
import com.knowledgeways.idocs.utils.PopupUtils

class DialogThemeDownloadProgress {
    var isDialogRunning = false
    var dialog: Dialog? = null

    var mTvProgress: TextView? = null
    var mTvDescription: TextView? = null
    var mProgressBar: ProgressBar? = null
    var mContext: Context ?= null
    var mCountDownTimer: CountDownTimer? = null
    var selfProgress = 0
    var filetype: Int = AppConstants.FILE_TYPE_IMAGE

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
            progressHorizontal.visibility = VISIBLE
            progressDefault.visibility = GONE
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

    fun initCountDownTimer(){
        if (mCountDownTimer == null){
            mCountDownTimer = object : CountDownTimer(120* 1000, 1200) {
                override fun onTick(millisUntilFinished: Long) {
                    if (filetype == AppConstants.FILE_TYPE_IMAGE){
                        selfProgress+= 1
                        mProgressBar?.progress = selfProgress
                        "${selfProgress}%".also { mTvProgress?.text = it }
                    }
                }

                override fun onFinish() {
                    //Do what you want

                }
            }
            mCountDownTimer!!.start()
        }
    }

    fun updateProgressStatus(status: DownloadStatus) {
        filetype = status.fileType
        if (status.fileType == AppConstants.FILE_TYPE_ICON) mCountDownTimer!!.cancel()
        mTvDescription?.text = when (status.fileType) {
            -1 -> mContext?.resources?.getString(R.string.str_fetching_theme_list)
            AppConstants.FILE_TYPE_IMAGE -> mContext?.resources?.getString(R.string.str_downloading_theme_images)
            else -> mContext?.resources?.getString(R.string.str_downloading_theme_icons)
        }
        if (status.fileType == AppConstants.FILE_TYPE_IMAGE) {
            val currentProgress =
                (mProgressBar?.progress?:0) + ((status.progress * (100 - (mProgressBar?.progress?:0))) / 100)
            mProgressBar!!.progress = currentProgress
            "${currentProgress}%".also { mTvProgress?.text = it }
        } else {
            mProgressBar!!.progress = status.progress
            "${status.progress}%".also { mTvProgress?.text = it }
        }

        if (status.unzipFinished){
            dismissDialog()
        }
    }

    fun dismissDialog(){
        mCountDownTimer?.cancel()
        isDialogRunning = false
        dialog?.dismiss()
    }

    companion object {
        private var instance: DialogThemeDownloadProgress? = null
        private val Instance: DialogThemeDownloadProgress
            get() {
                if (instance == null) {
                    instance = DialogThemeDownloadProgress()
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