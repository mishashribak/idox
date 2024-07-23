package com.knowledgeways.idocs.ui.component.dialog.forward

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.base.BaseRecyclerViewAdapter
import com.knowledgeways.idocs.databinding.RecyclerItemSelectedBinding
import com.knowledgeways.idocs.network.model.user.Organization

class DocSelectedOrgAdapter (private  val onDeleteOrgClicked :(organization: Organization) -> Unit) : BaseRecyclerViewAdapter<Organization, RecyclerItemSelectedBinding>() {

    override val layoutId: Int
        get() = R.layout.recycler_item_selected

    var filterKey: String = ""
    private val allOrganizations: ArrayList<Organization> = ArrayList()

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return DocORGViewHolder(createBindView(viewGroup))
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val organization = items[position]
        val holder =  viewHolder as DocORGViewHolder

        holder.binding.run {
            tvName.apply {
                text = organization.name ?: ""
            }

            ivClose.setOnClickListener {
                onDeleteOrgClicked(organization)
            }

            var descriptionString = ""
            with(organization){
                descriptionString = if (forwardType == 1) "CC" else "TO"
                descriptionString += " "
                descriptionString += priority?.label ?: ""

                for (actions in actionList ?: ArrayList()){
                    descriptionString += actions.label + " "
                }
            }

            tvDescription.text = descriptionString
        }
    }

    private fun applyFilter(){
        items.clear()
        for (organization in allOrganizations) {
            if ((organization.name ?: "").lowercase().contains(filterKey)) {
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