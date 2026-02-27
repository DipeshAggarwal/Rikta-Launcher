package com.lumina.domain.apps

interface AppShortcutRepository {
    suspend fun getShortcuts(app: AppInfo): List<AppShortcut>
}
