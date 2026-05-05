package com.lumina.feature.home.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lumina.core.ui.theme.ContentColor
import com.lumina.feature.home.R

private object RenameAppDefaults {
    const val MAX_LENGTH = 32
    val CharDisplayPadding = 4.dp
    val ButtonSpacerWidth = 8.dp
}

@Composable
fun RenameApp(
    initialText: String,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialText) }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = text,
            onValueChange = { if (it.length <= RenameAppDefaults.MAX_LENGTH) text = it },
            label = { Text(stringResource(R.string.app_name_label), style= MaterialTheme.typography.labelSmall) },
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
                .fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant,
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
            TextButton(onClick = { onSave(text) }) {
                Text(stringResource(R.string.reset_label), color = ContentColor)
            }

            Spacer(modifier = Modifier.width(RenameAppDefaults.ButtonSpacerWidth))
            FilledTonalButton(
                onClick = { onSave(text) },
                shape = MaterialTheme.shapes.medium
            ) {
                Text(stringResource(R.string.save_label), color = ContentColor)
            }
        }
    }
}
