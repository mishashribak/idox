package com.knowledgeways.idocs.network.model

data class Box(
    val badgeColor: String?,
    val badgeTxtColor: String?,
    val boxId: String?,
    val children: Any?,
    val group: String?,
    val hasChildBoxes: Boolean?,
    val iconName: String?,
    val isDownloadable: Boolean?,
    val order: Int?,
    val parentBoxId: Any?,
    val selectedBadgeColor: String?,
    val selectedBadgeTxtColor: String?,
    val selectedTitleColor: String?,
    val title: String?,
    val titleColor: String?,
    val webURL: String?
)