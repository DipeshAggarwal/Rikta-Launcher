package com.lumina.feature.profiles

import com.lumina.core.model.ProfileTriggerType

object ProfileNavigationRoute {
    const val PROFILE_ID_ARG = "profileId"
    const val TRIGGER_ID_ARG = "triggerId"
    const val TRIGGER_TYPE_ARG = "triggerType"
    const val EDIT_SECTION_ARG = "section"

    const val PROFILE_LIST_ROUTE = "profile_list"
    const val PROFILE_CREATE_ROUTE = "profile_create"
    const val PROFILE_DETAIL_ROUTE = "profile_detail"

    const val PROFILE_GRAPH_DETAIL_ROUTE = "profile_detail/{$PROFILE_ID_ARG}"
    const val PROFILE_GRAPH_EDIT_ROUTE = "profile_edit/{$PROFILE_ID_ARG}"

    const val PROFILE_PATTERN_MANAGE_ROUTE = "profile_manage?profileId={$PROFILE_ID_ARG}"
    const val PROFILE_PATTERN_TRIGGER_LIST_ROUTE = "profile_triggers/{$PROFILE_ID_ARG}"
    const val PROFILE_PATTERN_TRIGGER_ROUTE = "profile_triggers/{$PROFILE_ID_ARG}/{$TRIGGER_ID_ARG}?triggerType={$TRIGGER_TYPE_ARG}"

    const val PROFILE_SUMMARY_ROUTE = "profile_edit/{$PROFILE_ID_ARG}/summary"
    const val PROFILE_RULES_ROUTE = "profile_edit/{$PROFILE_ID_ARG}/rules"

    fun detailRoute(profileId: String): String {
        return "profile_detail/$profileId"
    }

    fun editRoute(profileId: String, route: String): String {
        return "profile_edit/$profileId/$route"
    }

    fun manageRoute(profileId: String? = null): String {
        return if (profileId == null) "profile_manage"
        else "profile_manage?$PROFILE_ID_ARG=$profileId"
    }

    fun triggersListRoute(profileId: String): String {
        return "profile_triggers/$profileId"
    }

    fun triggerRoute(
        profileId: String,
        triggerId: Long,
        triggerType: ProfileTriggerType? = null
    ): String {
        val base = "profile_triggers/$profileId/$triggerId"
        return if (triggerType != null) "$base?$TRIGGER_TYPE_ARG=${triggerType.name}"
        else base
    }
}
