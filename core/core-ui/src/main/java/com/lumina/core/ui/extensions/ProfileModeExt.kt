package com.lumina.core.ui.extensions

import androidx.annotation.ColorRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEmotions
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.lumina.core.model.ProfileMode
import com.lumina.core.ui.R

@Composable
fun ProfileMode.displayName(): String = stringResource(
    when (this) {
        ProfileMode.ADMIN -> R.string.mode_admin_name
        ProfileMode.PRIVATE -> R.string.mode_private_name
        ProfileMode.WORK -> R.string.mode_work_name
        ProfileMode.FOCUS -> R.string.mode_focus_name
        ProfileMode.GUEST -> R.string.mode_guest_name
        ProfileMode.KID -> R.string.mode_kid_name
        ProfileMode.CUSTOM -> R.string.mode_custom_name
    }
)

@Composable
fun ProfileMode.subtitle(): String = stringResource(
    when (this) {
        ProfileMode.ADMIN -> R.string.mode_admin_subtitle
        ProfileMode.PRIVATE -> R.string.mode_private_subtitle
        ProfileMode.WORK -> R.string.mode_work_subtitle
        ProfileMode.FOCUS -> R.string.mode_focus_subtitle
        ProfileMode.GUEST -> R.string.mode_guest_subtitle
        ProfileMode.KID -> R.string.mode_kid_subtitle
        ProfileMode.CUSTOM -> R.string.mode_custom_subtitle
    }
)

@Composable
fun ProfileMode.description(): String = stringResource(
    when (this) {
        ProfileMode.ADMIN -> R.string.mode_admin_description
        ProfileMode.PRIVATE -> R.string.mode_private_description
        ProfileMode.WORK -> R.string.mode_work_description
        ProfileMode.FOCUS -> R.string.mode_focus_description
        ProfileMode.GUEST -> R.string.mode_guest_description
        ProfileMode.KID -> R.string.mode_kid_description
        ProfileMode.CUSTOM -> R.string.mode_custom_description
    }
)

@Composable
fun ProfileMode.icon(): ImageVector = when (this) {
    ProfileMode.ADMIN -> Icons.Outlined.WorkspacePremium
    ProfileMode.PRIVATE -> Icons.Outlined.Lock
    ProfileMode.WORK -> Icons.Outlined.Work
    ProfileMode.FOCUS -> Icons.Outlined.TrackChanges
    ProfileMode.GUEST -> Icons.Outlined.Person
    ProfileMode.KID -> Icons.Outlined.EmojiEmotions
    ProfileMode.CUSTOM -> Icons.Outlined.Tune
}

@ColorRes
fun ProfileMode.accentColourRes(): Int = when (this) {
    ProfileMode.ADMIN -> R.color.mode_admin_accent
    ProfileMode.PRIVATE -> R.color.mode_private_accent
    ProfileMode.WORK -> R.color.mode_work_accent
    ProfileMode.FOCUS -> R.color.mode_focus_accent
    ProfileMode.GUEST -> R.color.mode_guest_accent
    ProfileMode.KID -> R.color.mode_kid_accent
    ProfileMode.CUSTOM -> R.color.mode_custom_accent
}
