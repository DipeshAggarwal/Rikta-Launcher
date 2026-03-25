package com.lumina.domain.shortcut.model

import com.lumina.core.model.ShortcutType

data class LauncherShortcut(
    val id: String,
    val label: String,
    val type: ShortcutType,
    val shortcutPackage: String?,
    val shortcutId: String?,
    val url: String?,
    val targetPackage: String?
)
