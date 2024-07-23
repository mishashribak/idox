package com.knowledgeways.idocs.ui.component.dialog.externalorganization

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Window
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.databinding.LayoutExternalOrganizationsBinding
import com.knowledgeways.idocs.db.PreferenceManager
import com.knowledgeways.idocs.network.model.user.Organization
import com.knowledgeways.idocs.ui.component.dialog.organization.OrganizationAdapter
import com.knowledgeways.idocs.utils.CommonUtils
import com.knowledgeways.idocs.utils.PopupUtils
import com.knowledgeways.idocs.utils.ResUtils

class ExternalOrganizationPicker : OrganizationAdapter.OnOrganizationClickedListener {

    private var organizationAdapter = OrganizationAdapter()
    var isDialogRunning = false
    var dialog: Dialog? = null
    lateinit var mItemSelected : (organization: Organization)-> Unit

    fun setOrganizationList(organizationList: List<Organization>) {
        organizationAdapter.setItems(organizationList)
    }

    fun openDialog(
        context: Context, toolbarColor: String,
        onItemClick: (organization: Organization) -> Unit,
        onDismiss: () -> Unit,
    ) {
        dialog = Dialog(context)

        val binding = DataBindingUtil.inflate<LayoutExternalOrganizationsBinding>(
            LayoutInflater.from(context),
            R.layout.layout_external_organizations,
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
            tvClose?.setTextAppearance(ResUtils.getTextViewStyle(CommonUtils.getDefaultTheme()?.popupForm?.button?.bold!!))
            tvClose?.setTextColor(Color.parseColor(CommonUtils.getDefaultTheme()?.popupForm?.button?.fgColor))
            tvTitle?.setTextAppearance(ResUtils.getTextViewStyle(CommonUtils.getDefaultTheme()?.popupForm?.button?.bold!!))
            tvTitle?.setTextColor(Color.parseColor(CommonUtils.getDefaultTheme()?.popupForm?.button?.fgColor))

            edittextSearch.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                    }

                    override fun afterTextChanged(textChanged: Editable?) {
                        organizationAdapter.setFilterString(textChanged.toString().trim())
                    }
                }
            )

        }

        initRecyclerView(context, binding?.recyclerView!!)
        setOrganizationList(PreferenceManager.externalOrganization)
        dialog?.setOnDismissListener {
            isDialogRunning = false
            onDismiss()
        }

        isDialogRunning = true


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
        private var instance: ExternalOrganizationPicker? = null
        private val Instance: ExternalOrganizationPicker
            get() {
                if (instance == null) {
                    instance = ExternalOrganizationPicker()
                }
                return instance!!
            }

        fun setOrganizationList(organizationList: List<Organization>) {
            Instance.setOrganizationList(organizationList)
        }

        fun openDialog(
            context: Context,
            toolbarColor: String,
            onItemClick: (organization : Organization)-> Unit,
            onDismiss: () -> Unit
        ) {
            if (!isDialogRunning()){
                Instance.openDialog(context, toolbarColor,
                    {organization ->  onItemClick(organization) }, {onDismiss()})
            }

        }

        fun isDialogRunning(): Boolean {
            return Instance.isDialogRunning
        }

        fun setDialogRunning(mRunning: Boolean){
            Instance.isDialogRunning = mRunning
        }

        fun dismissDialog() {
            Instance.isDialogRunning = false
            return Instance.dismissDialog()
        }
    }

    override fun onSelected(organization: Organization) {
        mItemSelected(organization)
        dismissDialog()
    }
}