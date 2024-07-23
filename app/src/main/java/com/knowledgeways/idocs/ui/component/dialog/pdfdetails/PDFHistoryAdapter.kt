package com.knowledgeways.idocs.ui.component.dialog.pdfdetails

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.base.BaseRecyclerViewAdapter
import com.knowledgeways.idocs.databinding.RecyclerItemDocumentHistoryBinding
import com.knowledgeways.idocs.network.model.PDFHistory

class PDFHistoryAdapter(private val onItemClicked :(position: Int) -> Unit)  : BaseRecyclerViewAdapter<PDFHistory, RecyclerItemDocumentHistoryBinding>() {
    override val layoutId: Int
        get() = R.layout.recycler_item_document_history

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return PDFHistoryViewHolder(createBindView(viewGroup))
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        val holder =  viewHolder as PDFHistoryViewHolder
        val context = holder.binding.root.context
        holder.binding.root.setOnClickListener {
            onItemClicked(position)
        }

        holder.binding.apply {
            with(item){
                tvFrom.text= from
                tvTo.text = to
                tvAction.text = action
                tvStatus.text = status
                tvDate.text = date
                tvNote.text = shortDescription
                tvPriority.text = priority
            }
        }
    }

    inner class PDFHistoryViewHolder(val binding: RecyclerItemDocumentHistoryBinding) :
        RecyclerView.ViewHolder(binding.root)
}