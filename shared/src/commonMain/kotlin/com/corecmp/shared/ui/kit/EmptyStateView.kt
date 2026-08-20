package com.corecmp.shared.ui.kit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.corecmp.shared.theme.LocalCoreCmpTypography
import com.corecmp.shared.ui.CommonButton

@Composable
fun EmptyStateView(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val typography = LocalCoreCmpTypography.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        icon?.let {
            androidx.compose.material3.Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = title,
            style = typography.emptyTitle,
            textAlign = TextAlign.Center,
        )
        subtitle?.let {
            Text(
                text = it,
                style = typography.emptySubtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        if (actionLabel != null && onAction != null) {
            CommonButton(
                label = actionLabel,
                onClick = onAction,
                padding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 10.dp),
            )
        }
    }
}
