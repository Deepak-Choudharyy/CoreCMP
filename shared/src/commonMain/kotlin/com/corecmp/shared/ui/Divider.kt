package com.corecmp.shared.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.corecmp.shared.theme.grayColor

@Composable
fun DashedDivider() {
    Canvas(modifier = Modifier.height(1.dp)) {
        val dashWidth = 5.dp.toPx()
        val dashGap = 3.dp.toPx()

        var startX = 0f

        while (startX < size.width) {
            drawLine(
                color = grayColor.copy(alpha = 0.3f),
                start = Offset(startX, 0f),
                end = Offset(startX + dashWidth, 0f),
                strokeWidth = 1.dp.toPx()
            )
            startX += dashWidth + dashGap
        }
    }
}