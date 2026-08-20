package com.corecmp.shared.ui.kit

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class TimelineItem(
    val title: String,
    val subtitle: String? = null,
    val isCompleted: Boolean = false,
    val isActive: Boolean = false,
)

@Composable
fun Timeline(
    items: List<TimelineItem>,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.outline,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        items.forEachIndexed { index, item ->
            val isLast = index == items.lastIndex
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val dotColor = when {
                        item.isCompleted || item.isActive -> activeColor
                        else -> inactiveColor
                    }
                    Canvas(modifier = Modifier.size(14.dp)) {
                        drawCircle(color = dotColor, radius = size.minDimension / 2f)
                        if (item.isActive && !item.isCompleted) {
                            drawCircle(
                                color = dotColor,
                                radius = size.minDimension / 2f,
                                style = Stroke(width = 2.dp.toPx()),
                            )
                        }
                    }
                    if (!isLast) {
                        Canvas(
                            modifier = Modifier
                                .width(2.dp)
                                .height(36.dp),
                        ) {
                            drawLine(
                                color = if (item.isCompleted) activeColor else inactiveColor,
                                start = Offset(size.width / 2f, 0f),
                                end = Offset(size.width / 2f, size.height),
                                strokeWidth = 2.dp.toPx(),
                            )
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = if (isLast) 0.dp else 16.dp),
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (item.isActive) FontWeight.SemiBold else FontWeight.Normal,
                        ),
                    )
                    item.subtitle?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
