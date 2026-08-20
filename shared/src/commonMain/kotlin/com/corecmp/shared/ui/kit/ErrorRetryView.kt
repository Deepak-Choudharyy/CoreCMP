package com.corecmp.shared.ui.kit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.corecmp.shared.theme.LocalCoreCmpTypography
import com.corecmp.shared.ui.CommonButton

@Composable
fun ErrorRetryView(
    message: String,
    modifier: Modifier = Modifier,
    retryLabel: String = "Retry",
    onRetry: () -> Unit,
) {
    val typography = LocalCoreCmpTypography.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = message,
            style = typography.emptySubtitle,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )
        CommonButton(
            label = retryLabel,
            onClick = onRetry,
            padding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 10.dp),
        )
    }
}
