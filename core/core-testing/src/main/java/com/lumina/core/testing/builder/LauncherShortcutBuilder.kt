package com.lumina.core.testing.builder

import com.lumina.core.model.LauncherShortcut
import com.lumina.core.model.ShortcutType

object LauncherShortcutBuilder {
    fun build(
        id: String = "shortcut_1",
        label: String = "Shortcut $id",
        type: ShortcutType = ShortcutType.APP_SHORTCUT,
        userHandleNumber: Long = 0L,
        shortcutPackage: String = "com.example.app",
        shortcutId: String = "shortcut_id"
    ) = LauncherShortcut(
        id = id,
        label = label,
        type = type,
        userHandleNumber = userHandleNumber,
        shortcutPackage = shortcutPackage,
        shortcutId = shortcutId,
        url = null,
        targetPackage = null
    )
}
