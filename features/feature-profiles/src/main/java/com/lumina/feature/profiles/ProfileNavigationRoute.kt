package com.lumina.feature.profiles

object ProfileNavigationRoute {
    const val PROFILE_ID_ARG = "profileId"
    const val TRIGGER_ID_ARG = "triggerId"

    const val PROFILE_LIST_ROUTE = "profile_list"
    const val PROFILE_CREATE_ROUTE = "profile_create"
    const val PROFILE_DETAIL_ROUTE = "profile_detail"

    const val PROFILE_GRAPH_DETAIL_ROUTE = "profile_detail/{$PROFILE_ID_ARG}"
    const val PROFILE_GRAPH_EDIT_ROUTE = "profile_edit/{$PROFILE_ID_ARG}"

    const val PROFILE_PATTERN_MANAGE_ROUTE = "profile_manage?profileId={$PROFILE_ID_ARG}"
    const val PROFILE_PATTERN_TRIGGER_ROUTE = "profile_trigger/{$PROFILE_ID_ARG}?triggerId={$TRIGGER_ID_ARG}"

    const val PROFILE_SUMMARY_SUB_ROUTE = "summary"

    fun detailRoute(profileId: String): String {
        return "profile_detail/$profileId"
    }

    fun editRoute(profileId: String): String {
        return "profile_edit/$profileId"
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
