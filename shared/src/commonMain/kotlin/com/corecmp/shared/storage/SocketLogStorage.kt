package com.corecmp.shared.storage

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlin.time.Clock

@Serializable
data class SocketLogItem(
    val id: String,
    val url: String,
    val event: String,
    val direction: String, // "SENT", "RECEIVED", "CONNECT", "DISCONNECT", "ERROR"
    val timestamp: String,
    val timestampEpochMs: Long,
    val requestData: JsonElement? = null,
    val responseData: JsonElement? = null,
    val sizeBytes: Long = 0L
)

@Serializable
data class SocketLogContainer(
    val totalSizeBytes: Long = 0L,
    val totalEntries: Int = 0,
    val entries: List<SocketLogItem> = emptyList()
)

/**
 * Single-file 10MB text/JSON Socket log cache for Kotlin Multiplatform.
 * Stores socket events sent from or received by the device with timestamp, URL, event name, request (data sent), response (data received).
 * Evicts oldest items (lowest timestamp) when total cache size exceeds 10MB limit.
 */
class SocketLogStorage(
    private val json: Json = Json { ignoreUnknownKeys = true },
    val maxCacheSizeBytes: Long = 10 * 1024 * 1024L // 10 MB Limit
) {
    private val singleCacheFilePath = "${CacheFileIO.cacheDirectory()}/socket_events_cache.json"

    private val prettyJson = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun logEvent(
        url: String,
        event: String,
        direction: String,
        requestData: String? = null,
        responseData: String? = null
    ): SocketLogItem? {
        val nowMs = Clock.System.now().toEpochMilliseconds()
        val timestampStr = formatTimestamp(nowMs)

        val reqElement = parseToJsonElement(requestData)
        val respElement = parseToJsonElement(responseData)

        val contentBytes = (url + event + direction + (requestData ?: "") + (responseData ?: "")).encodeToByteArray().size.toLong()
        if (contentBytes > maxCacheSizeBytes) return null

        val id = "${nowMs}_${(1000..9999).random()}"

        val newEntry = SocketLogItem(
            id = id,
            url = url,
            event = event,
            direction = direction,
            timestamp = timestampStr,
            timestampEpochMs = nowMs,
            requestData = reqElement,
            responseData = respElement,
            sizeBytes = contentBytes
        )

        val container = loadContainer()
        val currentEntries = container.entries.toMutableList()

        var currentTotalSize = currentEntries.sumOf { it.sizeBytes }

        while (currentTotalSize + contentBytes > maxCacheSizeBytes && currentEntries.isNotEmpty()) {
            currentEntries.sortBy { it.timestampEpochMs }
            val oldest = currentEntries.removeAt(0)
            currentTotalSize -= oldest.sizeBytes
        }

        currentEntries.add(newEntry)
        currentEntries.sortByDescending { it.timestampEpochMs }

        val newContainer = SocketLogContainer(
            totalSizeBytes = currentEntries.sumOf { it.sizeBytes },
            totalEntries = currentEntries.size,
            entries = currentEntries
        )

        saveContainer(newContainer)
        return newEntry
    }

    fun getLogs(): List<SocketLogItem> {
        return loadContainer().entries
    }

    fun getLogsForUrl(url: String): List<SocketLogItem> {
        return loadContainer().entries.filter { it.url == url }
    }

    fun clearAll() {
        CacheFileIO.delete(singleCacheFilePath)
    }

    fun getTotalLogSizeBytes(): Long {
        return loadContainer().totalSizeBytes
    }

    fun getLogCount(): Int {
        return loadContainer().totalEntries
    }

    /**
     * Formats stored logs into a human-readable plain text format suitable for viewing or exporting as a .txt file.
     */
    fun exportLogsAsText(): String {
        val container = loadContainer()
        if (container.entries.isEmpty()) return "No socket logs recorded."

        val sb = StringBuilder()
        sb.appendLine("==========================================================================")
        sb.appendLine("                        CORECMP SOCKET LOG REPORT                         ")
        sb.appendLine("Total Entries: ${container.totalEntries} | Total Size: ${container.totalSizeBytes} Bytes")
        sb.appendLine("==========================================================================")
        sb.appendLine()

        container.entries.sortedBy { it.timestampEpochMs }.forEachIndexed { index, item ->
            sb.appendLine("[#${index + 1}] TIME: ${item.timestamp}")
            sb.appendLine("URL       : ${item.url}")
            sb.appendLine("EVENT     : ${item.event}")
            sb.appendLine("DIRECTION : ${item.direction}")
            if (item.requestData != null) {
                sb.appendLine("REQUEST   : ${item.requestData}")
            }
            if (item.responseData != null) {
                sb.appendLine("RESPONSE  : ${item.responseData}")
            }
            sb.appendLine("--------------------------------------------------------------------------")
        }

        return sb.toString()
    }

    private fun parseToJsonElement(rawText: String?): JsonElement? {
        if (rawText.isNullOrBlank()) return null
        val trimmed = rawText.trim()
        if ((trimmed.startsWith("{") && trimmed.endsWith("}")) || (trimmed.startsWith("[") && trimmed.endsWith("]"))) {
            return runCatching { prettyJson.parseToJsonElement(trimmed) }.getOrNull() ?: JsonPrimitive(rawText)
        }
        return JsonPrimitive(rawText)
    }

    private fun loadContainer(): SocketLogContainer {
        val raw = CacheFileIO.read(singleCacheFilePath) ?: return SocketLogContainer()
        return runCatching { prettyJson.decodeFromString<SocketLogContainer>(raw) }.getOrDefault(SocketLogContainer())
    }

    private fun saveContainer(container: SocketLogContainer) {
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
