package com.lumina.core.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.lumina.core.ui.ThemeTokens
import kotlinx.coroutines.launch

typealias DismissAction = (afterDismiss: (() -> Unit)?) -> Unit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandardBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (dismissAction: DismissAction) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val dismissAction: DismissAction = { afterDismiss ->
        scope.launch {
            sheetState.hide()
            afterDismiss?.invoke()
        }
    }

    ModalBottomSheet(
        onDismissRequest = { dismissAction(onDismissRequest) },
        sheetState = sheetState,
        modifier = modifier.windowInsetsPadding(WindowInsets.ime),
        scrimColor = Color.Black.copy(alpha = ThemeTokens.Alpha.Strong),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        shape = MaterialTheme.shapes.large
    ) {
        content(dismissAction)
    }
}
