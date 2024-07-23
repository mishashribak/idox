package com.knowledgeways.idocs.ui.main.adapter.box

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.base.BaseRecyclerViewAdapter
import com.knowledgeways.idocs.databinding.RecyclerItemDrawerBodyBinding
import com.knowledgeways.idocs.network.model.Box
import com.knowledgeways.idocs.network.model.DrawerItemColor

class BoxAdapter(
    private var drawerItemColorList: List<DrawerItemColor>,
    private val onItemSelected :(box: Box) -> Unit,
) : BaseRecyclerViewAdapter<Box, RecyclerItemDrawerBodyBinding>(
) {

    lateinit var jsonObject: JsonObject

    override val layoutId: Int
        get() = R.layout.recycler_item_drawer_body

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return BoxViewHolder(createBindView(viewGroup), drawerItemColorList)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {

        val box = items[position]

        val holder = viewHolder as BoxViewHolder
        val context = holder.binding.root.context
        holder.itemSize = itemCount
        holder.update(context, box,drawerItemColorList ,  position, selected , if (::jsonObject.isInitialized) jsonObject else null)
        holder.itemView.setOnClickListener {
            setSelection(position)
            onItemSelected(box)
        }
    }

    fun getSelectedId(): Int{
        return selected
    }
    fun setColorList(mColorList: List<DrawerItemColor>) {
        drawerItemColorList = mColorList
        notifyDataSetChanged()
    }

    fun setItems(itemList: List<Box>, documentTotal: JsonObject) {
        jsonObject = documentTotal
        super.setItems(itemList)
    }

}