package com.lumina.core.ui.components.home

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lumina.core.ui.ThemeDimensions
import com.lumina.core.ui.ThemeTextDefaults
import com.lumina.core.ui.theme.ContentColor

@Composable
fun CompactTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = ThemeDimensions.CompactTextFieldHeight,
    textStyle: TextStyle = MaterialTheme.typography.bodySmall,
    colors: TextFieldColors = TextFieldDefaults.colors(),
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = textStyle.copy(color = MaterialTheme.colorScheme.onSurface),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
        singleLine = singleLine,
        modifier = modifier.height(height),
        decorationBox = { innerTextField ->
            TextFieldDefaults.DecorationBox(
                value = value,
                innerTextField = innerTextField,
                enabled = true,
                singleLine = singleLine,
                visualTransformation = VisualTransformation.None,
                interactionSource = interactionSource,
                placeholder = placeholder?.let {
                    { Text(
                        it,
                        style = textStyle,
                        color = ContentColor.copy(alpha = ThemeTextDefaults.PLACEHOLDER_TEXT_ALPHA)
                    )}
                },
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                contentPadding = PaddingValues(
                    horizontal = if (leadingIcon != null) 0.dp else ThemeDimensions.CompactTextFieldContentHorizontalPadding,
                    vertical = 0.dp
                ),
                colors = colors
            )
        }
    )
}
