package com.knowledgeways.idocs.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

abstract class BaseRecyclerViewAdapter<M, T : ViewDataBinding> :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    protected val items: ArrayList<M> = ArrayList()

    @get:LayoutRes
    abstract val layoutId: Int

    protected var selected: Int = -1

    protected fun createBindView(viewGroup: ViewGroup): T {
        return DataBindingUtil.inflate<T>(
            LayoutInflater.from(viewGroup.context),
            layoutId,
            viewGroup,
            false
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    open fun setItems(itemList: List<M>) {
        items.clear()
        items.addAll(itemList)
        selected = -1
        notifyDataSetChanged()
    }

    open fun setSelection(position: Int) {
        selected = position
        notifyDataSetChanged()
    }
}