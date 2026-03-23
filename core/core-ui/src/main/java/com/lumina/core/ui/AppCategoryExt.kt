package com.lumina.core.ui

import android.content.Context
import com.lumina.core.model.AppCategory

fun AppCategory.toDisplayString(context: Context): String = when(this) {
    AppCategory.GAME -> context.getString(R.string.category_game)
    AppCategory.SOCIAL -> context.getString(R.string.category_social)
    AppCategory.WORK_SOCIAL -> context.getString(R.string.category_work_social)
    AppCategory.PRODUCTIVITY -> context.getString(R.string.category_productivity)
    AppCategory.ENTERTAINMENT -> context.getString(R.string.category_entertainment)
    AppCategory.COMMUNICATION -> context.getString(R.string.category_communication)
    AppCategory.WORK_COMMUNICATION -> context.getString(R.string.category_work_communication)
    AppCategory.TOOLS -> context.getString(R.string.category_tools)
    AppCategory.HEALTH -> context.getString(R.string.category_health)
    AppCategory.FITNESS -> context.getString(R.string.category_fitness)
    AppCategory.FINANCE -> context.getString(R.string.category_finance)
    AppCategory.NEWS -> context.getString(R.string.category_news)
    AppCategory.EDUCATION -> context.getString(R.string.category_education)
    AppCategory.SHOPPING -> context.getString(R.string.category_shopping)
    AppCategory.TRAVEL -> context.getString(R.string.category_travel)
    AppCategory.UNCATEGORISED -> context.getString(R.string.category_uncategorised)
    AppCategory.CUSTOM -> context.getString(R.string.category_custom)
}
