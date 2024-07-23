package com.knowledgeways.idocs.ui.component.dialog.userNorgdetail

import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.base.BaseRecyclerViewAdapter
import com.knowledgeways.idocs.databinding.RecyclerItemDetailPageBinding
import com.knowledgeways.idocs.network.model.Action

class ActionsAdapter: BaseRecyclerViewAdapter<Action, RecyclerItemDetailPageBinding>() {

    override val layoutId: Int
        get() = R.layout.recycler_item_detail_page


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return PriorityViewHolder(createBindView(viewGroup))
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val priority = items[position]
        val holder =  viewHolder as PriorityViewHolder

        holder.binding.run {

            ivTick.visibility =  if (priority.selected == true) VISIBLE else GONE

            tvTitle.apply {
                text = priority.label

                setOnClickListener {
                    priority.selected = priority.selected != true
                    ivTick.visibility =  if (priority.selected == true) VISIBLE else GONE
                    notifyDataSetChanged()
                }
            }
        }
    }

    fun  getSelectedList(): ArrayList<Action>{
        val selectedList = ArrayList<Action>()
        for (item in items){
            if (item.selected == true) selectedList.add(item)
        }

        return selectedList
    }

    inner class PriorityViewHolder(val binding: RecyclerItemDetailPageBinding) :
        RecyclerView.ViewHolder(binding.root)
}