package com.knowledgeways.idocs.ui.component.dialog.pdflinks

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.base.BaseRecyclerViewAdapter
import com.knowledgeways.idocs.databinding.RecyclerItemPdfLinkBinding
import com.knowledgeways.idocs.network.model.Link

class PDFLinkAdapter(private val onLinkClicked: (link: Link) -> Unit) :
    BaseRecyclerViewAdapter<Link, RecyclerItemPdfLinkBinding>() {

    override val layoutId: Int
        get() = R.layout.recycler_item_pdf_link

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return PDFLinkViewHolder(createBindView(viewGroup))
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        val holder = viewHolder as PDFLinkViewHolder
        val context = holder.binding.root.context


        holder.binding.apply {
            root.setOnClickListener {
                onLinkClicked(item)
            }
            tvLink.text = item.title
        }
    }

    inner class PDFLinkViewHolder(val binding: RecyclerItemPdfLinkBinding) :
        RecyclerView.ViewHolder(binding.root)
}