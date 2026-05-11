package com.lumina.feature.profiles

object ProfileNavigationRoute {
    const val PROFILE_ID_ARG = "profileId"
    const val TRIGGER_ID_ARG = "triggerId"

    const val PATTERN_DETAIL_ROUTE = "profile_detail/{$PROFILE_ID_ARG}"
    const val PATTERN_MANAGE_ROUTE = "profile_manage?profileId={$PROFILE_ID_ARG}"
    const val PATTERN_TRIGGER_ROUTE = "profile_trigger/{$PROFILE_ID_ARG}?triggerId={$TRIGGER_ID_ARG}"

    const val PROFILE_LIST_ROUTE = "profiles_list"
    const val PROFILE_CREATE_ROUTE = "profiles_create"

    fun detailRoute(profileId: String): String {
        return "profile_detail/$profileId"
    }

    fun manageRoute(profileId: String? = null): String {
        return if (profileId == null) "profile_manage"
        else "profile_manage?$PROFILE_ID_ARG=$profileId"
    }

    fun triggerRoute(profileId: String, triggerId: Long? = null): String {
        return if (triggerId != null) "profile_trigger/$profileId?triggerId=$triggerId"
        else "profile_trigger/$profileId"
    }
}
