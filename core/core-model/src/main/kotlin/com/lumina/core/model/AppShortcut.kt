package com.lumina.core.model

data class AppShortcut(
    val shortcutId: String,
    val app: AppInfo,
    val shortLabel: String,
    val longLabel: String,
    val rank: Int
)