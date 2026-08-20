package com.corecmp.shared.ui.kit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.corecmp.shared.theme.LocalCoreCmpColors

@Composable
fun BadgeDot(
    count: Int,
    modifier: Modifier = Modifier,
    maxDisplay: Int = 99,
    backgroundColor: Color = LocalCoreCmpColors.current.error,
    contentColor: Color = Color.White,
) {
    if (count <= 0) return
    val label = if (count > maxDisplay) "$maxDisplay+" else count.toString()
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 18.dp, minHeight = 18.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
        )
    }
}
