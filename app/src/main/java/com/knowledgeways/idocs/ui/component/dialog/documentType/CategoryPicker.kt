package com.knowledgeways.idocs.ui.component.dialog.documentType

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
import com.knowledgeways.idocs.databinding.LayoutDocumentTypeBinding
import com.knowledgeways.idocs.databinding.LayoutOrganizationListBinding
import com.knowledgeways.idocs.db.PreferenceManager
import com.knowledgeways.idocs.network.model.Category
import com.knowledgeways.idocs.network.model.user.Organization
import com.knowledgeways.idocs.ui.component.dialog.externalorganization.ExternalOrganizationPicker
import com.knowledgeways.idocs.ui.component.dialog.organization.OrganizationAdapter
import com.knowledgeways.idocs.ui.component.dialog.organization.OrganizationPicker
import com.knowledgeways.idocs.utils.CommonUtils
import com.knowledgeways.idocs.utils.PopupUtils
import com.knowledgeways.idocs.utils.ResUtils

class CategoryPicker  : CategoryAdapter.OnCategorySelectedListener {

    var isDialogRunning = false
    var dialog: Dialog? = null
    lateinit var mItemSelected : (category: Category)-> Unit
    private var categoryAdapter = CategoryAdapter()

    fun openDialog(
        context: Context, toolbarColor: String,
        toolbarTextColor: String,
        onItemClick: (category: Category) -> Unit,
        onDismiss: () -> Unit,
    ) {
        dialog = Dialog(context)

        val binding = DataBindingUtil.inflate<LayoutDocumentTypeBinding>(
            LayoutInflater.from(context),
            R.layout.layout_document_type,
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
            tvClose?.setTextAppearance(ResUtils.getTextViewStyle(CommonUtils.getDefaultTheme()?.popupForm?.button?.bold!!))
            tvTitle.setTextAppearance(ResUtils.getTextViewStyle(CommonUtils.getDefaultTheme()?.popupForm?.button?.bold!!))
            tvTitle.setTextColor(Color.parseColor(toolbarTextColor))
            tvClose?.setTextColor(Color.parseColor(toolbarTextColor))
            tvTitle.textSize =
                ResUtils.getTextSize(CommonUtils.getDefaultTheme()?.popupForm?.button!!)
            tvClose?.textSize =
                ResUtils.getTextSize(CommonUtils.getDefaultTheme()?.popupForm?.button!!)
        }

        mItemSelected = onItemClick
        isDialogRunning = true
        initRecyclerView(context, binding?.recyclerView!!)
        setCategoryList(PreferenceManager.categories)

        dialog?.setOnDismissListener {
            isDialogRunning = false
            onDismiss()
        }

        PopupUtils.setDefaultDialogProperty(dialog!!)
        dialog?.show()
    }


    fun setCategoryList(categoryList: List<Category>) {
        categoryAdapter.setItems(categoryList)
    }

    private fun initRecyclerView(context: Context, recyclerView: RecyclerView) {
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = categoryAdapter
        categoryAdapter.listener = this
    }

    fun dismissDialog() {
        isDialogRunning = false
        dialog?.dismiss()
    }

    companion object {
        private var instance: CategoryPicker? = null
        private val Instance: CategoryPicker
            get() {
                if (instance == null) {
                    instance = CategoryPicker()
                }
                return instance!!
            }

        fun openDialog(
            context: Context,
            toolbarColor: String,
            toolbarTextColor: String,
            onItemClick: (category : Category)-> Unit,
            onDismiss: () -> Unit
        ) {
            if (!isDialogRunning()){
                Instance.openDialog(context, toolbarColor, toolbarTextColor,
                    {category ->  onItemClick(category) }, {onDismiss()})
            }
        }

        fun isDialogRunning(): Boolean {
            return Instance.isDialogRunning
        }

        fun dismissDialog() {
            return Instance.dismissDialog()
        }
    }

    override fun onSelected(category: Category) {
        mItemSelected(category)
        dismissDialog()
    }
}