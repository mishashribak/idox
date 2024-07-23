package com.knowledgeways.idocs.utils

import android.content.Context
import android.util.TypedValue
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.knowledgeways.idocs.db.PreferenceManager
import com.knowledgeways.idocs.network.model.*
import com.knowledgeways.idocs.network.model.theme.Theme
import com.knowledgeways.idocs.network.model.theme.ThemeTextStyle
import com.knowledgeways.idocs.network.model.user.*
import retrofit2.HttpException
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.reflect.Type


object ConverterUtils {
    fun convertThemeListToString(list: List<Theme>) : String {
        val mChangedList = ArrayList<Theme>()
        for (theme in list){
            theme.default = theme.mIsDefault
            mChangedList.add(theme)
        }
        val gson = Gson()
        return gson.toJson(mChangedList)
    }

    private fun getThemeListFromString(themeListString: String): List<Theme>? {
        val serializedObject: String = themeListString
        val gson = Gson()
        val type: Type = object : TypeToken<List<Theme>>() {}.type
        return gson.fromJson(serializedObject, type)
    }

    fun getThemeList(): List<Theme>? {
        return getThemeListFromString(PreferenceManager.themeListString)
    }

    fun convertUserObjectToString(user: User) : String{
        return Gson().toJson(user)
    }

    fun convertPrivilegeToString(privilege: Privilege?) : String{
        if (privilege == null) return ""
        return Gson().toJson(privilege)
    }

    private fun convertStringToPrivilege(privilegeString: String) : Privilege?{
        if (privilegeString == "") return null
        val gson = Gson()
        val type: Type = object : TypeToken<Privilege>() {}.type
        return gson.fromJson(privilegeString, type)
    }

    fun getPrivilege(privilegeString: String): Privilege?{
        return  convertStringToPrivilege(privilegeString)
    }

    fun convertDelegatorToString(delegator: Delegator?): String{
        if (delegator == null) return ""
        return Gson().toJson(delegator)
    }

    private fun convertStringToDelegator(delegatorString: String): Delegator?{
        val gson = Gson()
        val type: Type = object : TypeToken<Delegator?>() {}.type
        return gson.fromJson(delegatorString, type)
    }

    fun getDelegator(delegatorString: String): Delegator?{
        return convertStringToDelegator(delegatorString)
    }

    fun convertSettingsToString(settings: Settings?): String{
        if (settings == null) return ""
        return Gson().toJson(settings)
    }

    private fun convertStringToSettings(settingsString: String): Settings?{
        val gson = Gson()
        val type: Type = object : TypeToken<Settings?>() {}.type
        return gson.fromJson(settingsString, type)
    }

    fun getSettings(settingsString: String): Settings?{
        return convertStringToSettings(settingsString)
    }

    fun convertSyncDataToString(syncData: SyncData?): String{
        if (syncData == null) return ""
        return Gson().toJson(syncData)
    }

    private fun convertStringToSyncData(syncDataString: String): SyncData?{
        val gson = Gson()
        val type: Type = object : TypeToken<SyncData?>(){}.type
        return gson.fromJson(syncDataString, type)
    }

    fun getSyncData(syncDataString: String): SyncData?{
        return convertStringToSyncData(syncDataString)
    }

    fun convertOrganizationToString(organizationList: List<Organization>) : String{
        return Gson().toJson(organizationList)
    }

    fun getOrganizationList(organizationListString: String): List<Organization>?{
        val serializedObject: String = organizationListString
        val gson = Gson()
        val type: Type = object : TypeToken<List<Organization>>() {}.type
        return gson.fromJson(serializedObject, type)
    }

    fun getDocOrgList(organizationListString: String): List<DocOrganization>?{
        val serializedObject: String = organizationListString
        val gson = Gson()
        val type: Type = object : TypeToken<List<DocOrganization>>() {}.type
        return gson.fromJson(serializedObject, type)
    }

    fun convertDocOrgToString(organizationList: List<DocOrganization>) : String{
        return Gson().toJson(organizationList)
    }

    fun getDocUserList(docUserListString: String): List<DocExternalUser>?{
        val serializedObject: String = docUserListString
        val gson = Gson()
        val type: Type = object : TypeToken<List<DocExternalUser>>() {}.type
        return gson.fromJson(serializedObject, type)
    }

    fun convertDocUserToString(docUserList: List<DocExternalUser>) : String{
        return Gson().toJson(docUserList)
    }

    fun getRetrofitDownloadURL(themeID: String, filetype: Int): String{
        return if (filetype == AppConstants.FILE_TYPE_IMAGE)
            "idox/theme/$themeID"
        else "idox/theme/$themeID/icons"
    }

    fun convertErrorBody(throwable: HttpException): String? {
        return try {
            val body = throwable.response()!!.errorBody()
            convertStreamToString(body?.byteStream()!!)
        } catch (e : Exception){
            null
        }
    }

    private fun convertStreamToString(inputStream: InputStream): String {
        val reader = BufferedReader(InputStreamReader(inputStream))
        val sb = StringBuilder()
        var line: String? = null
        try {
            while (reader.readLine().also { line = it } != null) {
                sb.append(line).append('\n')
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                inputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return sb.toString()
    }

    fun convertUserListToString(userList: List<ExternalUser>): String{
        return Gson().toJson(userList)
    }

    fun convertStringToUserList(userListString: String): List<ExternalUser>?{
        val gson = Gson()
        val type: Type = object : TypeToken<List<ExternalUser>>() {}.type
        return gson.fromJson(userListString, type)
    }

    fun convertActionListToString(actionList: List<Action>): String{
        return Gson().toJson(actionList)
    }

    fun convertStringToActionList(actionListString: String): List<Action>?{
        val gson = Gson()
        val type: Type = object : TypeToken<List<Action>>() {}.type
        return gson.fromJson(actionListString, type)
    }

    fun convertCategoriesToString(categories: List<Category>): String{
        return Gson().toJson(categories)
    }

    fun convertStringToCategories(categoryString: String): List<Category>?{
        val gson = Gson()
        val type: Type = object : TypeToken<List<Category>>() {}.type
        return gson.fromJson(categoryString, type)
    }

    fun convertBoxListToString(boxList: List<Box>): String{
        return Gson().toJson(boxList)
    }

    fun convertStringToBoxList(boxListString: String): List<Box>?{
        val gson = Gson()
        val type: Type = object : TypeToken<List<Box>>() {}.type
        return gson.fromJson(boxListString, type)
    }

    fun convertDocumentListToString(documentList: List<DocID>): String{
        return Gson().toJson(documentList)
    }

    fun convertStringToDocumentList(documentListString: String): List<DocID>?{
        val gson = Gson()
        val type: Type = object : TypeToken<List<DocID>>() {}.type
        return gson.fromJson(documentListString, type)
    }

    fun convertJsonObjectToString(json: JsonObject): String{
        return json.toString()
    }

    fun convertStringToJson(jsonString: String): JsonObject{
        val jsonParser = JsonParser()
        return jsonParser.parse(jsonString) as JsonObject
    }

    fun getPriorityStyle(priorityValue : String): ThemeTextStyle?{
        return when (priorityValue) {
            "0" -> CommonUtils.getDefaultTheme()?.documentList?.item?.priorities?.v0
            "1" -> CommonUtils.getDefaultTheme()?.documentList?.item?.priorities?.v1
            "2" -> CommonUtils.getDefaultTheme()?.documentList?.item?.priorities?.v2
            else -> CommonUtils.getDefaultTheme()?.documentList?.item?.priorities?.v3
        }
    }

    fun getConfidentialStyle(priorityValue : String): ThemeTextStyle?{
        return when (priorityValue) {
            "0" -> CommonUtils.getDefaultTheme()?.documentList?.item?.confidential?.v0
            "1" -> CommonUtils.getDefaultTheme()?.documentList?.item?.confidential?.v1
            "2" -> CommonUtils.getDefaultTheme()?.documentList?.item?.confidential?.v2
            else -> CommonUtils.getDefaultTheme()?.documentList?.item?.confidential?.v3
        }
    }

    fun spToPx(sp: Float, context: Context): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            sp,
            context.resources.displayMetrics
        )
    }


    fun convertExternalTransferToString(documentList: List<ExternalTransferModel>): String{
        return Gson().toJson(documentList)
    }

    fun convertTransferToString(documentList: List<TransferShareModel>): String{
        return Gson().toJson(documentList)
    }
}