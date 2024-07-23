package com.knowledgeways.idocs.ui.component.dialog.userNorgdetail

import android.app.Dialog
import android.app.Notification.Action
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.Window
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.databinding.LayoutOrgUserDetailBinding
import com.knowledgeways.idocs.db.PreferenceManager
import com.knowledgeways.idocs.network.model.user.Organization
import com.knowledgeways.idocs.ui.pdf.CustomPDFActivity
import com.knowledgeways.idocs.utils.AppConstants
import com.knowledgeways.idocs.utils.CommonUtils
import com.knowledgeways.idocs.utils.PopupUtils
import com.knowledgeways.idocs.utils.ResUtils

class OrganizationDetailDialog {

    var isDialogRunning = false
    var dialog: Dialog? = null

    lateinit var mBinding: LayoutOrgUserDetailBinding

    var priorityAdapter = PriorityAdapter()
    var actionsAdapter = ActionsAdapter()

    fun openDialog(
        context: Context,
        isFavorite: Boolean,
        org: Organization,
    ){

        var isNew = false
        dialog = Dialog(context)

       var selectedOrg = (context as CustomPDFActivity).getSelectedOrgFromList(org.id ?: "0")

        if (selectedOrg == null) {
            isNew = true
            selectedOrg = org
        }

        val binding = DataBindingUtil.inflate<LayoutOrgUserDetailBinding>(
            LayoutInflater.from(context),
            R.layout.layout_org_user_detail,
            null,
            false
        )

        var forwardType = 0 // 0: To 1: CC

        mBinding = binding
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setContentView(binding.root)
        isDialogRunning = true
        dialog?.setOnDismissListener {
            isDialogRunning = false
        }

        binding.apply {
            if (com.knowledgeways.idocs.db.PreferenceManager.toolbarColor.isNotEmpty()) {
                val colorStateList =
                    ColorStateList.valueOf(Color.parseColor(com.knowledgeways.idocs.db.PreferenceManager.toolbarColor))
                layoutToolbar.backgroundTintList = colorStateList
            }
            tvAdd.setTextAppearance(ResUtils.getTextViewStyle(CommonUtils.getDefaultTheme()?.popupForm?.button?.bold!!))
            tvAdd.setTextColor(Color.parseColor(CommonUtils.getDefaultTheme()?.popupForm?.button?.fgColor))

            tvAdd.text = context.resources.getString( if (isNew) R.string.str_add else R.string.str_update)

            tvTitle.setTextAppearance(ResUtils.getTextViewStyle(CommonUtils.getDefaultTheme()?.popupForm?.button?.bold!!))
            tvTitle.setTextColor(Color.parseColor(CommonUtils.getDefaultTheme()?.popupForm?.button?.fgColor))
            tvTitle.textSize =
                ResUtils.getTextSize(CommonUtils.getDefaultTheme()?.popupForm?.button!!)
            tvAdd.textSize =
                ResUtils.getTextSize(CommonUtils.getDefaultTheme()?.popupForm?.button!!)

            tvAdd.setOnClickListener {
                dialog?.dismiss()
            }

            tvName.text = selectedOrg.name ?: ""

            ivTickCc.visibility = if (selectedOrg.forwardType == 1) VISIBLE else GONE
            ivTickTo.visibility = if (selectedOrg.forwardType == 1) GONE else VISIBLE

            layoutTo.setOnClickListener {
                forwardType = 0
                ivTickTo.visibility = VISIBLE
                ivTickCc.visibility = GONE
            }

            layoutCc.setOnClickListener {
                forwardType = 1
                ivTickTo.visibility = GONE
                ivTickCc.visibility = VISIBLE
            }

            rvPriority.layoutManager = LinearLayoutManager(context)
            priorityAdapter.setItems(PreferenceManager.priorities)
            rvPriority.adapter = priorityAdapter

            var priorityPosition = -1

            if (!isNew){
                for (i in PreferenceManager.priorities.indices){
                    if (PreferenceManager.priorities[i].label ==selectedOrg.priority?.label){
                        priorityPosition = i
                    }
                }
                if (priorityPosition != -1){
                    priorityAdapter.setSelection(priorityPosition)
                }
            }

            rvAction.layoutManager = LinearLayoutManager(context)

            if (!isNew){
                val actionsList = PreferenceManager.actions as ArrayList
                for (i in actionsList.indices){
                    for(action in selectedOrg.actionList?:ArrayList()){
                        if (action.label == actionsList[i].label){
                            actionsList[i].selected = true
                        }
                    }
                }
                actionsAdapter.setItems(actionsList)
            }else{
                actionsAdapter.setItems(PreferenceManager.actions)
            }

            rvAction.adapter = actionsAdapter

            tvAdd.setOnClickListener {
                if (priorityAdapter.getSelectedValue() == null){
                    PopupUtils.showAlert(context, "Priority selection required")
                }else if (actionsAdapter.getSelectedList().isEmpty()){
                    PopupUtils.showAlert(context, "Actions selection required")
                }else{
                    selectedOrg.actionList = actionsAdapter.getSelectedList()
                    selectedOrg.priority = priorityAdapter.getSelectedValue()
                    selectedOrg.forwardType = forwardType
                    selectedOrg.note = etNote.text.toString().trim()
                    (context as CustomPDFActivity).onAddOrgToSelectedList(selectedOrg)
                    dismissDialog()
                }
            }

            tvAddFavorite.visibility = if (isFavorite) GONE else VISIBLE
            tvAddFavorite.setOnClickListener {
                if (org.type == AppConstants.TYPE_INTERNAL){
                    (context as CustomPDFActivity).onAddOrgToFavoriteClicked(org)
                }else{
                    (context as CustomPDFActivity).onAddSisterOrgToFavoriteClicked(org)
                }
                dismissDialog()
            }

            tvBack.setOnClickListener {
                dismissDialog()
            }
        }

        PopupUtils.setDefaultDialogProperty(dialog!!)
        dialog?.show()
    }

    fun dismissDialog(){
        dialog?.dismiss()
        isDialogRunning = false
    }

    companion object {
        private var instance: OrganizationDetailDialog? = null
        private val Instance: OrganizationDetailDialog
            get() {
                if (instance == null) {
                    instance = OrganizationDetailDialog()
                }
                return instance!!
            }

        fun openDialog(
            context: Context,
            isFavorite: Boolean,
            org: Organization,
        ){
            Instance.openDialog(context, isFavorite,  org)
        }

        fun isDialogRunning(): Boolean{
            return Instance.isDialogRunning
        }

        fun dismissDialog(){
            Instance.dismissDialog()
        }
    }
}