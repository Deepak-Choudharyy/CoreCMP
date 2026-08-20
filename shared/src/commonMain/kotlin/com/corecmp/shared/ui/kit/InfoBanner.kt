package com.corecmp.shared.ui.kit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.corecmp.shared.theme.LocalCoreCmpColors
import com.corecmp.shared.theme.LocalCoreCmpTypography

enum class InfoBannerVariant {
    INFO, WARNING, ERROR,
}

@Composable
fun InfoBanner(
    message: String,
    modifier: Modifier = Modifier,
    variant: InfoBannerVariant = InfoBannerVariant.INFO,
    onDismiss: (() -> Unit)? = null,
) {
    val colors = LocalCoreCmpColors.current
    val typography = LocalCoreCmpTypography.current
    val (bg, fg, icon) = when (variant) {
        InfoBannerVariant.INFO -> Triple(colors.info.copy(alpha = 0.12f), colors.info, Icons.Default.Info)
        InfoBannerVariant.WARNING -> Triple(colors.warning.copy(alpha = 0.18f), colors.onWarning, Icons.Default.Warning)
        InfoBannerVariant.ERROR -> Triple(colors.error.copy(alpha = 0.12f), colors.error, Icons.Default.Warning)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = fg,
            modifier = Modifier.padding(end = 8.dp),
        )
        Text(
            text = message,
            modifier = Modifier.weight(1f),
            style = typography.banner.copy(color = MaterialTheme.colorScheme.onSurface),
        )
        onDismiss?.let {
            IconButton(onClick = it) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
