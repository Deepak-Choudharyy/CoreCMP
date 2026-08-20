package com.corecmp.shared.state

import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

suspend fun <T> pollUntil(
    interval: Duration = 1_000.milliseconds,
    timeout: Duration = 30_000.milliseconds,
    predicate: suspend (T) -> Boolean,
    block: suspend () -> T,
): T? {
    val deadline = kotlin.time.Clock.System.now() + timeout
    var last: T? = null
    while (kotlin.time.Clock.System.now() < deadline) {
        last = block()
        if (predicate(last)) return last
        delay(interval)
    }
    return null
}

suspend fun <T> pollEvery(
    interval: Duration = 1_000.milliseconds,
    times: Int = Int.MAX_VALUE,
    block: suspend () -> T,
): List<T> {
    val results = mutableListOf<T>()
    repeat(times) {
        results += block()
        if (it < times - 1) delay(interval)
    }
    return results
}
