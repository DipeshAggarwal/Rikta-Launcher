package com.lumina.core.model

object SystemProfileIds {
    const val DEFAULT = "_____launcher_default_____"
    const val FOCUS = "_____launcher_focus_____"
    const val GUEST = "_____launcher_guest_____"
    const val WORK = "_____system_work_____"
    const val PRIVATE = "_____system_private_____"

    val AUTO_CREATE = listOf(FOCUS, GUEST, WORK)
}
