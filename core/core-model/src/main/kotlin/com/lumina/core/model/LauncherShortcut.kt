package com.lumina.core.model

data class LauncherShortcut(
    val id: String,
    val label: String,
    val type: ShortcutType,
    val userHandleNumber: Long,
    val shortcutPackage: String?,
    val shortcutId: String?,
    val url: String?,
    val targetPackage: String?
)