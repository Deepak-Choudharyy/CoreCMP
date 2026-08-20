package com.corecmp.shared.network

import com.corecmp.shared.storage.ApiCacheStorage
import com.corecmp.shared.storage.ApiCacheItem
import kotlin.time.Duration.Companion.milliseconds

/**
 * High-level API Response Cache wrapping ApiCacheStorage.
 * Stores request URL, request body, response text, and timestamp in single file up to 10MB.
 */
class ApiResponseCache(
    private val delegate: ApiCacheStorage = ApiCacheStorage()
) {
    fun get(key: String): String? {
        return delegate.getResponseBody(key)
    }

    fun getEntry(key: String): ApiCacheItem? {
        return delegate.get(key)
    }

    fun put(key: String, body: String, ttlMs: Long? = null, url: String = key, requestBody: String? = null) {
        val duration = ttlMs?.milliseconds
        delegate.put(
            key = key,
            url = url,
            requestBody = requestBody,
            responseBody = body,
            ttl = duration
        )
    }

    fun evict(key: String) {
        delegate.invalidate(key)
    }

    fun clearAll() {
        delegate.clearAll()
    }

    fun getTotalSizeBytes(): Long = delegate.getTotalCacheSizeBytes()
}
