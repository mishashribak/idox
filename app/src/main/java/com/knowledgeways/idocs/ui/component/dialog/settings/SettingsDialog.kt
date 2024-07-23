package com.knowledgeways.idocs.ui.component.dialog.settings

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.Window
import android.widget.CompoundButton
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.databinding.LayoutSettingsBinding
import com.knowledgeways.idocs.utils.CommonUtils
import com.knowledgeways.idocs.utils.FileUtils
import com.knowledgeways.idocs.utils.PopupUtils
import com.knowledgeways.idocs.utils.ResUtils


class SettingsDialog {
    var isDialogRunning = false
    var dialog: Dialog? = null
    var listener: OnSelectedListener ?= null

    fun openDialog(
        context: Context,
        toolbarColor: String,
        toolbarTextColor: String,
        onDismiss: () -> Unit,
    ) {
        dialog = Dialog(context)

        val binding = DataBindingUtil.inflate<LayoutSettingsBinding>(
            LayoutInflater.from(context),
            R.layout.layout_settings,
            null,
            false
        )

        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setContentView(binding.root)


        binding?.apply {
            if (toolbarColor.isNotEmpty()) {
                ivBackground.setColorFilter(Color.parseColor(toolbarColor), PorterDuff.Mode.SRC_IN)
            }

            if (toolbarTextColor.isNotEmpty()) {
                tvClose.setTextAppearance(ResUtils.getTextViewStyle(CommonUtils.getDefaultTheme()?.popupForm?.button?.bold!!))
                tvTitle.setTextAppearance(ResUtils.getTextViewStyle(CommonUtils.getDefaultTheme()?.popupForm?.button?.bold!!))

                tvClose.setTextColor(Color.parseColor(toolbarTextColor))
                tvTitle.setTextColor(Color.parseColor(toolbarTextColor))
                tvTitle.textSize =
                    ResUtils.getTextSize(CommonUtils.getDefaultTheme()?.popupForm?.button!!)
                tvClose.textSize =
                    ResUtils.getTextSize(CommonUtils.getDefaultTheme()?.popupForm?.button!!)
            }

     /*       tvClearTempFiles.apply {
                text =
                    context.resources.getString(  if (FileUtils.isTempPDFExisting(context)) R.string.str_clear_temp_files
                    else R.string.str_temp_files_cleared)
            }*/

            layoutClearTempFiles.setOnClickListener {
                listener!!.onTempFileDeleted()
                tvClearTempFiles.text = context.resources.getString(R.string.str_temp_files_cleared)
            }


            layoutSyncData.setOnClickListener {
                listener!!.onSyncUserDataSelected()
            }

            layoutThemeManager.setOnClickListener {
                listener!!.onThemeManagerSelected()
            }

            tvDeleteOfflineActions.apply {
                isEnabled = false
                isClickable = false
                setTextColor(ContextCompat.getColor(context, R.color.color_grey_500))
            }

            tvClose.setOnClickListener {
                dialog!!.dismiss()
            }

            seekbarPageSizeValue.setOnSeekBarChangeListener(
                object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekbar: SeekBar?, progress: Int, fromUser: Boolean) {
               //         tvSeekbarValue.text = progress.toString()
                        listener!!.onPageSizeSelected(progress)
                    }

                    override fun onStartTrackingTouch(p0: SeekBar?) {

                    }

                    override fun onStopTrackingTouch(p0: SeekBar?) {

                    }
                }
            )

            switchNightReadMode.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                // do something, the isChecked will be
                // true if the switch is in the On position
                listener!!.onNightModeSelected(isChecked)
            })

            layoutSideScrolling.setOnClickListener {
                ivTickSideScrolling.visibility = VISIBLE
                ivTickContinueReading.visibility = GONE
                ivTickPageCurl.visibility = GONE
                listener!!.onScrollingModeSelected(1)
            }

            layoutContinueReading.setOnClickListener {
                ivTickSideScrolling.visibility = GONE
                ivTickContinueReading.visibility = VISIBLE
                ivTickPageCurl.visibility = GONE
                listener!!.onScrollingModeSelected(2)
            }

            layoutPageCurl.setOnClickListener {
                ivTickSideScrolling.visibility = GONE
                ivTickContinueReading.visibility = GONE
                ivTickPageCurl.visibility = VISIBLE
                listener!!.onScrollingModeSelected(3)
            }

            layoutHorizontal.setOnClickListener {
                ivTickHorizontal.visibility = VISIBLE
                ivTickVertical.visibility = GONE
                listener!!.onPageTurnOptionSelected(1)
            }

            layoutVertical.setOnClickListener {
                ivTickHorizontal.visibility = GONE
                ivTickVertical.visibility = VISIBLE
                listener!!.onPageTurnOptionSelected(2)
            }

            layoutSinglePage.setOnClickListener {
                ivTickSinglePage.visibility = VISIBLE
                ivTwoPage.visibility = GONE
                ivAutoRotate.visibility = GONE
                listener!!.onPageFlippingSelected(1)
            }

            layoutTwoPage.setOnClickListener {
                ivTickSinglePage.visibility = GONE
                ivTwoPage.visibility = VISIBLE
                ivAutoRotate.visibility = GONE
                listener!!.onPageFlippingSelected(2)
            }

            layoutAutoRotate.setOnClickListener {
                ivTickSinglePage.visibility = GONE
                ivTwoPage.visibility = GONE
                ivAutoRotate.visibility = VISIBLE
                listener!!.onPageFlippingSelected(3)
            }

            layoutOfflineActions.setOnClickListener {
                listener!!.onDeleteOfflineAction()
            }

        }

        isDialogRunning = true

        PopupUtils.setDefaultDialogProperty(dialog!!)
        dialog?.show()
    }

    fun dismissDialog() {
        isDialogRunning = false
        dialog?.dismiss()
    }

    companion object {
        private var instance: SettingsDialog? = null
        private val Instance: SettingsDialog
            get() {
                if (instance == null) {
                    instance = SettingsDialog()
                }
                return instance!!
            }

        fun openDialog(
            context: Context,
            toolbarColor: String,
            toolbarTextColor: String,
            onDismiss: () -> Unit
        ) {
            Instance.openDialog(context , toolbarColor, toolbarTextColor){
                onDismiss()
            }
        }

        fun isDialogRunning(): Boolean {
            return Instance.isDialogRunning
        }

        fun setSelectedListener(listener: OnSelectedListener){
            Instance.listener = listener
        }

        fun setDialogRunning(mRunning: Boolean){
            Instance.isDialogRunning = mRunning
        }

        fun dismissDialog() {
            return Instance.dismissDialog()
        }
    }

    interface OnSelectedListener{
        fun onTempFileDeleted()
        fun onSyncUserDataSelected()
        fun onThemeManagerSelected()
        fun onPageSizeSelected(pageSize: Int)
        fun onNightModeSelected(boolean: Boolean)
        fun onScrollingModeSelected(mode: Int)
        fun onPageTurnOptionSelected(value : Int)
        fun onPageFlippingSelected(value : Int)
        fun onDeleteOfflineAction()
    }
}