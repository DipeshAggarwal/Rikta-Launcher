package com.lumina.core.ui.components.home

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumina.core.ui.theme.ContentColor

private object BottomSheetDefaults {
    const val MAX_HEIGHT_FRACTION = 0.8f

    val ContentPadding = 25.dp
    val IconSize = 45.dp
    val IconEndPadding = 10.dp
    val TitleFontSize = 32.sp
    val ActionsPaddingStart = 47.dp
    val ActionsBottomPadding = 50.dp
    val DividerVerticalPadding = 15.dp
    val ActionVerticalPadding = 10.dp
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(
    modifier: Modifier = Modifier,
    title: String,
    actions: List<BottomSheetAppAction>,
    onDismissRequest: () -> Unit,
    sheetState: SheetState,
    shortcutActions: List<BottomSheetAppAction> = emptyList()
) {
    val screenHeight = LocalWindowInfo.current.containerDpSize.height
    val scrollPos = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Column(
            modifier
                .heightIn(max = screenHeight * BottomSheetDefaults.MAX_HEIGHT_FRACTION)
                .fillMaxWidth()
                .padding(
                    start = BottomSheetDefaults.ContentPadding,
                    top = BottomSheetDefaults.ContentPadding,
                    end = BottomSheetDefaults.ContentPadding,
                    bottom = 0.dp
                )
                .verticalScroll(scrollPos)
        ) {
            // Header
            Row {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "App Options",
                    tint = ContentColor,
                    modifier = Modifier
                        .size(BottomSheetDefaults.IconSize)
                        .padding(end = BottomSheetDefaults.IconEndPadding)
                )
                Text(
                    title,
                    color = ContentColor,
                    fontSize = BottomSheetDefaults.TitleFontSize,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            HorizontalDivider(Modifier.padding(vertical = BottomSheetDefaults.DividerVerticalPadding))

            // Actions
            Column(Modifier.padding(
                start = BottomSheetDefaults.ActionsPaddingStart,
                bottom = BottomSheetDefaults.ActionsBottomPadding
            )) {
                if (!shortcutActions.isEmpty()) {
                    shortcutActions.forEach { action ->
                        Text(
                            text = action.label,
                            modifier = Modifier
                                .padding(vertical = BottomSheetDefaults.ActionVerticalPadding)
                                .combinedClickable(onClick = action.onClick),
                            color = ContentColor,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    HorizontalDivider(Modifier.padding(vertical = BottomSheetDefaults.DividerVerticalPadding))
                }

                actions.forEach { action ->
                    Text(
                        text = action.label,
                        modifier = Modifier
                            .padding(vertical = BottomSheetDefaults.ActionVerticalPadding)
                            .combinedClickable(onClick = action.onClick),
                        color = ContentColor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
