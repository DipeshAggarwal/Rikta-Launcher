package com.lumina.feature.profiles.ui.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.lumina.core.model.AppCategory
import com.lumina.core.ui.ThemeTokens
import com.lumina.core.ui.components.SelectableOptionRow
import com.lumina.core.ui.components.StandardBottomSheet
import com.lumina.core.ui.extensions.toDisplayString
import com.lumina.feature.profiles.R

@Composable
fun AutoAppCategoryBottomSheet(
    initialSelectedCategories: Set<AppCategory>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategories by remember { mutableStateOf(initialSelectedCategories) }

    StandardBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) { dismissAction ->
        Text(
            text = stringResource(R.string.profile_rules_auto_add_categories_select),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .padding(
                    horizontal = ThemeTokens.Spacing.ExtraLarge,
                    vertical = ThemeTokens.Spacing.Large
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = ThemeTokens.ExpandedContentMaxHeight)
                .padding(
                    horizontal = ThemeTokens.Spacing.ExtraLarge,
                    vertical = ThemeTokens.Spacing.Large
                )
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(ThemeTokens.Spacing.Small)
        ) {
            AppCategory.entries.forEach { category ->
                val isSelected = selectedCategories.contains(category)

                SelectableOptionRow(
                    title = category.toDisplayString(),
                    selected = isSelected,
                    onClick = {
                        selectedCategories = if (isSelected) selectedCategories - category
                        else selectedCategories + category
                    }
                )
            }
        }
    }
}