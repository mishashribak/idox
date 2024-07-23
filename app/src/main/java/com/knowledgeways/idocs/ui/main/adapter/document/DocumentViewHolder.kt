package com.knowledgeways.idocs.ui.main.adapter.document

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.knowledgeways.idocs.databinding.RecyclerItemDocumentBinding
import com.knowledgeways.idocs.db.PreferenceManager
import com.knowledgeways.idocs.network.model.Document
import com.knowledgeways.idocs.utils.*

class DocumentViewHolder(
    val binding: RecyclerItemDocumentBinding
) :
    RecyclerView.ViewHolder(binding.root) {

    fun update(
        context: Context,
        document: Document,
    ) {
        binding.apply {
            var isOpened = false
            val documentIdList = PreferenceManager.documentList
            for (documentId in documentIdList){
                if (documentId.transferId == document.transferId && documentId.docID == document.documentId){
                    isOpened = true
                }
            }

            val defaultTheme = CommonUtils.getDefaultTheme()

            ivBackground.setColorFilter(Color.parseColor(defaultTheme?.documentList?.item?.bgColor))

            tvPriority.apply {
                text = document.priorityLabel ?: ""
                setTextColor(
                    Color.parseColor(
                        ConverterUtils.getPriorityStyle(
                            document.priorityValue ?: "0"
                        )?.fgColor ?: ""
                    )
                )
                textSize =
                    ResUtils.getTextSize(
                        ConverterUtils.getPriorityStyle(document.priorityValue ?: "0")?.fontSize!!
                    )

            }
            tvConfidential.apply {
                text = document.confidentialLabel ?: ""
                setTextColor(
                    Color.parseColor(
                        ConverterUtils.getConfidentialStyle(
                            document.priorityValue ?: "0"
                        )?.fgColor ?: ""
                    )
                )

                textSize =
                    ResUtils.getTextSize(
                        ConverterUtils.getConfidentialStyle(
                            document.confidentialValue ?: "0"
                        )?.fontSize!!
                    )
            }

            tvFrom.apply {
                if (document.from == null || document.from.isEmpty()) {
                    visibility = GONE
                } else {
                    visibility = VISIBLE
                    text = document.from
                    setTextColor(
                        Color.parseColor(
                            defaultTheme?.documentList?.item?.from?.fgColor ?: ""
                        )
                    )
                    textSize =
                        ResUtils.getTextSize(defaultTheme?.documentList?.item?.from?.fontSize!!)
                }

            }

            tvSubject.apply {
                if (document.subject == null || document.subject.isEmpty()) {
                    visibility = GONE
                } else {
                    visibility = VISIBLE
                    text = document.subject ?: ""
                    setTextColor(
                        Color.parseColor(
                            defaultTheme?.documentList?.item?.subject?.fgColor ?: ""
                        )
                    )
                    textSize =
                        ResUtils.getTextSize(defaultTheme?.documentList?.item?.subject?.fontSize!!)
                }
            }

            tvPlaceholderDate.apply {
                if (document.placeholderDate == null || document.placeholderDate.isEmpty()) {
                    visibility = GONE
                } else {
                    visibility = VISIBLE
                    text = document.placeholderDate ?: ""
                    setTextColor(
                        Color.parseColor(
                            defaultTheme?.documentList?.item?.date?.fgColor ?: ""
                        )
                    )
                    textSize =
                        ResUtils.getTextSize(defaultTheme?.documentList?.item?.date?.fontSize!!)
                }
            }

            tvStatus.apply {
                if (document.status == null || document.status.isEmpty()) {
                    visibility = GONE
                } else {
                    visibility = VISIBLE
                    text = document.status
                    setTextColor(
                        Color.parseColor(
                            defaultTheme?.documentList?.item?.status?.fgColor ?: ""
                        )
                    )
                    textSize =
                        ResUtils.getTextSize(defaultTheme?.documentList?.item?.status?.fontSize!!)
                }

            }

            tvCategory.apply {
                if (document.category == null || document.category.isEmpty()) {
                    visibility = GONE
                } else {
                    visibility = VISIBLE
                    text = document.category
                    setTextColor(
                        Color.parseColor(
                            defaultTheme?.documentList?.item?.category?.fgColor ?: ""
                        )
                    )
                    textSize =
                        ResUtils.getTextSize(defaultTheme?.documentList?.item?.category?.fontSize!!)
                }
            }

            tvDocumentNumber.apply {
                text = document.documentNumber ?: ""
                setTextColor(
                    Color.parseColor(
                        defaultTheme?.documentList?.item?.referenceNumber?.fgColor ?: ""
                    )
                )
                textSize =
                    ResUtils.getTextSize(defaultTheme?.documentList?.item?.referenceNumber?.fontSize!!)

                visibility =
                    if (document.documentNumber == null || document.documentNumber.isEmpty()) GONE else VISIBLE
            }

            layoutPriority.visibility =
                if (document.priorityValue != null && document.priorityValue != "0") {
                    GlideApp.with(context)
                        .load(ResUtils.getPriorityIcon(context, document.priorityValue))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(ivPriority)

                    ivBackgroundPriority.setColorFilter(
                        Color.parseColor(
                            ConverterUtils.getPriorityStyle(document.priorityValue)?.bgColor
                        ), PorterDuff.Mode.SRC_IN
                    )
                    VISIBLE
                } else {
                    GONE
                }

            layoutConfidential.visibility =
                if (document.confidentialValue != null && document.confidentialValue != "0") {
                    GlideApp.with(context)
                        .load(ResUtils.getConfidentialIcon(context, document.confidentialValue))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(ivConfidential)

                    ivBackgroundConfidential.setColorFilter(
                        Color.parseColor(
                            ConverterUtils.getPriorityStyle(document.confidentialValue)?.bgColor
                        ), PorterDuff.Mode.SRC_IN
                    )
                    VISIBLE
                } else {
                    GONE
                }

            ivUnread.apply {
                setColorFilter(Color.parseColor(defaultTheme?.documentList?.item?.unreadDotColor))
                visibility =
                    if (layoutConfidential.visibility == VISIBLE || layoutPriority.visibility == VISIBLE) {
                        if (FileUtils.isPDFExisting(
                                context,
                                document.documentId!!,
                                document.transferId!!
                            ) && isOpened
                        ) GONE
                        else VISIBLE
                    } else GONE
            }

            layoutTop.visibility =
                if (layoutConfidential.visibility == VISIBLE || layoutPriority.visibility == VISIBLE) VISIBLE else GONE

            ivUnreadFrom.apply {
                setColorFilter(Color.parseColor(defaultTheme?.documentList?.item?.unreadDotColor))
                visibility =
                    if (layoutConfidential.visibility == VISIBLE || layoutPriority.visibility == VISIBLE) GONE else {
                        if (FileUtils.isPDFExisting(
                                context,
                                document.documentId!!,
                                document.transferId!!
                            ) && isOpened
                        ) GONE
                        else VISIBLE
                    }
            }
        }
    }
}