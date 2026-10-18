package com.devphilip.receipt_scanner.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

@Composable
fun ChevronLeft(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onBackground
) {
    Canvas(
        modifier = modifier
            .size(24.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(
                    bounded = false,
                    radius = 20.dp
                )
            ) {
                onClick()
            }
    ) {
        val strokeWidth = 2.5.dp.toPx()

        drawLine(
            color = color,
            start = Offset(
                x = size.width * 0.6f,
                y = size.height * 0.3f
            ),
            end = Offset(
                x = size.width * 0.35f,
                y = size.height * 0.5f
            ),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        drawLine(
            color = color,
            start = Offset(
                x = size.width * 0.35f,
                y = size.height * 0.5f
            ),
            end = Offset(
                x = size.width * 0.6f,
                y = size.height * 0.7f
            ),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}
