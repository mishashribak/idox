package com.knowledgeways.idocs.db

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.JsonObject
import com.knowledgeways.idocs.network.model.*
import com.knowledgeways.idocs.network.model.user.*
import com.knowledgeways.idocs.utils.AppConstants
import com.knowledgeways.idocs.utils.CommonUtils
import com.knowledgeways.idocs.utils.ConverterUtils
import com.knowledgeways.idocs.utils.MessageEvent
import org.greenrobot.eventbus.EventBus


object PreferenceManager {

    //Constants
    private const val PREF_KEY_DEFAULT_THEME_ID = "PREF_KEY_DEFAULT_THEME_ID"
    private const val PREF_KEY_RESOURCE_DOWNLOAD_STATUS = "PREF_KEY_RESOURCE_DOWNLOAD_STATUS"
    private const val PREF_KEY_CURRENT_USER_LANGUAGE = "PREF_KEY_CURRENT_USER_LANGUAGE"
    private const val PREF_KEY_THEME_LIST = "PREF_KEY_THEME_LIST"

    // User Object
    private const val PREF_KEY_USER_NAME = "PREF_KEY_USER_NAME"
    private const val PREF_KEY_PREFIX = "PREF_KEY_PREFIX"
    private const val PREF_KEY_FULL_NAME = "PREF_KEY_FULL_NAME"
    private const val PREF_KEY_language = "PREF_KEY_LANGUAGE"
    private const val PREF_KEY_ORGANIZATION_ID = "PREF_KEY_ORGANIZATION_ID"
    private const val PREF_KEY_ORGANIZATION_NAME = "PREF_KEY_ORGANIZATION_NAME"
    private const val PREF_KEY_LOGGED_IN_DATE = "PREF_KEY_LOGGED_IN_DATE"
    private const val PREF_KEY_USER_ORGANIZATION_LIST = "PREF_KEY_USER_ORGANIZATION_LIST"
    private const val PREF_KEY_DELEGATORS = "PREF_KEY_DELEGATOR"
    private const val PREF_KEY_PRIVILEGES = "PREF_KEY_PRIVILEGES"
    private const val PREF_KEY_SETTINGS = "PREF_KEY_SETTINGS"
    private const val PREF_KEY_SYNC_DATA = "PREF_KEY_SYNC_DATA"

    private const val PREF_KEY_SESSION_ID = "PREF_KEY_SESSION_ID"
    private const val PREF_KEY_REMEMBER_ME = "PREF_KEY_REMEMBER_ME"

    private const val PREF_KEY_INTERNAL_ORGANIZATION = "PREF_KEY_INTERNAL_ORGANIZATION"
    private const val PREF_KEY_EXTERNAL_ORGANIZATION = "PREF_KEY_EXTERNAL_ORGANIZATION"
    private const val PREF_KEY_DOC_FAVORITE_EXTERNAL_ORGANIZATION = "PREF_KEY_DOC_FAVORITE_EXTERNAL_ORGANIZATION"
    private const val PREF_KEY_DOC_ALL_EXTERNAL_ORGANIZATION = "PREF_KEY_DOC_ALL_EXTERNAL_ORGANIZATION"
    private const val PREF_KEY_EXTERNAL_USERS = "PREF_KEY_EXTERNAL_USERS"
    private const val PREF_KEY_DOC_FAVORITE_EXTERNAL_USERS = "PREF_KEY_DOC_FAVORITE_EXTERNAL_USERS"
    private const val PREF_KEY_DOC_ALL_EXTERNAL_USERS = "PREF_KEY_DOC_ALL_EXTERNAL_USERS"
    private const val PREF_KEY_ACTIONS = "PREF_KEY_ACTIONS"
    private const val PREF_KEY_PRIORITIES = "PREF_KEY_PRIORITIES"
    private const val PREF_KEY_CONFIDENTIAL = "PREF_KEY_CONFIDENTIAL"
    private const val PREF_KEY_CATEGORIES = "PREF_KEY_CATEGORIES"
    private const val PREF_KEY_TRANSFERABLE_ORGANIZATIONS = "PREF_KEY_TRANSFERABLE_ORGANIZATIONS"
    private const val PREF_KEY_MIXED_ORGANIZATIONS = "PREF_KEY_MIXED_ORGANIZATIONS"
    private const val PREF_KEY_BOX_LIST = "PREF_KEY_BOX_LIST"
    private const val PREF_KEY_BOX_DOCUMENT_TOTAL = "PREF_KEY_BOX_DOCUMENT_TOTAL"
    private const val PREF_KEY_DOCUMENT_LIST = "PREF_KEY_DOCUMENT_LIST"

    private const val PREF_KEY_LOGGED_IN = "PREF_KEY_LOGGED_IN"
    private const val PREF_KEY_TOOLBAR_COLOR = "PREF_KEY_TOOLBAR_COLOR"
    private const val PREF_KEY_API_BASE_URL = "PREF_KEY_API_BASE_URL"

    private const val PREF_KEY_DOCUMENT_FAVORITE_ORGANIZATION_LIST = "PREF_KEY_DOCUMENT_FAVORITE_ORGANIZATION_LIST"
    private const val PREF_KEY_DOCUMENT_ALL_ORG_LIST = "PREF_KEY_DOCUMENT_ALL_ORG_LIST"

    private const val PREF_KEY_DOCUMENT_SELECTED_USER_LIST = "PREF_KEY_DOCUMENT_SELECTED_USER_LIST"
    private const val PREF_KEY_DOCUMENT_SELECTED_ORG_LIST = "PREF_KEY_DOCUMENT_SELECTED_ORG_LIST"
    private const val PREF_KEY_DOCUMENT_SELECTED_SISTER_ORG_LIST = "PREF_KEY_DOCUMENT_SELECTED_SISTER_ORG_LIST"

    lateinit var mPrefs: SharedPreferences

    fun init(context: Context) {
        mPrefs = context.getSharedPreferences(AppConstants.APP_PREF_NAME, Context.MODE_PRIVATE)
    }

    var resourceStatus: Int // 0 : Never started 1: themeDownloaded 2: Zip downloaded 3: Unzip Completed
        get() = mPrefs.getInt(PREF_KEY_RESOURCE_DOWNLOAD_STATUS, 0)
        set(status) {
            mPrefs.edit().putInt(PREF_KEY_RESOURCE_DOWNLOAD_STATUS, status).apply()
        }

    var userLanguage: Int // 1: English 2: Arabic
        get() = mPrefs.getInt(PREF_KEY_CURRENT_USER_LANGUAGE, 1)
        set(language) {
            mPrefs.edit().putInt(PREF_KEY_CURRENT_USER_LANGUAGE, language).apply()
        }

    var themeListString: String
        get() = mPrefs.getString(PREF_KEY_THEME_LIST, "") ?: ""
        set(value) {
            mPrefs.edit().putString(PREF_KEY_THEME_LIST, value).apply()
        }

    private var _user: User? = null
    var user: User?
        get() {
            if (_user != null) {
                _user = User()
                _user!!.username = userName
                _user!!.prefix = prefix
                _user!!.fullName = fullName
                _user!!.organizationId = organizationID
                _user!!.organizationName = organizationName
                _user!!.loggedInDate = loggedInDate
                _user!!.organizations = userOrganizationList
                _user!!.privileges = privilege
                _user!!.delegators = delegator
                _user!!.settings = settings
                _user!!.syncData = syncData
            }
            return _user
        }
        set(value) {
            _user = value
            userName = value?.username ?: ""
            prefix = value?.prefix ?: ""
            fullName = value?.fullName ?: ""
            organizationID = value?.organizationId ?: ""
            organizationName = value?.organizationName ?: ""
            loggedInDate = value?.loggedInDate ?: ""
            userOrganizationList = value?.organizations ?: ArrayList()
            privilege = value?.privileges
            delegator = value?.delegators
            settings = value?.settings
            syncData = value?.syncData
        }

    // UserName
    var userName: String
        get() = mPrefs.getString(PREF_KEY_USER_NAME, "") ?: ""
        set(userName) {
            mPrefs.edit().putString(PREF_KEY_USER_NAME, userName).apply()
        }

    // Prefix
    var prefix: String
        get() = mPrefs.getString(PREF_KEY_PREFIX, "") ?: ""
        set(prefix) {
            mPrefs.edit().putString(PREF_KEY_PREFIX, prefix).apply()
        }

    // FullName
    var fullName: String
        get() = mPrefs.getString(PREF_KEY_FULL_NAME, "") ?: ""
        set(fullName) {
            mPrefs.edit().putString(PREF_KEY_FULL_NAME, fullName).apply()
        }

    // OrganizationId
    var organizationID: String
        get() = mPrefs.getString(PREF_KEY_ORGANIZATION_ID, "") ?: ""
        set(value) {
            mPrefs.edit().putString(PREF_KEY_ORGANIZATION_ID, value).apply()
        }

    // Organization Name
    var organizationName: String
        get() = mPrefs.getString(PREF_KEY_ORGANIZATION_NAME, "") ?: ""
        set(value) {
            mPrefs.edit().putString(PREF_KEY_ORGANIZATION_NAME, value).apply()
        }

    // Logged-in date
    var loggedInDate: String
        get() = mPrefs.getString(PREF_KEY_LOGGED_IN_DATE, "") ?: ""
        set(value) {
            mPrefs.edit().putString(PREF_KEY_LOGGED_IN_DATE, value).apply()
        }

    //Organization List
    var userOrganizationList: List<Organization>
        get() = ConverterUtils.getOrganizationList(
            mPrefs.getString(PREF_KEY_USER_ORGANIZATION_LIST, "") ?: ""
        ) ?: ArrayList()
        set(value) {
            mPrefs.edit().putString(
                PREF_KEY_USER_ORGANIZATION_LIST,
                ConverterUtils.convertOrganizationToString(value)
            ).apply()
        }

    var docFavoriteOrganizationList: List<DocOrganization>
        get() = ConverterUtils.getDocOrgList(
            mPrefs.getString(PREF_KEY_DOC_FAVORITE_EXTERNAL_ORGANIZATION, "") ?: ""
        ) ?: ArrayList()
        set(value) {
            mPrefs.edit().putString(
                PREF_KEY_DOC_FAVORITE_EXTERNAL_ORGANIZATION,
                ConverterUtils.convertDocOrgToString(value)
            ).apply()
        }

    var docSelectedOrgList: List<DocOrganization>
        get() = ConverterUtils.getDocOrgList(
            mPrefs.getString(PREF_KEY_DOCUMENT_SELECTED_ORG_LIST, "") ?: ""
        ) ?: ArrayList()
        set(value) {
            mPrefs.edit().putString(
                PREF_KEY_DOCUMENT_SELECTED_ORG_LIST,
                ConverterUtils.convertDocOrgToString(value)
            ).apply()
            EventBus.getDefault().post(MessageEvent(AppConstants.MESSAGE_SELECTED_ORG_CHANGED))
        }

    var docAllOrganizationList: List<DocOrganization>
        get() = ConverterUtils.getDocOrgList(
            mPrefs.getString(PREF_KEY_DOCUMENT_ALL_ORG_LIST, "") ?: ""
        ) ?: ArrayList()
        set(value) {
            mPrefs.edit().putString(
                PREF_KEY_DOCUMENT_ALL_ORG_LIST,
                ConverterUtils.convertDocOrgToString(value)
            ).apply()
        }


    var docFavoriteSisterOrganization: List<DocOrganization>
        get() = ConverterUtils.getDocOrgList(
            mPrefs.getString(PREF_KEY_DOC_ALL_EXTERNAL_ORGANIZATION, "") ?: ""
        ) ?: ArrayList()
        set(value) {
            mPrefs.edit().putString(
                PREF_KEY_DOC_ALL_EXTERNAL_ORGANIZATION,
                ConverterUtils.convertDocOrgToString(value)
            ).apply()
        }

    var docAllSisterOrganization: List<DocOrganization>
        get() = ConverterUtils.getDocOrgList(
            mPrefs.getString(PREF_KEY_DOCUMENT_FAVORITE_ORGANIZATION_LIST, "") ?: ""
        ) ?: ArrayList()
        set(value) {
            mPrefs.edit().putString(
                PREF_KEY_DOCUMENT_FAVORITE_ORGANIZATION_LIST,
                ConverterUtils.convertDocOrgToString(value)
            ).apply()
        }

    var docSelectedSisterOrgList: List<DocOrganization>
        get() = ConverterUtils.getDocOrgList(
            mPrefs.getString(PREF_KEY_DOCUMENT_SELECTED_SISTER_ORG_LIST, "") ?: ""
        ) ?: ArrayList()
        set(value) {
            mPrefs.edit().putString(
                PREF_KEY_DOCUMENT_SELECTED_SISTER_ORG_LIST,
                ConverterUtils.convertDocOrgToString(value)
            ).apply()
            EventBus.getDefault().post(MessageEvent(AppConstants.MESSAGE_SELECTED_SISTER_ORG_CHANGED))
        }

    var docFavoriteUserList: List<DocExternalUser>
        get() = ConverterUtils.getDocUserList(
            mPrefs.getString(PREF_KEY_DOC_FAVORITE_EXTERNAL_USERS, "") ?: ""
        ) ?: ArrayList()
        set(value) {
            mPrefs.edit().putString(
                PREF_KEY_DOC_FAVORITE_EXTERNAL_USERS,
                ConverterUtils.convertDocUserToString(value)
            ).apply()
        }

    var docSelectedUserList: List<DocExternalUser>
        get() = ConverterUtils.getDocUserList(
            mPrefs.getString(PREF_KEY_DOCUMENT_SELECTED_USER_LIST, "") ?: ""
        ) ?: ArrayList()
        set(value) {
            mPrefs.edit().putString(
                PREF_KEY_DOCUMENT_SELECTED_USER_LIST,
                ConverterUtils.convertDocUserToString(value)
            ).apply()
            EventBus.getDefault().post(MessageEvent(AppConstants.MESSAGE_SELECTED_USER_CHANGED))
        }

    var docAllUserList: List<DocExternalUser>
        get() = ConverterUtils.getDocUserList(
            mPrefs.getString(PREF_KEY_DOC_ALL_EXTERNAL_USERS, "") ?: ""
        ) ?: ArrayList()
        set(value) {
            mPrefs.edit().putString(
                PREF_KEY_DOC_ALL_EXTERNAL_USERS,
                ConverterUtils.convertDocUserToString(value)
            ).apply()
        }

    // Privileges
    var privilege: Privilege?
        get() = ConverterUtils.getPrivilege(mPrefs.getString(PREF_KEY_PRIVILEGES, "") ?: "")
        set(value) {
            mPrefs.edit()
                .putString(PREF_KEY_PRIVILEGES, ConverterUtils.convertPrivilegeToString(value))
                .apply()
        }

    // Delegator
    var delegator: Delegator?
        get() = ConverterUtils.getDelegator(mPrefs.getString(PREF_KEY_DELEGATORS, "") ?: "")
        set(value) {
            mPrefs.edit()
                .putString(PREF_KEY_DELEGATORS, ConverterUtils.convertDelegatorToString(value))
                .apply()
        }

    // Settings
    var settings: Settings?
        get() = ConverterUtils.getSettings(mPrefs.getString(PREF_KEY_SETTINGS, "") ?: "")
        set(value) {
            mPrefs.edit()
                .putString(PREF_KEY_SETTINGS, ConverterUtils.convertSettingsToString(value)).apply()
        }

    // SyncData
    var syncData: SyncData?
        get() = ConverterUtils.getSyncData(mPrefs.getString(PREF_KEY_SYNC_DATA, "") ?: "")
        set(value) {
            mPrefs.edit()
                .putString(PREF_KEY_SYNC_DATA, ConverterUtils.convertSyncDataToString(value))
                .apply()
        }

    var sessionId: String
        get() = mPrefs.getString(PREF_KEY_SESSION_ID, "") ?: ""
        set(value) {
            mPrefs.edit().putString(PREF_KEY_SESSION_ID, value).apply()
        }

    var rememberMe: String
        get() = mPrefs.getString(PREF_KEY_REMEMBER_ME, "") ?: ""
        set(value) {
            mPrefs.edit().putString(PREF_KEY_REMEMBER_ME, value).apply()
        }

    var loggedInStatus: Int
        get() = mPrefs.getInt(PREF_KEY_LOGGED_IN, 0) ?: 0
        set(value) {
            mPrefs.edit().putInt(PREF_KEY_LOGGED_IN, value).apply()
        }

    var internalOrganization: List<Organization>
        get() = ConverterUtils.getOrganizationList(
            mPrefs.getString(PREF_KEY_INTERNAL_ORGANIZATION, "") ?: ""
        ) ?: ArrayList()
        set(value) {
            mPrefs.edit().putString(
                PREF_KEY_INTERNAL_ORGANIZATION,
                ConverterUtils.convertOrganizationToString(value)
            ).apply()
        }

    var externalOrganization: List<Organization>
        get() = ConverterUtils.getOrganizationList(
            mPrefs.getString(PREF_KEY_EXTERNAL_ORGANIZATION, "") ?: ""
        ) ?: ArrayList()
        set(value) {
            mPrefs.edit().putString(
                PREF_KEY_EXTERNAL_ORGANIZATION,
                ConverterUtils.convertOrganizationToString(value)
            ).apply()
        }

    var externalUsers: List<ExternalUser>
        get() = ConverterUtils.convertStringToUserList(
            mPrefs.getString(PREF_KEY_EXTERNAL_USERS, "") ?: ""
        ) ?: ArrayList()
        set(value) {
            mPrefs.edit().putString(
                PREF_KEY_EXTERNAL_USERS,
                ConverterUtils.convertUserListToString(value)
            ).apply()
        }

    var actions: List<Action>
        get() = ConverterUtils.convertStringToActionList(
            mPrefs.getString(PREF_KEY_ACTIONS, "") ?: ""
        ) ?: ArrayList()
        set(value) {
            mPrefs.edit().putString(
                PREF_KEY_ACTIONS,
                ConverterUtils.convertActionListToString(value)
            ).apply()
        }

    var priorities: List<Action>
        get() = ConverterUtils.convertStringToActionList(
            mPrefs.getString(PREF_KEY_PRIORITIES, "") ?: ""
        ) ?: ArrayList()
        set(value) {
            mPrefs.edit().putString(
                PREF_KEY_PRIORITIES,
                ConverterUtils.convertActionListToString(value)
            ).apply()
        }

    var confidential: List<Action>
        get() = ConverterUtils.convertStringToActionList(
            mPrefs.getString(PREF_KEY_CONFIDENTIAL, "") ?: ""
        ) ?: ArrayList()
        set(value) {
            mPrefs.edit().putString(
                PREF_KEY_CONFIDENTIAL,
                ConverterUtils.convertActionListToString(value)
            ).apply()
        }

    var categories: List<Category>
        get() = ConverterUtils.convertStringToCategories(
            mPrefs.getString(PREF_KEY_CATEGORIES, "") ?: ""
        ) ?: ArrayList()
        set(value) {
            mPrefs.edit().putString(
                PREF_KEY_CATEGORIES,
                ConverterUtils.convertCategoriesToString(value)
            ).apply()
        }

    var boxes: List<Box>
        get() = ConverterUtils.convertStringToBoxList(
            mPrefs.getString(PREF_KEY_BOX_LIST, "") ?: ""
        ) ?: ArrayList()
        set(value) {
            mPrefs.edit().putString(
                PREF_KEY_BOX_LIST,
                ConverterUtils.convertBoxListToString(value)
            ).apply()
        }

    var documentTotal: JsonObject
        get() = ConverterUtils.convertStringToJson(
            mPrefs.getString(PREF_KEY_BOX_DOCUMENT_TOTAL, "") ?: ""
        )
        set(value) {
            mPrefs.edit().putString(
                PREF_KEY_BOX_DOCUMENT_TOTAL,
                ConverterUtils.convertJsonObjectToString(value)
            ).apply()
        }

    var transferableOrganization: List<Organization>
        get() = ConverterUtils.getOrganizationList(
            mPrefs.getString(PREF_KEY_TRANSFERABLE_ORGANIZATIONS, "") ?: ""
        ) ?: ArrayList()
        set(value) {
            mPrefs.edit().putString(
                PREF_KEY_TRANSFERABLE_ORGANIZATIONS,
                ConverterUtils.convertOrganizationToString(value)
            ).apply()
        }

    var mixedOrganization: List<Organization>
        get() = ConverterUtils.getOrganizationList(
            mPrefs.getString(PREF_KEY_MIXED_ORGANIZATIONS, "") ?: ""
        ) ?: ArrayList()
        set(value) {
            mPrefs.edit().putString(
                PREF_KEY_MIXED_ORGANIZATIONS,
                ConverterUtils.convertOrganizationToString(value)
            ).apply()
        }

    var documentList: List<DocID>
        get() = ConverterUtils.convertStringToDocumentList(
            mPrefs.getString(PREF_KEY_DOCUMENT_LIST, "") ?: ""
        ) ?: ArrayList()
        set(value) {
            mPrefs.edit().putString(
                PREF_KEY_DOCUMENT_LIST,
                ConverterUtils.convertDocumentListToString(value)
            ).apply()
        }

    var toolbarColor: String
        get() = mPrefs.getString(PREF_KEY_TOOLBAR_COLOR, "") ?: ""
        set(value) {
            mPrefs.edit().putString(PREF_KEY_TOOLBAR_COLOR, value).apply()
        }

    var baseUrl: String
        get() = mPrefs.getString(PREF_KEY_API_BASE_URL, CommonUtils.reformatUrl(AppConstants.BASE_URL)) ?: CommonUtils.reformatUrl(AppConstants.BASE_URL)
        set(value) {
            mPrefs.edit().putString(PREF_KEY_API_BASE_URL, value).apply()
        }
}