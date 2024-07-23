package com.knowledgeways.idocs.network.model.user

open class User{
    var delegators: Delegator? = null
    var fullName: String? = null
    var language: String?= null
    var loggedInDate: String? = null
    var organizationId: String? = null
    var organizationName: String? = null
    var organizations: List<Organization>? = null
    var prefix: String? = null
    var privileges: Privilege? = null
    var settings: Settings? = null
    var syncData: SyncData? = null
    var username: String?  = null
}

