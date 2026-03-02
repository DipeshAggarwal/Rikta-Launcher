package com.lumina.domain.apps

import com.lumina.core.model.AppInfo
import com.lumina.core.model.AppShortcut

interface AppShortcutRepository {
    suspend fun getShortcuts(app: AppInfo): List<AppShortcut>
}
