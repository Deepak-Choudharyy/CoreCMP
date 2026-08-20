package com.corecmp.shared.storage

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlin.time.Clock
import kotlin.time.Duration

@Serializable
data class ApiCacheItem(
    val key: String,
    val url: String,
    val timestamp: String,
    val timestampEpochMs: Long,
    val requestBody: JsonElement? = null,
    val responseBody: JsonElement? = null,
    val sizeBytes: Long = 0L,
    val expiresAtEpochMs: Long? = null
)

@Serializable
data class ApiCacheContainer(
    val totalSizeBytes: Long = 0L,
    val totalEntries: Int = 0,
    val entries: List<ApiCacheItem> = emptyList()
)

/**
 * Single-file 10MB text API cache for Kotlin Multiplatform / Android.
 * Saved in a single formatted, pretty-printed JSON file: api_response_cache.json.
 * Formats request and response payloads as clean structured JSON objects.
 * Evicts oldest items (lowest timestamp) when total cache size exceeds 10MB.
 */
class ApiCacheStorage(
    private val json: Json = Json { ignoreUnknownKeys = true },
    private val defaultTtl: Duration? = null,
    val maxCacheSizeBytes: Long = 10 * 1024 * 1024L // 10 MB Limit
) {
    private val singleCacheFilePath = "${CacheFileIO.cacheDirectory()}/api_response_cache.json"

    private val prettyJson = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun get(key: String): ApiCacheItem? {
        val container = loadContainer()
        val entry = container.entries.firstOrNull { it.key == key } ?: return null
        if (entry.expiresAtEpochMs != null && Clock.System.now().toEpochMilliseconds() > entry.expiresAtEpochMs) {
            invalidate(key)
            return null
        }
        return entry
    }

    fun getResponseBody(key: String): String? {
        val entry = get(key) ?: return null
        val element = entry.responseBody ?: return null
        return when (element) {
            is JsonPrimitive -> if (element.isString) element.content else element.toString()
            else -> element.toString()
        }
    }

    fun put(
        key: String,
        url: String,
        requestBody: String? = null,
        responseBody: String,
        ttl: Duration? = defaultTtl
    ) {
        val nowMs = Clock.System.now().toEpochMilliseconds()
        val expiresAt = ttl?.let { nowMs + it.inWholeMilliseconds }
        val timestampStr = formatTimestamp(nowMs)

        val reqElement = parseToJsonElement(requestBody)
        val respElement = parseToJsonElement(responseBody)

        val contentBytes = (url + (requestBody ?: "") + responseBody).encodeToByteArray().size.toLong()
        if (contentBytes > maxCacheSizeBytes) return

        val newEntry = ApiCacheItem(
            key = key,
            url = url,
            timestamp = timestampStr,
            timestampEpochMs = nowMs,
            requestBody = reqElement,
            responseBody = respElement,
            sizeBytes = contentBytes,
            expiresAtEpochMs = expiresAt
        )

        val container = loadContainer()
        val currentEntries = container.entries.filterNot { it.key == key }.toMutableList()

        var currentTotalSize = currentEntries.sumOf { it.sizeBytes }

        while (currentTotalSize + contentBytes > maxCacheSizeBytes && currentEntries.isNotEmpty()) {
            currentEntries.sortBy { it.timestampEpochMs }
            val oldest = currentEntries.removeAt(0)
            currentTotalSize -= oldest.sizeBytes
        }

        currentEntries.add(newEntry)
        currentEntries.sortByDescending { it.timestampEpochMs }

        val newContainer = ApiCacheContainer(
            totalSizeBytes = currentEntries.sumOf { it.sizeBytes },
            totalEntries = currentEntries.size,
            entries = currentEntries
        )

        saveContainer(newContainer)
    }

    fun invalidate(key: String) {
        val container = loadContainer()
        val filtered = container.entries.filterNot { it.key == key }
        if (filtered.size != container.entries.size) {
            val updated = ApiCacheContainer(
                totalSizeBytes = filtered.sumOf { it.sizeBytes },
                totalEntries = filtered.size,
                entries = filtered
            )
            saveContainer(updated)
        }
    }

    fun clearAll() {
        CacheFileIO.delete(singleCacheFilePath)
    }

    fun getTotalCacheSizeBytes(): Long {
        return loadContainer().totalSizeBytes
    }

    fun getCacheCount(): Int {
        return loadContainer().totalEntries
    }

    private fun parseToJsonElement(rawText: String?): JsonElement? {
        if (rawText.isNullOrBlank()) return null
        val trimmed = rawText.trim()
        if ((trimmed.startsWith("{") && trimmed.endsWith("}")) || (trimmed.startsWith("[") && trimmed.endsWith("]"))) {
            return runCatching { prettyJson.parseToJsonElement(trimmed) }.getOrNull() ?: JsonPrimitive(rawText)
        }
        return JsonPrimitive(rawText)
    }

    private fun loadContainer(): ApiCacheContainer {
        val raw = CacheFileIO.read(singleCacheFilePath) ?: return ApiCacheContainer()
        return runCatching { prettyJson.decodeFromString<ApiCacheContainer>(raw) }.getOrDefault(ApiCacheContainer())
    }

    private fun saveContainer(container: ApiCacheContainer) {
        val raw = prettyJson.encodeToString(container)
        CacheFileIO.write(singleCacheFilePath, raw)
    }

    private fun formatTimestamp(epochMs: Long): String {
        return try {
            kotlin.time.Instant.fromEpochMilliseconds(epochMs).toString()
        } catch (_: Exception) {
            epochMs.toString()
        }
    }
}
