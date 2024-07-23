package com.knowledgeways.idocs.network.model

data class UserFavoriteRequest(val userId: String,
                               val fullName: String,
                               val organizationId: String,
                               val organizationName: String)
