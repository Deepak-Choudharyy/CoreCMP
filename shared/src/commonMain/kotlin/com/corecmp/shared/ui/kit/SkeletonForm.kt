package com.corecmp.shared.ui.kit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.corecmp.shared.theme.LocalCoreCmpColors
import com.corecmp.shared.ui.shimmer

@Composable
fun SkeletonForm(
    fieldCount: Int = 4,
    modifier: Modifier = Modifier,
) {
    val colors = LocalCoreCmpColors.current
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        repeat(fieldCount) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth(0.35f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer(
                        shimmerColor = colors.skeletonHighlight,
                        bgColor = colors.skeletonBase,
                    ),
            )
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .shimmer(
                        shimmerColor = colors.skeletonHighlight,
                        bgColor = colors.skeletonBase,
                    ),
            )
        }
    }
}
