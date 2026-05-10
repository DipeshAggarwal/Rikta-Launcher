package com.lumina.feature.home.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.lumina.core.model.AppCategory
import com.lumina.core.ui.Motion
import com.lumina.core.ui.ThemeDimensions
import com.lumina.core.ui.theme.ContentColor
import com.lumina.core.ui.extensions.toDisplayString
import com.lumina.feature.home.R

@Composable
fun ChangeAppCategory(
    currentCategory: AppCategory,
    onSave: (AppCategory) -> Unit,
    onReset: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(currentCategory) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = ThemeDimensions.ExpandedContentMaxHeight)
                .verticalScroll(rememberScrollState())
        ) {
            AppCategory.entries.forEach { category ->
                val isSelectedCategory = selectedCategory == category
                val categoryName = category.toDisplayString()

                val checkAlpha by animateFloatAsState(
                    targetValue = if (isSelectedCategory) 1f else 0f,
                    animationSpec = tween(Motion.SHEET_TRANSITION_MS),
                    label = "check_alpha"
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(ThemeDimensions.DefaultCornerRadius))
                        .clickable { selectedCategory = category },
                    color = if (isSelectedCategory) MaterialTheme.colorScheme.secondaryContainer
                        else Color.Transparent,
                    contentColor = if (isSelectedCategory) MaterialTheme.colorScheme.onSecondaryContainer
                        else ContentColor
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedCategory = category }
                            .padding(
                                vertical = ThemeDimensions.RowVerticalPadding,
                                horizontal = ThemeDimensions.RowHorizontalPadding
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = categoryName, style = MaterialTheme.typography.bodySmall)

                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = stringResource(R.string.category_selected_label),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.alpha(checkAlpha)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { onReset() }) {
                Text(stringResource(R.string.category_reset_label), color = ContentColor)
            }

            Spacer(modifier = Modifier.width(ThemeDimensions.InLineButtonSpacerWidth))
            FilledTonalButton(
                onClick = { onSave(selectedCategory) },
                shape = MaterialTheme.shapes.medium
            ) {
                Text(stringResource(R.string.category_save_label), color = ContentColor)
            }
        }
    }
}
