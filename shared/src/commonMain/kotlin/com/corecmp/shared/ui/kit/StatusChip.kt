package com.corecmp.shared.ui.kit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.corecmp.shared.theme.LocalCoreCmpColors
import com.corecmp.shared.theme.LocalCoreCmpTypography

enum class StatusChipVariant {
    NEUTRAL, SUCCESS, WARNING, ERROR, INFO,
}

@Composable
fun StatusChip(
    label: String,
    modifier: Modifier = Modifier,
    variant: StatusChipVariant = StatusChipVariant.NEUTRAL,
    backgroundColor: Color? = null,
    contentColor: Color? = null,
) {
    val colors = LocalCoreCmpColors.current
    val typography = LocalCoreCmpTypography.current
    val (bg, fg) = when (variant) {
        StatusChipVariant.SUCCESS -> colors.success to colors.onSuccess
        StatusChipVariant.WARNING -> colors.warning to colors.onWarning
        StatusChipVariant.ERROR -> colors.error to colors.onError
        StatusChipVariant.INFO -> colors.info to colors.onInfo
        StatusChipVariant.NEUTRAL -> colors.chipBackground to MaterialTheme.colorScheme.onSurface
    }

    Text(
        text = label,
        modifier = modifier
            .background(backgroundColor ?: bg, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        style = typography.chip.copy(
            color = contentColor ?: fg,
            fontWeight = FontWeight.Medium,
        ),
    )
}
