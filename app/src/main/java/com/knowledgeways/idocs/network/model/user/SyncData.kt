package com.knowledgeways.idocs.network.model.user

data class SyncData(
    val needSyncActions: Boolean?,
    val needSyncConfidential: Boolean?,
    val needSyncDocumentCategory: Boolean?,
    val needSyncExternalOrganizations: Boolean?,
    val needSyncFavoriteOrganizations: Boolean?,
    val needSyncFavoriteTransferableExternalOrganizations: Boolean?,
    val needSyncFavoriteUsers: Boolean?,
    val needSyncInternalOrganizations: Boolean?,
    val needSyncOrganizations: Boolean?,
    val needSyncOrganizationsUsers: Boolean?,
    val needSyncPriority: Boolean?,
    val needSyncTransferableExternalOrganizations: Boolean?
)