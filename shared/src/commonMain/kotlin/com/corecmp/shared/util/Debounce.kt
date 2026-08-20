package com.corecmp.shared.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlin.time.Clock

class RateLimiter(private val intervalMs: Long) {
    private var lastTime = 0L

    fun tryAcquire(): Boolean {
        val now = Clock.System.now().toEpochMilliseconds()
        if (now - lastTime < intervalMs) return false
        lastTime = now
        return true
    }
}

@Composable
fun rememberDebouncedClick(
    debounceMs: Long = 500L,
    onClick: () -> Unit,
): () -> Unit {
    var lastClick by remember { mutableLongStateOf(0L) }
    return remember(onClick, debounceMs) {
        {
            val now = Clock.System.now().toEpochMilliseconds()
            if (now - lastClick >= debounceMs) {
                lastClick = now
                onClick()
            }
        }
    }
}

fun debounce(
    debounceMs: Long = 500L,
    block: () -> Unit,
): () -> Unit {
    var last = 0L
    return {
        val now = Clock.System.now().toEpochMilliseconds()
        if (now - last >= debounceMs) {
            last = now
            block()
        }
    }
}
