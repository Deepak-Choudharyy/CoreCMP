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
fun SkeletonList(
    itemCount: Int = 5,
    modifier: Modifier = Modifier,
) {
    val colors = LocalCoreCmpColors.current
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        repeat(itemCount) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .shimmer(
                        shimmerColor = colors.skeletonHighlight,
                        bgColor = colors.skeletonBase,
                    ),
            )
        }
    }
}
