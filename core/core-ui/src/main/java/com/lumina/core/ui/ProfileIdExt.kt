package com.lumina.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.lumina.core.model.SystemProfileIds

@Composable
fun systemProfileDisplayName(name: String): String {
    val resId = when (name) {
        SystemProfileIds.DEFAULT -> R.string.launcher_default
        SystemProfileIds.FOCUS -> R.string.launcher_focus
        SystemProfileIds.GUEST -> R.string.launcher_guest
        SystemProfileIds.WORK -> R.string.system_work
        SystemProfileIds.PRIVATE -> R.string.system_private
        else -> null
    }

    return if (resId != null) stringResource(resId) else name
}
