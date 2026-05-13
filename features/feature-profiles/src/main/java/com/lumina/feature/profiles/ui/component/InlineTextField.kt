package com.lumina.feature.profiles.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.lumina.core.ui.ThemeTokens

@Composable
fun InLineTextField(
    label: String,
    value: String,
    maxLength: Int,
    maxLines: Int,
    onValueChange: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit,
    imeAction: ImeAction,
    modifier: Modifier = Modifier
) {
    val singleLine = maxLines == 1

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = ThemeTokens.Spacing.ExtraLarge,
                vertical = ThemeTokens.Spacing.Large
            )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(ThemeTokens.Spacing.Small))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { onFocusChange(it.isFocused) },
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = imeAction
                ),
                singleLine = singleLine,
                maxLines = maxLines
            )
            Spacer(modifier = Modifier.width(ThemeTokens.Spacing.Medium))
            Text(
                text = "${value.length}/$maxLength",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
