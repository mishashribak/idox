package com.knowledgeways.idocs.ui.component.dialog.organization

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Window
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.databinding.LayoutOrganizationListBinding
import com.knowledgeways.idocs.network.model.theme.ThemeTextStyle
import com.knowledgeways.idocs.network.model.user.Organization
import com.knowledgeways.idocs.utils.CommonUtils
import com.knowledgeways.idocs.utils.PopupUtils
import com.knowledgeways.idocs.utils.ResUtils

class OrganizationPicker : OrganizationAdapter.OnOrganizationClickedListener {
    private var organizationAdapter = OrganizationAdapter()
    var isDialogRunning = false
    var dialog: Dialog? = null
    lateinit var mItemSelected : (organization: Organization)-> Unit

    fun setOrganizationList(organizationList: List<Organization>) {
        organizationAdapter.setItems(organizationList)
    }

    fun openDialog(
        context: Context, toolbarColor: String,
        toolbarTextColor: String,
        popupButtonForm : ThemeTextStyle?,
        onItemClick: (organization : Organization) -> Unit,
        onDismiss: () -> Unit,
    ) {
        dialog = Dialog(context)

        val binding = DataBindingUtil.inflate<LayoutOrganizationListBinding>(
            LayoutInflater.from(context),
            R.layout.layout_organization_list,
            null,
            false
        )

        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setContentView(binding.root)

        mItemSelected = onItemClick
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
        }

        initRecyclerView(context, binding?.recyclerView!!)

        binding.tvClose.setOnClickListener {
            isDialogRunning = false
            onDismiss()
            dismissDialog()
        }

        isDialogRunning = true

        dialog?.setOnDismissListener {
            isDialogRunning = false
            onDismiss()
        }


        PopupUtils.setDefaultDialogProperty(dialog!!)
        dialog?.show()
    }



    private fun initRecyclerView(context: Context, recyclerView: RecyclerView) {
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = organizationAdapter
        organizationAdapter.listener = this
    }

    fun dismissDialog() {
        isDialogRunning = false
        dialog?.dismiss()
    }

    companion object {
        private var instance: OrganizationPicker? = null
        private val Instance: OrganizationPicker
            get() {
                if (instance == null) {
                    instance = OrganizationPicker()
                }
                return instance!!
            }

        fun setOrganizationList(organizationList: List<Organization>) {
            Instance.setOrganizationList(organizationList)
        }

        fun openDialog(
            context: Context,
            toolbarColor: String,
            toolbarTextColor: String,
            popupButtonForm : ThemeTextStyle?,
            onItemClick: (organization : Organization)-> Unit,
            onDismiss: () -> Unit
        ) {
            if (!isDialogRunning()){
                Instance.openDialog(context, toolbarColor, toolbarTextColor, popupButtonForm,
                    {organizationID ->  onItemClick(organizationID) }, {onDismiss()})
            }
        }

        fun isDialogRunning(): Boolean {
            return Instance.isDialogRunning
        }

        fun setDialogRunning(mRunning: Boolean){
            Instance.isDialogRunning = mRunning
        }

        fun dismissDialog() {
            return Instance.dismissDialog()
        }
    }

    override fun onSelected(organization: Organization) {
        mItemSelected(organization)
    }
}