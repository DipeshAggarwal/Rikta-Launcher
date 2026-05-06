package com.lumina.core.ui.components.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lumina.core.ui.ThemeDimensions
import com.lumina.core.ui.ThemeTextDefaults
import com.lumina.core.ui.theme.ContentColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = ThemeDimensions.CompactTextFieldHeight,
    textStyle: TextStyle = MaterialTheme.typography.bodySmall,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
    labelName: String? = null,
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true
) {
    var focused by remember { mutableStateOf(false) }

    val interactionSource = remember { MutableInteractionSource() }
    val cursorAlpha by animateFloatAsState(
        targetValue = if (focused) 1f else 0f,
        animationSpec = tween(delayMillis = 200, durationMillis = 100)
    )

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = textStyle.copy(color = MaterialTheme.colorScheme.onSurface),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface.copy(alpha = cursorAlpha)),
        singleLine = singleLine,
        interactionSource = interactionSource,
        modifier = modifier
            .height(height)
            .onFocusChanged{ focused = it.isFocused },
        decorationBox = { innerTextField ->
            OutlinedTextFieldDefaults.DecorationBox(
                value = value,
                innerTextField = innerTextField,
                enabled = true,
                singleLine = singleLine,
                visualTransformation = VisualTransformation.None,
                interactionSource = interactionSource,
                label = labelName?.let { { Text(text = it, style= MaterialTheme.typography.labelSmall) } },
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
                container = {
                    OutlinedTextFieldDefaults.Container(
                        enabled = true,
                        isError = false,
                        interactionSource = interactionSource,
                        colors = colors,
                        shape = OutlinedTextFieldDefaults.shape
                    )
                },
                colors = colors
            )
        }
    )
}
