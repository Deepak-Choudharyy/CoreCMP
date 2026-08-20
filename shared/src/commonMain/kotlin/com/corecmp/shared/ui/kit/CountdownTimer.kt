package com.corecmp.shared.ui.kit

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Composable
fun CountdownTimer(
    targetEpochMs: Long,
    modifier: Modifier = Modifier,
    onFinished: (() -> Unit)? = null,
    formatter: (Duration) -> String = { formatCountdown(it) },
) {
    var remainingMs by remember(targetEpochMs) {
        mutableLongStateOf((targetEpochMs - Clock.System.now().toEpochMilliseconds()).coerceAtLeast(0L))
    }

    LaunchedEffect(targetEpochMs) {
        while (remainingMs > 0L) {
            delay(1.seconds)
            remainingMs = (targetEpochMs - Clock.System.now().toEpochMilliseconds()).coerceAtLeast(0L)
        }
        onFinished?.invoke()
    }

    Text(
        text = formatter(remainingMs.milliseconds),
        modifier = modifier,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.primary,
    )
}

private fun formatCountdown(duration: Duration): String {
    val totalSeconds = duration.inWholeSeconds
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    } else {
        "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    }
}
