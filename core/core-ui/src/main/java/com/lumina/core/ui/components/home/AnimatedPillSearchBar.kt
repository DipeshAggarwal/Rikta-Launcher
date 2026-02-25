package com.lumina.core.ui.components.home

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.lumina.core.ui.R
import com.lumina.core.ui.theme.BackgroundColor
import com.lumina.core.ui.theme.primaryContentColor

private object SearchBarDefaults {
    val ExpandedWidth = 280.dp
    val CollapsedWidth = 150.dp
    val Height = 56.dp
    val CornerRadius = 28.dp
    val HorizontalPadding = 12.dp
    val IconSize = 24.dp
    val ContentStartPadding = 4.dp
}
@Composable
fun AnimatedPillSearchBar(
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSearchTextChanged: (String) -> Unit,
    onSearchDone: (String) -> Unit,
    modifier: Modifier = Modifier,
    initialText: String = "",
    autoFocus: Boolean = false
) {
    var searchText by remember { mutableStateOf(TextFieldValue(initialText)) }

    // Animation Specs
    val width by animateDpAsState(
        targetValue = if (isExpanded) SearchBarDefaults.ExpandedWidth else SearchBarDefaults.CollapsedWidth,
        label = "widthAnimation"
    )
    val alpha by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        label = "alphaAnimation"
    )

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Handle Auto-focus and Expansion changes
    LaunchedEffect(isExpanded, autoFocus) {
        if (isExpanded) {
            focusRequester.requestFocus()
            keyboardController?.show()
        } else {
            keyboardController?.hide()
        }
    }

    Surface(
        modifier = modifier
            .width(width)
            .height(SearchBarDefaults.Height)
            .clickable { onExpandedChange(!isExpanded) },
        shape = RoundedCornerShape(SearchBarDefaults.CornerRadius),
        color = primaryContentColor
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = SearchBarDefaults.HorizontalPadding)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.search),
                tint = BackgroundColor,
                modifier = Modifier.size(SearchBarDefaults.IconSize)
            )

            if (!isExpanded) {
                Text(
                    text = stringResource(id = R.string.search),
                    color = BackgroundColor,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = SearchBarDefaults.ContentStartPadding)
                )
            } else {
                BasicTextField(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                        onSearchTextChanged(it.text)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = SearchBarDefaults.ContentStartPadding)
                        .alpha(alpha)
                        .focusRequester(focusRequester),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        keyboardController?.hide()
                        onSearchDone(searchText.text.trim())
                    }),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = BackgroundColor
                    ),
                    cursorBrush = SolidColor(BackgroundColor)
                )
            }
        }
    }
}
