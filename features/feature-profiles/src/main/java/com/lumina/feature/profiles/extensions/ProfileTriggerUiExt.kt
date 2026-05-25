package com.lumina.feature.profiles.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.lumina.core.model.LogicalOperator
import com.lumina.core.model.ProfileTriggerType
import com.lumina.feature.profiles.R

@Composable
fun ProfileTriggerType.displayName(): String = when (this) {
    ProfileTriggerType.TIME -> stringResource(R.string.profile_trigger_time)
    ProfileTriggerType.DAY -> stringResource(R.string.profile_trigger_day)
    ProfileTriggerType.LOCATION -> stringResource(R.string.profile_trigger_location)
    ProfileTriggerType.WIFI -> stringResource(R.string.profile_trigger_wifi)
    ProfileTriggerType.BLUETOOTH -> stringResource(R.string.profile_trigger_bluetooth)
}

@Composable
fun ProfileTriggerType.subtitle(): String = when (this) {
    ProfileTriggerType.TIME -> stringResource(R.string.profile_trigger_time_subtitle)
    ProfileTriggerType.DAY -> stringResource(R.string.profile_trigger_day_subtitle)
    ProfileTriggerType.LOCATION -> stringResource(R.string.profile_trigger_location_subtitle)
    ProfileTriggerType.WIFI -> stringResource(R.string.profile_trigger_wifi_subtitle)
    ProfileTriggerType.BLUETOOTH -> stringResource(R.string.profile_trigger_bluetooth_subtitle)
}

@Composable
fun LogicalOperator.displayName(): String = when (this) {
    LogicalOperator.AND -> stringResource(R.string.profile_trigger_logical_operator_and)
    LogicalOperator.OR -> stringResource(R.string.profile_trigger_logical_operator_or)
    LogicalOperator.NOT -> stringResource(R.string.profile_trigger_logical_operator_not)
}
