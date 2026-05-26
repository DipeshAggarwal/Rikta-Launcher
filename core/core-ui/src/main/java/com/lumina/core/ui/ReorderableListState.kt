package com.lumina.core.ui

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

class ReorderableListState(
    private val onMove: (fromIndex: Int, toIndex: Int) -> Unit
) {
    var draggedKey by mutableStateOf<String?>(null)
    var dragOffset by mutableFloatStateOf(0f)
    var measuredItemHeight by mutableIntStateOf(0)

    fun isDragging(key: String): Boolean = draggedKey == key

    fun itemModifier(key: String, index: Int, totalCount: Int): Modifier {
        val isDragging = isDragging(key)
        val maxDragUp = -index * measuredItemHeight.toFloat()
        val maxDragDown = (totalCount - 1 - index) * measuredItemHeight.toFloat()

        return Modifier
            .onSizeChanged { size -> if (measuredItemHeight == 0) measuredItemHeight = size.height }
            .zIndex(if (isDragging) 1f else 0f)
            .offset {
                IntOffset(
                    x = 0,
                    y = if (isDragging) dragOffset.coerceIn(maxDragUp, maxDragDown).roundToInt()
                        else 0
                )
            }
    }

    fun dragHandleModifier(key: String, currentIndex: Int, listSize: Int): Modifier {
        return Modifier.pointerInput(key) {
            detectVerticalDragGestures(
                onDragStart = {
                    draggedKey = key
                    dragOffset = 0f
                },
                onDragEnd = {
                    draggedKey = null
                    dragOffset = 0f
                },
                onDragCancel = {
                    draggedKey = null
                    dragOffset = 0f
                }
            ) { change, dragAmount ->
                change.consume()
                dragOffset += dragAmount

                val itemHeight = measuredItemHeight.toFloat()
                if (itemHeight == 0f) return@detectVerticalDragGestures
                val threshold = itemHeight / 2

                val fromIndex = currentIndex
                if (fromIndex == -1) return@detectVerticalDragGestures

                if (dragOffset > threshold && fromIndex < listSize - 1) {
                    onMove(fromIndex, fromIndex + 1)
                    dragOffset -= itemHeight
                } else if (dragOffset < -threshold && fromIndex > 0) {
                    onMove(fromIndex, fromIndex - 1)
                    dragOffset += itemHeight
                }
            }
        }
    }
}
