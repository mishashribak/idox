package com.knowledgeways.idocs.ui.component.dialog.forward

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.swipe.SimpleSwipeListener
import com.daimajia.swipe.SwipeLayout
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.base.BaseRecyclerViewAdapter
import com.knowledgeways.idocs.databinding.RecyclerItemFavoriteBinding
import com.knowledgeways.idocs.network.model.user.Organization

class DocORGAdapter(
    private val isFavorite: Boolean,
    private val onDocOrgClicked: (organization: Organization) -> Unit,
    private val onDeleteOrgClicked: (org: Organization) -> Unit
) : BaseRecyclerViewAdapter<Organization, RecyclerItemFavoriteBinding>() {

    override val layoutId: Int
        get() = R.layout.recycler_item_favorite

    var filterKey: String = ""
    private val allOrganizations: ArrayList<Organization> = ArrayList()
    private var openedLayout: SwipeLayout? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return DocORGViewHolder(createBindView(viewGroup))
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val organization = items[position]
        val holder = viewHolder as DocORGViewHolder

        holder.binding.run {
            tvName.apply {
                text = organization.name ?: ""

                setOnClickListener {
                    onDocOrgClicked(organization)
                }
            }

            // Set Layout Swipe Mode
            layoutSwipe.showMode = SwipeLayout.ShowMode.PullOut
            layoutSwipe.isLeftSwipeEnabled = false
            layoutSwipe.isRightSwipeEnabled = isFavorite
            layoutSwipe.addSwipeListener(object : SimpleSwipeListener() {
                override fun onOpen(layout: SwipeLayout?) {
                    if (openedLayout != null && openedLayout != layout) openedLayout?.close()
                    openedLayout = layout
                }
            })

            layoutDelete.setOnClickListener {
                onDeleteOrgClicked(organization)
            }
        }
    }

    private fun applyFilter() {
        items.clear()
        for (organization in allOrganizations) {
            if ((organization.name ?: "").lowercase().contains(filterKey)) {
                items.add(organization)
            }
        }
        notifyDataSetChanged()
    }

    fun setFilterString(mFilterString: String) {
        filterKey = mFilterString.lowercase()
        applyFilter()
    }

    fun removeElement(organization: Organization){
        var position = -1
        for (i in items.indices){
            if (items[i].id == organization.id) {
                position = i
            }
        }

        if (position != -1 && position < items.size){
            items.removeAt(position)
            notifyItemRemoved(position)
        }

    }

    inner class DocORGViewHolder(val binding: RecyclerItemFavoriteBinding) :
        RecyclerView.ViewHolder(binding.root)
}