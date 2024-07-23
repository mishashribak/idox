package com.knowledgeways.idocs.ui.component.dialog.userNorgdetail

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.databinding.LayoutOrgUserDetailBinding
import com.knowledgeways.idocs.db.PreferenceManager
import com.knowledgeways.idocs.network.model.ExternalUser
import com.knowledgeways.idocs.ui.pdf.CustomPDFActivity
import com.knowledgeways.idocs.utils.CommonUtils
import com.knowledgeways.idocs.utils.PopupUtils
import com.knowledgeways.idocs.utils.ResUtils


class UserDetailDialog {

    var isDialogRunning = false
    var dialog: Dialog? = null

    lateinit var mBinding: LayoutOrgUserDetailBinding

    var priorityAdapter = PriorityAdapter()
    var actionsAdapter = ActionsAdapter()

    fun openDialog(
        context: Context,
        isFavorite: Boolean,
        user: ExternalUser
    ){

        dialog = Dialog(context)

        val binding = DataBindingUtil.inflate<LayoutOrgUserDetailBinding>(
            LayoutInflater.from(context),
            R.layout.layout_org_user_detail,
            null,
            false
        )

        var isNew = false
        dialog = Dialog(context)

        var selectedOrg = (context as CustomPDFActivity).getSelectedUserFromList(user.userId ?: "0")

        if (selectedOrg == null) {
            isNew = true
            selectedOrg = user
        }


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

            tvAdd.text =
                context.resources.getString(if (isNew) R.string.str_add else R.string.str_update)

            tvTitle.setTextAppearance(ResUtils.getTextViewStyle(CommonUtils.getDefaultTheme()?.popupForm?.button?.bold!!))
            tvTitle.setTextColor(Color.parseColor(CommonUtils.getDefaultTheme()?.popupForm?.button?.fgColor))
            tvTitle.textSize =
                ResUtils.getTextSize(CommonUtils.getDefaultTheme()?.popupForm?.button!!)
            tvAdd.textSize =
                ResUtils.getTextSize(CommonUtils.getDefaultTheme()?.popupForm?.button!!)

            tvAdd.setOnClickListener {
                dialog?.dismiss()
            }

            tvName.text = selectedOrg.fullName ?: ""

            ivTickCc.visibility = if (selectedOrg.forwardType == 1) View.VISIBLE else View.GONE
            ivTickTo.visibility = if (selectedOrg.forwardType == 1) View.GONE else View.VISIBLE

            layoutTo.setOnClickListener {
                forwardType = 0
                ivTickTo.visibility = View.VISIBLE
                ivTickCc.visibility = View.GONE
            }

            layoutCc.setOnClickListener {
                forwardType = 1
                ivTickTo.visibility = View.GONE
                ivTickCc.visibility = View.VISIBLE
            }

            rvPriority.layoutManager = LinearLayoutManager(context)
            priorityAdapter.setItems(PreferenceManager.priorities)
            rvPriority.adapter = priorityAdapter

            rvAction.layoutManager = LinearLayoutManager(context)
            actionsAdapter.setItems(PreferenceManager.actions)
            rvAction.adapter = actionsAdapter

            tvAdd.setOnClickListener {
                if (priorityAdapter.getSelectedValue() == null){
                    PopupUtils.showAlert(context, "Priority selection required")
                }else if (actionsAdapter.getSelectedList().isEmpty()){
                    PopupUtils.showAlert(context, "Actions selection required")
                }else{
                    selectedOrg.actionList = actionsAdapter.getSelectedList()
                    selectedOrg.priorityValue = priorityAdapter.getSelectedValue()
                    selectedOrg.forwardType = forwardType
                    selectedOrg.note = etNote.text.toString().trim()
                    (context as CustomPDFActivity).onAddUserToSelectedList(selectedOrg)
                    dismissDialog()
                }
            }

            tvAddFavorite.visibility = if (isFavorite) View.GONE else View.VISIBLE

            tvAddFavorite.setOnClickListener {
                (context as CustomPDFActivity).onAddUserToFavoriteList(user)
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
        private var instance: UserDetailDialog? = null
        private val Instance: UserDetailDialog
            get() {
                if (instance == null) {
                    instance = UserDetailDialog()
                }
                return instance!!
            }

        fun openDialog(
            context: Context,
            isFavorite: Boolean,
            user: ExternalUser
        ){
            Instance.openDialog(context, isFavorite,  user)
        }

        fun isDialogRunning(): Boolean{
            return Instance.isDialogRunning
        }

        fun dismissDialog(){
            Instance.dismissDialog()
        }
    }
}