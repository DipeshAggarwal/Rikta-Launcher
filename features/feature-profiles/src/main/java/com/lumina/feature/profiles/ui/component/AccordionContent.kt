package com.lumina.feature.profiles.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lumina.core.ui.ThemeTokens
import com.lumina.core.ui.components.DetailRow
import com.lumina.core.ui.components.SwitchRow
import com.lumina.feature.profiles.ui.model.AccordionRow

@Composable
fun AccordionContent(
    title: String,
    subtitle: String,
    rows: List<AccordionRow>,
    modifier: Modifier = Modifier
) {
    ExpandableCard(
        title = title,
        subtitle = subtitle,
        itemCount = rows.size,
        modifier = modifier
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            rows.forEachIndexed { index, row ->
                when (row) {
                    is AccordionRow.Switch -> SwitchRow(
                        title = row.title,
                        subtitle = row.subtitle,
                        checked = row.checked,
                        onCheckedChange = row.onCheckedChange
                    )
                    is AccordionRow.Detail -> DetailRow(
                        label = row.title,
                        subtitle = row.subtitle,
                        onClick = row.onClick,
                        stacked = true
                    )
                }

                if (index < rows.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = ThemeTokens.Spacing.ExtraLarge),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}
