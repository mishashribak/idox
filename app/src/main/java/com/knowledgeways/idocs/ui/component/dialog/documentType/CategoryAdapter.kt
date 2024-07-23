package com.knowledgeways.idocs.ui.component.dialog.documentType

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.base.BaseRecyclerViewAdapter
import com.knowledgeways.idocs.databinding.RecyclerItemOrganizationBinding
import com.knowledgeways.idocs.network.model.Category

class CategoryAdapter : BaseRecyclerViewAdapter<Category, RecyclerItemOrganizationBinding>() {

    override val layoutId: Int
        get() = R.layout.recycler_item_organization

    var listener: OnCategorySelectedListener?= null

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CategoryViewHolder(createBindView(viewGroup))
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val category = items[position]
        val holder =  viewHolder as CategoryViewHolder
        holder.binding.run {
            tvName.text = category.label ?: ""

            layoutName.setOnClickListener {
                listener?.onSelected(category)
            }
        }
    }

    inner class CategoryViewHolder(val binding: RecyclerItemOrganizationBinding) :
        RecyclerView.ViewHolder(binding.root)

    interface OnCategorySelectedListener{
        fun onSelected(category: Category)
    }
}