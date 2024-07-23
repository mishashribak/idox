package com.knowledgeways.idocs.ui.component.dialog.pdf.pdfnav

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Window
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.databinding.LayoutMorePdfNavigationBinding
import com.knowledgeways.idocs.network.model.Icon
import com.knowledgeways.idocs.network.model.documentdetail.Acl
import com.knowledgeways.idocs.network.model.documentdetail.DocumentDetail
import com.knowledgeways.idocs.ui.pdf.CustomPDFActivity
import com.knowledgeways.idocs.utils.PopupUtils
import com.knowledgeways.idocs.utils.ResUtils


class DialogMorePDFNavBar {

    var isDialogRunning = false
    var dialog: Dialog? = null
    val pdfAdapter = PDFNavAdapter{pos->
        onNavModeClicked(pos)
    }

    var mContext: Context?= null

    fun openDialog(context: Context, mDocumentDetail: DocumentDetail) {
        dialog = Dialog(context)

        mContext = context
        val binding = DataBindingUtil.inflate<LayoutMorePdfNavigationBinding>(
            LayoutInflater.from(context),
            R.layout.layout_more_pdf_navigation,
            null,
            false
        )
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setContentView(binding.root)



        binding!!.apply {
            val iconList = getSortedIconList(context, mDocumentDetail.acl)
            val gridLayoutManager = GridLayoutManager(context, 4)
            recyclerView.layoutManager = gridLayoutManager
            recyclerView.adapter = pdfAdapter
            pdfAdapter.setItems(iconList)

            tvCancel.setOnClickListener {
                dismissDialog()
            }
        }

        dialog?.setOnDismissListener {
            isDialogRunning = false
        }
        isDialogRunning = true
        PopupUtils.setDefaultDialogProperty(dialog!!)
        dialog?.show()
    }

    private fun getSortedIconList(context: Context, acl: Acl): ArrayList<Icon>{
        val  iconList: ArrayList<Icon> = ArrayList()
        with(acl) {
            
            iconList.add(
                Icon(
                    context.resources.getString(R.string.str_unread),
                    ResUtils.getToolbarIcon(
                        context,
                        context.resources.getString(R.string.pdf_document_unread_icon)
                    )
                )
            )
            
            if (documentShare) iconList.add(
                Icon(
                    context.resources.getString(R.string.str_share),
                    ResUtils.getToolbarIcon(
                        context,
                        context.resources.getString(R.string.pdf_document_share_icon)
                    )
                )
            )

            iconList.add(
                Icon(
                    context.resources.getString(R.string.str_follow_up),
                    ResUtils.getToolbarIcon(
                        context,
                        context.resources.getString(
                            if (documentFollowup)
                            R.string.pdf_document_follow_up_selected_icon else R.string.pdf_document_follow_up_icon
                        )
                    )
                )
            )

            iconList.add(
                Icon(
                    context.resources.getString(R.string.str_favorite),
                    ResUtils.getToolbarIcon(
                        context,
                        context.resources.getString(
                            if (documentFavorite)
                                R.string.pdf_document_favorite_selected_icon else R.string.pdf_document_favorite_icon
                        )
                    )
                )
            )

            if (documentEdit) iconList.add(
                Icon(
                    context.resources.getString(R.string.str_edit),
                    ResUtils.getToolbarIcon(
                        context,
                        context.resources.getString(R.string.pdf_document_edit_icon)
                    )
                )
            )

            if (documentDelete) iconList.add(
                Icon(
                    context.resources.getString(R.string.str_delete),
                    ResUtils.getToolbarIcon(
                        context,
                        context.resources.getString(R.string.pdf_document_delete_icon)
                    )
                )
            )

            if (documentTrash) iconList.add(
                Icon(
                    context.resources.getString(R.string.str_trash),
                    ResUtils.getToolbarIcon(
                        context,
                        context.resources.getString(R.string.pdf_document_trash_icon)
                    )
                )
            )

            if (documentUntrash) iconList.add(
                Icon(
                    context.resources.getString(R.string.str_un_trash),
                    ResUtils.getToolbarIcon(
                        context,
                        context.resources.getString(R.string.pdf_document_un_trash_icon)
                    )
                )
            )

            if (createReplyInternalOutgoing) iconList.add(
                Icon(
                    context.resources.getString(R.string.str_reply_to),
                    ResUtils.getToolbarIcon(
                        context,
                        context.resources.getString(R.string.pdf_document_reply_to_icon)
                    )
                )
            )

            if (createLinkedInternalOutgoing) iconList.add(
                Icon(
                    context.resources.getString(R.string.str_linked_to),
                    ResUtils.getToolbarIcon(
                        context,
                        context.resources.getString(R.string.pdf_document_linked_to_icon)
                    )
                )
            )

            if (documentDCC) iconList.add(
                Icon(
                    context.resources.getString(R.string.str_dcc),
                    ResUtils.getToolbarIcon(
                        context,
                        context.resources.getString(R.string.pdf_document_dcc_icon)
                    )
                )
            )

            if (documentUnarchive) iconList.add(
                Icon(
                    context.resources.getString(R.string.str_unarchive),
                    ResUtils.getToolbarIcon(
                        context,
                        context.resources.getString(R.string.pdf_document_unarchive_icon)
                    )
                )
            )

            if (documentSignSubmit) iconList.add(
                Icon(
                    context.resources.getString(R.string.str_sign_request),
                    ResUtils.getToolbarIcon(
                        context,
                        context.resources.getString(R.string.pdf_document_sign_request_icon)
                    )
                )
            )

            if (documentSignCancel) iconList.add(
                Icon(
                    context.resources.getString(R.string.str_sign_cancel),
                    ResUtils.getToolbarIcon(
                        context,
                        context.resources.getString(R.string.pdf_document_sign_cancel_icon)
                    )
                )
            )
        }

        return iconList
    }

    fun onNavModeClicked(content: String){
        dismissDialog()
        (mContext as CustomPDFActivity).onNavMoreClicked(content)
    }

    fun mDialogRunning(): Boolean {
        return isDialogRunning
    }

    fun dismissDialog() {
        dialog?.dismiss()
    }

    companion object {
        private var instance: DialogMorePDFNavBar? = null
        private val Instance: DialogMorePDFNavBar
            get() {
                if (instance == null) {
                    instance = DialogMorePDFNavBar()
                }
                return instance!!
            }

        fun openDialog(
            context: Context, mDocumentDetail: DocumentDetail,

            ) {
            Instance.openDialog(context, mDocumentDetail)
        }

        fun isDialogRunning(): Boolean {
            return Instance.mDialogRunning()
        }

        fun dismissDialog() {
            Instance.dismissDialog()
        }
    }
}