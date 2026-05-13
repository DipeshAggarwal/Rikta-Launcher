package com.lumina.feature.home.ui.component

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lumina.core.ui.ThemeTokens
import com.lumina.core.ui.components.home.CompactOutlinedTextField
import com.lumina.core.ui.theme.ContentColor
import com.lumina.feature.home.R

private object RenameAppDefaults {
    const val MAX_LENGTH = 32
    val CharDisplayPadding = 4.dp
}

@Composable
fun RenameApp(
    initialText: String,
    onSave: (String) -> Unit,
    onReset: () -> Unit,
    originalText: String? = null
) {
    var text by remember { mutableStateOf(initialText.takeIf { it != originalText } ?: "") }
    Log.w("TEST", originalText + "Original")
    Log.w("TEST", text + "text")

    Column(modifier = Modifier.fillMaxWidth()) {
        CompactOutlinedTextField(
            value = text,
            onValueChange = { if (it.length <= RenameAppDefaults.MAX_LENGTH) text = it },
            placeholder = originalText,
            labelName = stringResource(R.string.app_name_label),
            textStyle = MaterialTheme.typography.bodySmall,
            trailingIcon = {
                if (text.isNotEmpty()) {
                    IconButton(onClick = { text = "" }) {
                        Icon(Icons.Outlined.Clear, contentDescription = stringResource(R.string.clear_name))
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(ThemeTokens.DefaultCornerRadius)),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        )

        Text(
            text = stringResource(R.string.char_counter, text.length, RenameAppDefaults.MAX_LENGTH),
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = RenameAppDefaults.CharDisplayPadding),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { onReset() }) {
                Text(stringResource(R.string.rename_reset_label), color = ContentColor)
            }

            Spacer(modifier = Modifier.width(ThemeTokens.InLineButtonSpacerWidth))
            FilledTonalButton(
                onClick = { onSave(text) },
                shape = MaterialTheme.shapes.medium
            ) {
                Text(stringResource(R.string.rename_save_label), color = ContentColor)
            }
        }
    }
}
