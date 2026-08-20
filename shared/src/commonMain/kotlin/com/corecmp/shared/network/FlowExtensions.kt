package com.corecmp.shared.network

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import kotlin.time.Clock

fun <T> Flow<T>.throttleFlow(windowMs: Long): Flow<T> = flow {
    var lastEmitAt = 0L
    collect { value ->
        val now = Clock.System.now().toEpochMilliseconds()
        if (now - lastEmitAt >= windowMs) {
            lastEmitAt = now
            emit(value)
        }
    }
}

@OptIn(FlowPreview::class)
fun <T> Flow<T>.debounceFlow(timeoutMs: Long): Flow<T> = debounce(timeoutMs)
