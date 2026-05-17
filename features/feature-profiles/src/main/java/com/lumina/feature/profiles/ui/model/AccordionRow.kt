package com.lumina.feature.profiles.ui.model

sealed interface AccordionRow {
    data class Switch(
        val title: String,
        val subtitle: String,
        val checked: Boolean,
        val onCheckedChange: (Boolean) -> Unit
    ) : AccordionRow

    data class Detail(
        val title: String,
        val subtitle: String,
        val onClick: () -> Unit
    ) : AccordionRow
}