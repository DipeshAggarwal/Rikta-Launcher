package com.lumina.core.ui.extensions

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEmotions
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import com.lumina.core.model.ProfilePreset
import com.lumina.core.ui.R

@Composable
fun ProfilePreset.displayName(): String = stringResource(
    when (this) {
        ProfilePreset.ADMIN -> R.string.mode_admin_name
        ProfilePreset.PRIVATE -> R.string.mode_private_name
        ProfilePreset.WORK -> R.string.mode_work_name
        ProfilePreset.FOCUS -> R.string.mode_focus_name
        ProfilePreset.GUEST -> R.string.mode_guest_name
        ProfilePreset.KID -> R.string.mode_kid_name
        ProfilePreset.CUSTOM -> R.string.mode_custom_name
    }
)

@Composable
fun ProfilePreset.subtitle(): String = stringResource(
    when (this) {
        ProfilePreset.ADMIN -> R.string.mode_admin_subtitle
        ProfilePreset.PRIVATE -> R.string.mode_private_subtitle
        ProfilePreset.WORK -> R.string.mode_work_subtitle
        ProfilePreset.FOCUS -> R.string.mode_focus_subtitle
        ProfilePreset.GUEST -> R.string.mode_guest_subtitle
        ProfilePreset.KID -> R.string.mode_kid_subtitle
        ProfilePreset.CUSTOM -> R.string.mode_custom_subtitle
    }
)

@Composable
fun ProfilePreset.description(): String = stringResource(
    when (this) {
        ProfilePreset.ADMIN -> R.string.mode_admin_description
        ProfilePreset.PRIVATE -> R.string.mode_private_description
        ProfilePreset.WORK -> R.string.mode_work_description
        ProfilePreset.FOCUS -> R.string.mode_focus_description
        ProfilePreset.GUEST -> R.string.mode_guest_description
        ProfilePreset.KID -> R.string.mode_kid_description
        ProfilePreset.CUSTOM -> R.string.mode_custom_description
    }
)

fun ProfilePreset.icon(): ImageVector = when (this) {
    ProfilePreset.ADMIN -> Icons.Outlined.WorkspacePremium
    ProfilePreset.PRIVATE -> Icons.Outlined.Lock
    ProfilePreset.WORK -> Icons.Outlined.Work
    ProfilePreset.FOCUS -> Icons.Outlined.TrackChanges
    ProfilePreset.GUEST -> Icons.Outlined.Person
    ProfilePreset.KID -> Icons.Outlined.EmojiEmotions
    ProfilePreset.CUSTOM -> Icons.Outlined.Tune
}

@Composable
fun ProfilePreset.accentColourRes(): Color = colorResource(when (this) {
    ProfilePreset.ADMIN -> R.color.mode_admin_accent
    ProfilePreset.PRIVATE -> R.color.mode_private_accent
    ProfilePreset.WORK -> R.color.mode_work_accent
    ProfilePreset.FOCUS -> R.color.mode_focus_accent
    ProfilePreset.GUEST -> R.color.mode_guest_accent
    ProfilePreset.KID -> R.color.mode_kid_accent
    ProfilePreset.CUSTOM -> R.color.mode_custom_accent
})
