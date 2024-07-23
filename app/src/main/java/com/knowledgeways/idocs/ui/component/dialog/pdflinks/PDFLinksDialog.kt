package com.knowledgeways.idocs.ui.component.dialog.pdflinks

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.Window
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.databinding.LayoutPdfLinksBinding
import com.knowledgeways.idocs.network.model.Link
import com.knowledgeways.idocs.ui.component.dialog.pdfdetails.PDFDetailDialog
import com.knowledgeways.idocs.utils.CommonUtils
import com.knowledgeways.idocs.utils.PopupUtils
import com.knowledgeways.idocs.utils.ResUtils

class PDFLinksDialog {

    var isDialogRunning = false
    var dialog: Dialog? = null
    lateinit var mLinkClicked :(link: Link) -> Unit

    lateinit var mBinding: LayoutPdfLinksBinding

    fun openDialog(
        context: Context,
        toolbarColor: String,
         onLinkClicked: (link: Link) -> Unit
    ){
        dialog = Dialog(context)

        mLinkClicked = onLinkClicked

        val binding = DataBindingUtil.inflate<LayoutPdfLinksBinding>(
            LayoutInflater.from(context),
            R.layout.layout_pdf_links,
            null,
            false
        )

        mBinding = binding
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setContentView(binding.root)

        binding.apply {
            if (toolbarColor.isNotEmpty()) {
                ivBackground.setColorFilter(Color.parseColor(toolbarColor), PorterDuff.Mode.SRC_IN)
            }
            tvClose.setTextAppearance(ResUtils.getTextViewStyle(CommonUtils.getDefaultTheme()?.popupForm?.button?.bold!!))
            tvClose.setTextColor(Color.parseColor(CommonUtils.getDefaultTheme()?.popupForm?.button?.fgColor))
            tvTitle.setTextAppearance(ResUtils.getTextViewStyle(CommonUtils.getDefaultTheme()?.popupForm?.button?.bold!!))
            tvTitle.setTextColor(Color.parseColor(CommonUtils.getDefaultTheme()?.popupForm?.button?.fgColor))
            tvTitle.textSize =
                ResUtils.getTextSize(CommonUtils.getDefaultTheme()?.popupForm?.button!!)
            tvClose.textSize =
                ResUtils.getTextSize(CommonUtils.getDefaultTheme()?.popupForm?.button!!)
            triangle?.setColorFilter(Color.parseColor(toolbarColor),
                PorterDuff.Mode.SRC_IN)

            recyclerView.layoutManager = LinearLayoutManager(context)

            tvClose.setOnClickListener {
                dialog?.dismiss()
            }
        }

        dialog?.setOnDismissListener {
            isDialogRunning = false
        }

        isDialogRunning = true
        PopupUtils.setDefaultDialogProperty(dialog!!)
        dialog?.show()
    }

    fun setLinkList(linkList: List<Link>){
        mBinding.apply {
            if (linkList.isEmpty()){
                tvEmptyLinks.visibility = VISIBLE
                recyclerView.visibility = GONE
                layoutProgress.visibility = GONE
            }else{
                tvEmptyLinks.visibility = GONE
                recyclerView.visibility = VISIBLE
                layoutProgress.visibility = GONE

                val mAdapter = PDFLinkAdapter(){ link->
                    mLinkClicked(link)
                }
                recyclerView.apply {
                    adapter = mAdapter
                    mAdapter.setItems(linkList)
                }
            }
        }
    }

    fun dismissDialog(){
        dialog?.dismiss()
        isDialogRunning = false
    }

    companion object {
        private var instance: PDFLinksDialog? = null
        private val Instance: PDFLinksDialog
            get() {
                if (instance == null) {
                    instance = PDFLinksDialog()
                }
                return instance!!
            }

        fun openDialog(
            context: Context,
            toolbarColor: String,
            onLinkClicked :(link: Link) -> Unit
        ){
            Instance.openDialog(context, toolbarColor){ link->
                onLinkClicked(link)
            }
        }

        fun setLinkList(linkList: List<Link>){
            Instance.setLinkList(linkList)
        }

        fun isDialogRunning(): Boolean{
            return Instance.isDialogRunning
        }

        fun dismissDialog(){
            Instance.dismissDialog()
        }
    }
}