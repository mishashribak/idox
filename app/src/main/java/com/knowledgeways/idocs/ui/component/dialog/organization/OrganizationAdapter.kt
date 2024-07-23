package com.knowledgeways.idocs.ui.component.dialog.organization

import android.graphics.Color
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.base.BaseRecyclerViewAdapter
import com.knowledgeways.idocs.databinding.RecyclerItemOrganizationBinding
import com.knowledgeways.idocs.network.model.theme.ThemeTextStyle
import com.knowledgeways.idocs.network.model.user.Organization
import com.knowledgeways.idocs.utils.ResUtils

class OrganizationAdapter : BaseRecyclerViewAdapter<Organization, RecyclerItemOrganizationBinding>() {

    override val layoutId: Int
        get() = R.layout.recycler_item_organization

    var listener: OnOrganizationClickedListener?= null

    var filterKey: String = ""
    private val allOrganizations: ArrayList<Organization> = ArrayList()

    var themeTextStyle : ThemeTextStyle ?= null

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return OrganizationViewHolder(createBindView(viewGroup))
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val organization = items[position]
        val holder =  viewHolder as OrganizationViewHolder
        holder.binding.run {
            tvName.apply {
                text = organization.name ?: ""
                if (themeTextStyle != null){
                    themeTextStyle.apply {
                        textSize = ResUtils.getTextSize(this!!.fontSize!!)
                        setTextColor(Color.parseColor(this.fgColor))
                    }
                }
            }

            layoutName.setOnClickListener {
                listener?.onSelected((organization))
            }
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

    override fun setItems(itemList: List<Organization>) {
        allOrganizations.clear()
        allOrganizations.addAll(itemList)
        applyFilter()
    }

    fun setFilterString(mFilterString: String){
        filterKey = mFilterString.lowercase()
        applyFilter()
    }

    inner class OrganizationViewHolder(val binding: RecyclerItemOrganizationBinding) :
            RecyclerView.ViewHolder(binding.root)

    interface OnOrganizationClickedListener{
        fun onSelected(organization: Organization)
    }

}