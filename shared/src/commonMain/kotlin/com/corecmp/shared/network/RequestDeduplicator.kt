package com.corecmp.shared.network

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class RequestDeduplicator {
    private val mutex = Mutex()
    private val inFlight = mutableMapOf<String, Deferred<Any?>>()

    @Suppress("UNCHECKED_CAST")
    suspend fun <T> execute(
        key: String,
        block: suspend () -> T,
    ): T = coroutineScope {
        val deferred = mutex.withLock {
            inFlight.getOrPut(key) {
                async {
                    try {
                        block()
                    } finally {
                        mutex.withLock { inFlight.remove(key) }
                    }
                }
            }
        }
        deferred.await() as T
    }

    suspend fun clear() {
        mutex.withLock {
            inFlight.values.forEach { it.cancel() }
            inFlight.clear()
        }
    }
}
