package com.knowledgeways.idocs.ui.component.dialog.pdfdetails

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.databinding.RecyclerItemDocumentHistoryDetailBinding

class PDFDetailAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var hashMap: HashMap<String,String> = HashMap()
    private var keySet : MutableList<String> = ArrayList()

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = DataBindingUtil.inflate<RecyclerItemDocumentHistoryDetailBinding>(
            LayoutInflater.from(viewGroup.context),
            R.layout.recycler_item_document_history_detail,
            viewGroup, false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as ViewHolder
        holder.binding.apply {
            tvTitle.text = keySet[position]
            tvContent.text = hashMap[keySet[position]]
        }
    }

    override fun getItemCount(): Int {
        return keySet.size
    }

    fun setValue(mHashMap: HashMap<String, String>, mKeySet: List<String>){
        this.hashMap = mHashMap
        keySet.clear()
        keySet.addAll(mKeySet)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: RecyclerItemDocumentHistoryDetailBinding):
        RecyclerView.ViewHolder(binding.root)

}