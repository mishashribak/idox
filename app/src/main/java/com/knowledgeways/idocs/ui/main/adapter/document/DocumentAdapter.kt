package com.knowledgeways.idocs.ui.main.adapter.document

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.base.BaseRecyclerViewAdapter
import com.knowledgeways.idocs.databinding.RecyclerItemDocumentBinding
import com.knowledgeways.idocs.network.model.Document
import com.knowledgeways.idocs.utils.ConverterUtils

class DocumentAdapter(
    private val onDocumentSelected: (document: Document, position: Int) -> Unit
) : BaseRecyclerViewAdapter<Document, RecyclerItemDocumentBinding>(
) {
    override val layoutId: Int
        get() = R.layout.recycler_item_document

    private val allDocuments: ArrayList<Document> = ArrayList()
    private var filterString = ""

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return DocumentViewHolder(createBindView(viewGroup))
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val document = items[position]

        val holder = viewHolder as DocumentViewHolder
        val context = holder.binding.root.context
        holder.update(context, document)

        holder.binding.root.setOnClickListener {
            onDocumentSelected(document, position)
        }
    }

    override fun setItems(itemList: List<Document>) {
        allDocuments.clear()
        allDocuments.addAll(itemList)
        applyFilter()
    }

    private fun applyFilter() {
        items.clear()
        for (document in allDocuments) {
            if ((document.subject ?: "").lowercase().contains(filterString) || (document.from
                    ?: "").lowercase().contains(filterString) || filterString.isEmpty()
            ) {
                items.add(document)
            }
        }
        notifyDataSetChanged()
    }

    fun setFilterString(mFilterString: String) {
        filterString = mFilterString.lowercase()
        applyFilter()
    }
}