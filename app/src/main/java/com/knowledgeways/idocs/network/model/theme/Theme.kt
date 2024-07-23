package com.knowledgeways.idocs.network.model.theme

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

open class Theme {
    @SerializedName("controlPanel")
    @Expose
    var controlPanel: ControlPanel ?= null
    @SerializedName( "dateInfo")
    @Expose
    var dateInfo: ThemeTextStyle ?= null
    @SerializedName("default")
    @Expose
    var default: Boolean ?= null
    @Expose
    @SerializedName("documentList")
    var documentList: DocumentList ?= null
    @SerializedName("isDefault")
    @Expose
    var mIsDefault: Boolean? = false
    @SerializedName("loginForm")
    @Expose
    var loginForm: LoginForm ?= null
    @SerializedName("popupForm")
    @Expose
    var popupForm: PopupForm ?= null
    @SerializedName("themeDescription")
    @Expose
    var themeDescription: String ? = null

    @SerializedName("themeId")
    @Expose
    var themeId: String ?= null
    @SerializedName("themeName")
    @Expose
    var themeName: String ?= null
    @SerializedName("userInfo")
    @Expose
    var userInfo: UserInfo ?= null
    @SerializedName("viewer")
    @Expose
    var viewer: Viewer ?= null
}

