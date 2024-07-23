package com.knowledgeways.idocs.network.model.theme

import com.squareup.moshi.Json

open class ControlPanel {

    @Json(name = "logout")
    var logout: Logout? = null

    @Json(name = "others")
    var others: Others ?= null
}

