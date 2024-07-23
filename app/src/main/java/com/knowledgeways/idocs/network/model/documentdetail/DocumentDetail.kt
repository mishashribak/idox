package com.knowledgeways.idocs.network.model.documentdetail

import java.io.Serializable

data class DocumentDetail(
    val acl: Acl,
    val attachmentsTotal: Int,
    val bookmarks: List<Any>,
    val boxId: String,
    val documentId: String,
    val linksTotal: Int,
    val sendForm: SendForm,
    val transfer: Transfer,
    val watermark: Watermark
): Serializable
