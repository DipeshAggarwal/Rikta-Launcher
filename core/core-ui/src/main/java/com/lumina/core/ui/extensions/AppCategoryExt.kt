package com.lumina.core.ui.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.lumina.core.model.AppCategory
import com.lumina.core.ui.R

@Composable
fun AppCategory.toDisplayString(): String = stringResource(
    when(this) {
        AppCategory.GAME -> R.string.category_game
        AppCategory.SOCIAL -> R.string.category_social
        AppCategory.WORK_SOCIAL -> R.string.category_work_social
        AppCategory.PRODUCTIVITY -> R.string.category_productivity
        AppCategory.ENTERTAINMENT -> R.string.category_entertainment
        AppCategory.COMMUNICATION -> R.string.category_communication
        AppCategory.WORK_COMMUNICATION -> R.string.category_work_communication
        AppCategory.TOOLS -> R.string.category_tools
        AppCategory.HEALTH -> R.string.category_health
        AppCategory.FITNESS -> R.string.category_fitness
        AppCategory.FINANCE -> R.string.category_finance
        AppCategory.NEWS -> R.string.category_news
        AppCategory.EDUCATION -> R.string.category_education
        AppCategory.SHOPPING -> R.string.category_shopping
        AppCategory.TRAVEL -> R.string.category_travel
        AppCategory.UNCATEGORISED -> R.string.category_uncategorised
        AppCategory.CUSTOM -> R.string.category_custom
    }
)