package com.knowledgeways.idocs.ui.component.dialog.pdfattachments

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.base.BaseRecyclerViewAdapter
import com.knowledgeways.idocs.databinding.RecyclerItemPdfAttachmentBinding
import com.knowledgeways.idocs.network.model.Link

class PdfAttachmentAdapter(private val onLinkClicked: (link: Link) -> Unit) :
    BaseRecyclerViewAdapter<Link, RecyclerItemPdfAttachmentBinding>() {

    override val layoutId: Int
        get() = R.layout.recycler_item_pdf_attachment

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return PDFAttachmentViewHolder(createBindView(viewGroup))
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        val holder = viewHolder as PDFAttachmentViewHolder
        val context = holder.binding.root.context


        holder.binding.apply {
            root.setOnClickListener {
                onLinkClicked(item)
            }
            if (item.category == "primary"){
                tvAttachmentMain.text = item.title
                ivAttachmentMain.setImageDrawable(getIconFromFileExtension(context, item.fileExtension ?: ""))
            }else{
                tvAttachmentOther.text = item.title
                ivAttachmentOther.setImageDrawable(getIconFromFileExtension(context, item.fileExtension ?: ""))
            }

        }
    }

    fun getIconFromFileExtension(context: Context, extension: String) : Drawable?{
        return ContextCompat.getDrawable(context,
        when(extension) {
            "pdf" -> R.drawable.ic_pdf
            "xls" -> R.drawable.ic_xls
            "doc" -> R.drawable.ic_doc
            "ppt" -> R.drawable.ic_ppt
            "zip" -> R.drawable.ic_zip
            else -> R.drawable.ic_pdf
        })
    }

    inner class PDFAttachmentViewHolder(val binding: RecyclerItemPdfAttachmentBinding) :
        RecyclerView.ViewHolder(binding.root)
}