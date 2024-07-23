package com.knowledgeways.idocs.ui.component.dialog.forward

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.swipe.SimpleSwipeListener
import com.daimajia.swipe.SwipeLayout
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.base.BaseRecyclerViewAdapter
import com.knowledgeways.idocs.databinding.RecyclerItemFavoriteBinding
import com.knowledgeways.idocs.network.model.ExternalUser
import com.knowledgeways.idocs.network.model.user.Organization

class DocUserAdapter(
    private val isFavorite: Boolean,
    private val onUserDetailClicked: (user: ExternalUser) -> Unit,
    private val onDeleteUserClicked: (org: ExternalUser) -> Unit
) : BaseRecyclerViewAdapter<ExternalUser, RecyclerItemFavoriteBinding>() {

    override val layoutId: Int
        get() = R.layout.recycler_item_favorite

    var filterKey: String = ""
    private val allUsers: ArrayList<ExternalUser> = ArrayList()

    private var openedLayout: SwipeLayout? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return DocORGViewHolder(createBindView(viewGroup))
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val organization = items[position]
        val holder = viewHolder as DocORGViewHolder

        holder.binding.run {
            tvName.apply {
                text = organization.fullName ?: ""

                setOnClickListener {
                    onUserDetailClicked(organization)
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
                onDeleteUserClicked(organization)
            }
        }
    }

    private fun applyFilter() {
        items.clear()
        for (user in allUsers) {
            if ((user.fullName ?: "").lowercase().contains(filterKey)) {
                items.add(user)
            }
        }
        notifyDataSetChanged()
    }

    fun removeElement(user: ExternalUser){
        var position = -1
        for (i in items.indices){
            if (items[i].userId == user.userId) {
                position = i
            }
        }

        if (position != -1 && position < items.size){
            items.removeAt(position)
            notifyItemRemoved(position)
        }

    }


    fun setFilterString(mFilterString: String) {
        filterKey = mFilterString.lowercase()
        applyFilter()
    }

    inner class DocORGViewHolder(val binding: RecyclerItemFavoriteBinding) :
        RecyclerView.ViewHolder(binding.root)
}