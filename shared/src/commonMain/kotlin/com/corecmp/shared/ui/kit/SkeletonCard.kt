package com.corecmp.shared.ui.kit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.corecmp.shared.theme.LocalCoreCmpColors
import com.corecmp.shared.ui.shimmer

@Composable
fun SkeletonCard(
    modifier: Modifier = Modifier,
    lines: Int = 3,
    lineHeight: Int = 14,
) {
    val colors = LocalCoreCmpColors.current
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceMuted),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .height(lineHeight.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer(
                        shimmerColor = colors.skeletonHighlight,
                        bgColor = colors.skeletonBase,
                    ),
            )
            repeat(lines) { index ->
                Spacer(modifier = Modifier.height(10.dp))
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth(if (index == lines - 1) 0.7f else 1f)
                        .height(lineHeight.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer(
                            shimmerColor = colors.skeletonHighlight,
                            bgColor = colors.skeletonBase,
                        ),
                )
            }
        }
    }
}
