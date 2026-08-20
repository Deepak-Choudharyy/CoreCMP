package com.corecmp.shared.ui.kit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.corecmp.shared.accessibility.minTouchTarget

@Composable
fun RatingBar(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    maxStars: Int = 5,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.outline,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(maxStars) { index ->
            val starIndex = index + 1
            val filled = starIndex <= rating
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "$starIndex stars",
                tint = if (filled) activeColor else inactiveColor.copy(alpha = 0.35f),
                modifier = Modifier
                    .size(28.dp)
                    .minTouchTarget()
                    .clickable { onRatingChange(starIndex) },
            )
        }
    }
}
