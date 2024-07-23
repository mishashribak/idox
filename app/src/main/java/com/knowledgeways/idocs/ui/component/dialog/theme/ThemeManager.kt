package com.knowledgeways.idocs.ui.component.dialog.theme

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.Window
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.databinding.LayoutThemeManagerBinding
import com.knowledgeways.idocs.db.PreferenceManager
import com.knowledgeways.idocs.network.model.theme.Theme
import com.knowledgeways.idocs.ui.component.dialog.organization.OrganizationAdapter
import com.knowledgeways.idocs.ui.component.dialog.settings.SettingsDialog
import com.knowledgeways.idocs.utils.CommonUtils
import com.knowledgeways.idocs.utils.ConverterUtils
import com.knowledgeways.idocs.utils.PopupUtils
import com.knowledgeways.idocs.utils.ResUtils

class ThemeManager {

    private lateinit var themeAdapter: ThemeAdapter
    var isDialogRunning = false
    var dialog: Dialog? = null
    private lateinit var listener: OnSelectedListener

    fun openDialog(
        context: Context,
        toolbarColor: String,
        toolbarTextColor: String,
        onDismiss: () -> Unit,
    ) {
        dialog = Dialog(context)

        val binding = DataBindingUtil.inflate<LayoutThemeManagerBinding>(
            LayoutInflater.from(context),
            R.layout.layout_theme_manager,
            null,
            false
        )

        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setContentView(binding.root)

        if (!::themeAdapter.isInitialized) {
            themeAdapter = ThemeAdapter(listener)
        }

        binding?.apply {
            if (toolbarColor.isNotEmpty()) {
                ivBackground.setColorFilter(Color.parseColor(toolbarColor), PorterDuff.Mode.SRC_IN)
            }

            tvSettings.setTextAppearance(ResUtils.getTextViewStyle(CommonUtils.getDefaultTheme()?.popupForm?.button?.bold!!))
            tvTitle.setTextAppearance(ResUtils.getTextViewStyle(CommonUtils.getDefaultTheme()?.popupForm?.button?.bold!!))

            if (toolbarTextColor.isNotEmpty()) {
                tvSettings.setTextColor(Color.parseColor(toolbarTextColor))
                tvTitle.setTextColor(Color.parseColor(toolbarTextColor))
            }

            tvTitle.textSize =
                ResUtils.getTextSize(CommonUtils.getDefaultTheme()?.popupForm?.button!!)
            tvSettings.textSize =
                ResUtils.getTextSize(CommonUtils.getDefaultTheme()?.popupForm?.button!!)


            tvSettings.setOnClickListener {
                dismissDialog()
                listener.onSettingsClicked()
            }

            themeAdapter.setItems(ConverterUtils.getThemeList() ?: ArrayList())
            rvTheme.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = themeAdapter
            }

        }

        isDialogRunning = true

        PopupUtils.setDefaultDialogProperty(dialog!!)
        dialog?.show()
    }

    fun resetThemeAdapter() {
        themeAdapter.setItems(ConverterUtils.getThemeList() ?: ArrayList())
    }

    fun dismissDialog() {
        isDialogRunning = false
        dialog?.dismiss()
    }

    companion object {
        private var instance: ThemeManager? = null
        private val Instance: ThemeManager
            get() {
                if (instance == null) {
                    instance = ThemeManager()
                }
                return instance!!
            }

        fun openDialog(
            context: Context,
            toolbarColor: String,
            toolbarTextColor: String,
            onDismiss: () -> Unit
        ) {
            Instance.openDialog(context, toolbarColor, toolbarTextColor) {
                onDismiss()
            }
        }

        fun setSelectedListener(listener: OnSelectedListener) {
            Instance.listener = listener
        }

        fun dismissDialog() {
            Instance.dismissDialog()
        }

        fun resetThemeAdapter() {
            Instance.resetThemeAdapter()
        }
    }

    interface OnSelectedListener {
        fun onDownloadClicked(theme: Theme)
        fun onDeleteClicked(theme: Theme)
        fun onCheckSelected(theme: Theme)
        fun onEditSelected(theme: Theme)
        fun onSettingsClicked()
    }
}