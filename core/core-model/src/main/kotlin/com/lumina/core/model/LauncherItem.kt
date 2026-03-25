package com.lumina.core.model

sealed interface LauncherItem {
    val favouriteOrder: Int?

    data class App(
        val info: AppInfo,
        val showCountdown: Boolean,
        val recommendedUsageMinutes: Int?,
        override val favouriteOrder: Int?
    ) : LauncherItem

    data class Shortcut(
        val id: String,
        val label: String,
        val type: ShortcutType,
        val shortcutPackage: String?,
        val shortcutId: String?,
        val url: String?,
        val targetPackage: String?,
        override val favouriteOrder: Int?
    ) : LauncherItem
}
