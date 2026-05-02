package com.lumina.data.shortcut.builder

import com.lumina.core.model.ShortcutType
import com.lumina.core.model.LauncherShortcut

object LauncherShortcutBuilder {
    fun build(
        id: String = "shortcut_1",
        label: String = "Shortcut 1",
        type: ShortcutType = ShortcutType.APP_SHORTCUT
    ) = LauncherShortcut(
        id = id,
        label = label,
        type = type,
        userHandleNumber = 0L,
        shortcutPackage = null,
        shortcutId = null,
        url = null,
        targetPackage = null
    )
}
