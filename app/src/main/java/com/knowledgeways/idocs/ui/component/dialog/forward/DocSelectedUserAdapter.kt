package com.knowledgeways.idocs.ui.component.dialog.forward

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.base.BaseRecyclerViewAdapter
import com.knowledgeways.idocs.databinding.RecyclerItemSelectedBinding
import com.knowledgeways.idocs.network.model.ExternalUser

class DocSelectedUserAdapter(private  val onDeleteOrgClicked :(user: ExternalUser) -> Unit) : BaseRecyclerViewAdapter<ExternalUser, RecyclerItemSelectedBinding>() {

    override val layoutId: Int
        get() = R.layout.recycler_item_selected

    var filterKey: String = ""
    private val allOrganizations: ArrayList<ExternalUser> = ArrayList()

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return DocORGViewHolder(createBindView(viewGroup))
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val organization = items[position]
        val holder =  viewHolder as DocORGViewHolder

        holder.binding.run {
            tvName.apply {
                text = organization.fullName ?: ""
            }

            ivClose.setOnClickListener {
                onDeleteOrgClicked(organization)
            }
        }
    }

    private fun applyFilter(){
        items.clear()
        for (organization in allOrganizations) {
            if ((organization.fullName ?: "").lowercase().contains(filterKey)) {
                items.add(organization)
            }
        }
        notifyDataSetChanged()
    }

    fun setFilterString(mFilterString: String){
        filterKey = mFilterString.lowercase()
        applyFilter()
    }

    inner class DocORGViewHolder(val binding: RecyclerItemSelectedBinding) :
        RecyclerView.ViewHolder(binding.root)

}