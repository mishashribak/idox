package com.knowledgeways.idocs.ui.main.adapter.box

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.gson.JsonObject
import com.knowledgeways.idocs.R
import com.knowledgeways.idocs.databinding.RecyclerItemDrawerBodyBinding
import com.knowledgeways.idocs.network.model.Box
import com.knowledgeways.idocs.network.model.DrawerItemColor
import com.knowledgeways.idocs.utils.CommonUtils
import com.knowledgeways.idocs.utils.GlideApp
import com.knowledgeways.idocs.utils.ResUtils

class BoxViewHolder(
    val binding: RecyclerItemDrawerBodyBinding,
    private var drawerItemColorList: List<DrawerItemColor>
) :
    RecyclerView.ViewHolder(binding.root) {

    var itemSize: Int = 0

    fun update(
        context: Context,
        box: Box,
        drawerItemColorList: List<DrawerItemColor>,
        position: Int,
        selected: Int,
        jsonObject: JsonObject? = null
    ) {
        binding.apply {

            GlideApp.with(context).load(ResUtils.getBoxIconNam(context, box.iconName ?: "", selected == position))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.ivBox)

            tvBox.apply {
                text = box.title ?: ""
                setTextColor(CommonUtils.getBoxTextColor(box, selected == position))
            }


            layoutName.apply {
                if (CommonUtils.isColorListFilled(drawerItemColorList)) {
                    setBackgroundColor(
                        getDeselectedBackgroundColor(position)
                    )
                }
            }

            ivBackground.apply {
                setColorFilter(
                    getSelectedBackgroundFilterColor(selected, context, position),
                    PorterDuff.Mode.SRC_IN
                )
                visibility = if (selected == position) VISIBLE else GONE
            }
            ivBadge.setColorFilter(Color.parseColor(getBadgeColorFilter(box, position, selected)))
            tvBadgeCount.apply {
                setTextColor(Color.parseColor(getBadgeTextColor(box, position, selected)))
                if (jsonObject != null){
                    if (jsonObject.has(box.boxId) && jsonObject[box.boxId] != null){
                        ivBadge.visibility = VISIBLE
                        visibility = VISIBLE
                        text = jsonObject[box.boxId].asInt.toString()
                    }else{
                        ivBadge.visibility = GONE
                        visibility = GONE
                    }
                }else{
                    ivBadge.visibility = GONE
                    visibility = GONE
                }
            }
        }
    }

    private fun getBadgeColorFilter(box: Box, position: Int, selected: Int): String{
        return if (position == selected) box.selectedBadgeColor ?: "" else box.badgeColor?: ""
    }

    private fun getBadgeTextColor(box: Box, position: Int, selected: Int) : String{
        return if (position == selected) box.selectedBadgeTxtColor ?: "" else box.badgeTxtColor?: ""
    }

    private fun getDeselectedBackgroundColor(position: Int): Int {
        return Color.parseColor(
            when (position) {
                0 -> drawerItemColorList.first().colorDeselected
                itemSize - 1 -> drawerItemColorList.last().colorDeselected
                else -> drawerItemColorList[1].colorDeselected
            }
        )
    }

    private fun getSelectedBackgroundFilterColor(selected: Int,context: Context, position: Int): Int {
        return if (selected == position && CommonUtils.isColorListFilled(drawerItemColorList)) {
            Color.parseColor(
                when (position) {
                    0 -> drawerItemColorList.first().colorSelected
                    itemSize - 1 -> drawerItemColorList.last().colorSelected
                    else -> drawerItemColorList[1].colorSelected
                }
            )
        }  else ContextCompat.getColor(context, R.color.color_transparent_1000)
    }
}