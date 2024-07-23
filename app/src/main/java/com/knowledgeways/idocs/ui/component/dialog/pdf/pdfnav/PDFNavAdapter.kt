package com.knowledgeways.idocs.ui.component.dialog.pdf.pdfnav

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.base.BaseRecyclerViewAdapter
import com.knowledgeways.idocs.databinding.RecyclerItemPdfNavMoreBinding
import com.knowledgeways.idocs.network.model.Icon
import com.knowledgeways.idocs.utils.GlideApp

class PDFNavAdapter(private val onNavModeCLicked:(content: String) -> Unit) : BaseRecyclerViewAdapter<Icon, RecyclerItemPdfNavMoreBinding>(
) {
    override val layoutId: Int
        get() = R.layout.recycler_item_pdf_nav_more

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return PDFNavViewHolder(createBindView(viewGroup))
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val icon  = items[position]
        val holder = viewHolder as PDFNavViewHolder
        val context = holder.binding.root.context

        holder.binding.apply {
            tvTitle.text = icon.title
            GlideApp.with(context).load(icon.res)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(ivIcon)

            ivIcon.setOnClickListener {
                onNavModeCLicked(icon.title)
            }

            tvTitle.setOnClickListener {
                onNavModeCLicked(icon.title)
            }
        }
    }

    inner class PDFNavViewHolder(val binding: RecyclerItemPdfNavMoreBinding) :
        RecyclerView.ViewHolder(binding.root)
}