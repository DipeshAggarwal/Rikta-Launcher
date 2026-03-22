package com.lumina.data.shortcut.builder

import com.lumina.core.model.ShortcutType
import com.lumina.domain.shortcut.model.LauncherShortcut

object LauncherShortcutBuilder {
    fun build(
        id: String = "shortcut_1",
        label: String = "Shortcut 1",
        pinnedToDefault: Boolean = false,
        type: ShortcutType = ShortcutType.APP_SHORTCUT
    ) = LauncherShortcut(
        id = id,
        label = label,
        pinnedToDefault = pinnedToDefault,
        type = type,
        shortcutPackage = null,
        shortcutId = null,
        url = null,
        targetPackage = null
    )
}
