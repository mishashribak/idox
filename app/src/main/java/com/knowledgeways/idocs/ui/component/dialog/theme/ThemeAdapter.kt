package com.knowledgeways.idocs.ui.component.dialog.theme

import android.view.View.*
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.base.BaseRecyclerViewAdapter
import com.knowledgeways.idocs.databinding.RecyclerItemThemeManagerBinding
import com.knowledgeways.idocs.network.model.theme.Theme
import com.knowledgeways.idocs.utils.CommonUtils
import com.knowledgeways.idocs.utils.FileUtils

class ThemeAdapter(
    private val listener: ThemeManager.OnSelectedListener)  : BaseRecyclerViewAdapter<Theme, RecyclerItemThemeManagerBinding>() {

    override val layoutId: Int
        get() = R.layout.recycler_item_theme_manager

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ThemeViewHolder(createBindView(viewGroup))
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val theme = items[position]
        val holder =  viewHolder as ThemeViewHolder
        val context = holder.binding.root.context

        val isDefaultTheme = CommonUtils.getDefaultThemeId() == theme.themeId
        val isThemeDownloaded = FileUtils.isThemeExisting(context, theme)

        holder.binding.apply {
            tvThemeName.text = theme.themeName ?: ""
            tvThemeDescription.text = theme.themeDescription ?: ""

            ivDefault.visibility = if (isDefaultTheme) VISIBLE else INVISIBLE
            ivDownload.visibility = if (isThemeDownloaded) INVISIBLE else VISIBLE
            ivDelete.visibility = if (isThemeDownloaded && !isDefaultTheme) VISIBLE else INVISIBLE
            ivEdit.visibility = if (isDefaultTheme || !isThemeDownloaded) INVISIBLE else VISIBLE

            ivDownload.setOnClickListener {
                listener.onDownloadClicked(theme)
            }

            ivEdit.setOnClickListener {
                listener.onEditSelected(theme)
            }

            ivDelete.setOnClickListener {
                listener.onDeleteClicked(theme)
            }
        }
    }

    inner class ThemeViewHolder(val binding: RecyclerItemThemeManagerBinding) :
        RecyclerView.ViewHolder(binding.root)
}