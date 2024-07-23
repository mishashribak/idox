package com.knowledgeways.idocs.ui.component.dialog.userNorgdetail

import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.base.BaseRecyclerViewAdapter
import com.knowledgeways.idocs.databinding.RecyclerItemDetailPageBinding
import com.knowledgeways.idocs.network.model.Action

class PriorityAdapter : BaseRecyclerViewAdapter<Action, RecyclerItemDetailPageBinding>() {

    override val layoutId: Int
        get() = R.layout.recycler_item_detail_page


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return PriorityViewHolder(createBindView(viewGroup))
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val priority = items[position]
        val holder =  viewHolder as PriorityViewHolder

        holder.binding.run {
            ivTick.visibility =  if (selected == position) VISIBLE else GONE

            tvTitle.apply {
                text = priority.label

                setOnClickListener {
                    setSelection(position)
                }
            }
        }
    }

    fun  getSelectedValue(): Action?{
        return if (selected > -1 && selected < items.size) {
            items[selected]
        }
        else {
            null
        }
    }

    inner class PriorityViewHolder(val binding: RecyclerItemDetailPageBinding) :
        RecyclerView.ViewHolder(binding.root)
}