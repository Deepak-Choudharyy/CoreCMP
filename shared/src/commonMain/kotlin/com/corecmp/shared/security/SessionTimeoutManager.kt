package com.corecmp.shared.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Clock

enum class SessionTimeoutDuration(val millis: Long) {
    FIVE_MINUTES(5 * 60 * 1000L),
    FIFTEEN_MINUTES(15 * 60 * 1000L),
    THIRTY_MINUTES(30 * 60 * 1000L),
}

class SessionTimeoutManager(
    private val duration: SessionTimeoutDuration = SessionTimeoutDuration.FIFTEEN_MINUTES,
    private val onTimeout: () -> Unit = {},
) {
    private var lastActiveAtMs: Long = Clock.System.now().toEpochMilliseconds()
    private val _isTimedOut = MutableStateFlow(false)
    val isTimedOut: StateFlow<Boolean> = _isTimedOut.asStateFlow()

    fun touch() {
        lastActiveAtMs = Clock.System.now().toEpochMilliseconds()
        if (_isTimedOut.value) {
            _isTimedOut.value = false
        }
    }

    fun reset() {
        lastActiveAtMs = Clock.System.now().toEpochMilliseconds()
        _isTimedOut.value = false
    }

    fun elapsedMillis(): Long =
        Clock.System.now().toEpochMilliseconds() - lastActiveAtMs

    fun remainingMillis(): Long =
        (duration.millis - elapsedMillis()).coerceAtLeast(0L)

    fun checkTimeout(): Boolean {
        val timedOut = elapsedMillis() >= duration.millis
        if (timedOut && !_isTimedOut.value) {
            _isTimedOut.value = true
            onTimeout()
        }
        return timedOut
    }
}
