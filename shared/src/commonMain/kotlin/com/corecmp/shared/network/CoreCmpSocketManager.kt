package com.corecmp.shared.network

import com.corecmp.shared.api.CoreCmpLogger
import com.corecmp.shared.api.HttpClientProvider
import com.corecmp.shared.storage.SocketLogItem
import com.corecmp.shared.storage.SocketLogStorage
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import io.ktor.websocket.readBytes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.time.Clock

@Serializable
data class SocketMessage(
    val url: String,
    val event: String,
    val data: String,
    val direction: String, // "SENT", "RECEIVED", "CONNECT", "DISCONNECT", "ERROR"
    val timestampMs: Long = Clock.System.now().toEpochMilliseconds()
)

/**
 * Socket Manager for Kotlin Multiplatform applications.
 * Manages persistent WebSocket connections, handles auto-reconnection, event sending & receiving,
 * displays real-time console logs, emits real-time events via SharedFlow / Flow streams,
 * and maintains a 10MB persistent log file.
 */
class CoreCmpSocketManager(
    private val client: HttpClient = HttpClientProvider.client,
    val logStorage: SocketLogStorage = SocketLogStorage()
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @PublishedApi
    internal val json: Json = Json { ignoreUnknownKeys = true; isLenient = true; encodeDefaults = true }

    private val activeSessions = mutableMapOf<String, DefaultClientWebSocketSession>()
    private val messageFlows = mutableMapOf<String, MutableSharedFlow<SocketMessage>>()
    private val globalSharedFlow = MutableSharedFlow<SocketMessage>(extraBufferCapacity = 256)
    private val manualDisconnects = mutableSetOf<String>()

    /**
     * Observe all socket events across all connected URLs.
     */
    fun observeAll(): SharedFlow<SocketMessage> = globalSharedFlow.asSharedFlow()

    /**
     * Observe incoming and outgoing socket events for a specific socket URL.
     */
    fun observe(url: String): SharedFlow<SocketMessage> {
        return getOrCreateFlow(url).asSharedFlow()
    }

    /**
     * Observe specific named socket events (e.g. "chat_message", "profile_sync_event") for a socket URL.
     */
    fun observeEvent(url: String, eventName: String): Flow<SocketMessage> {
        return observe(url).filter { it.event == eventName }
    }

    /**
     * Connect to a WebSocket URL and observe incoming messages.
     * Stays connected continuously until explicit disconnect(url) is called.
     * Automatically reconnects if network drops or connection drops unexpectedly when autoReconnect = true.
     */
    fun connect(
        url: String,
        extraHeaders: Map<String, String> = emptyMap(),
        autoReconnect: Boolean = true,
        reconnectDelayMs: Long = 3000L
    ): Flow<SocketMessage> = flow {
        manualDisconnects.remove(url)
        val sharedFlow = getOrCreateFlow(url)

        var keepAlive = true
        while (keepAlive && !manualDisconnects.contains(url) && currentCoroutineContext().isActive) {
            // Log connection start
            CoreCmpLogger.logSocketEvent(
                url = url,
                event = "CONNECT",
                direction = "CONNECT",
                requestData = if (extraHeaders.isNotEmpty()) json.encodeToString(extraHeaders) else null
            )
            logStorage.logEvent(
                url = url,
                event = "CONNECT",
                direction = "CONNECT",
                requestData = if (extraHeaders.isNotEmpty()) json.encodeToString(extraHeaders) else null
            )

            val connectMsg = SocketMessage(
                url = url,
                event = "CONNECT",
                data = "Connected to $url",
                direction = "CONNECT"
            )
            sharedFlow.emit(connectMsg)
            globalSharedFlow.emit(connectMsg)

            try {
                client.webSocket(urlString = url, request = {
                    extraHeaders.forEach { (key, value) -> headers.append(key, value) }
                }) {
                    activeSessions[url] = this
                    try {
                        for (frame in incoming) {
                            val rawText = when (frame) {
                                is Frame.Text -> frame.readText()
                                is Frame.Binary -> frame.readBytes().decodeToString()
                                else -> null
                            }

                            if (rawText != null) {
                                val eventName = extractEventName(rawText)

                                val message = SocketMessage(
                                    url = url,
                                    event = eventName,
                                    data = rawText,
                                    direction = "RECEIVED"
                                )

                                // Display console log & persist to 10MB log storage
                                CoreCmpLogger.logSocketEvent(
                                    url = url,
                                    event = eventName,
                                    direction = "RECEIVED",
                                    responseData = rawText
                                )
                                logStorage.logEvent(
                                    url = url,
                                    event = eventName,
                                    direction = "RECEIVED",
                                    responseData = rawText
                                )

                                sharedFlow.emit(message)
                                globalSharedFlow.emit(message)
                                emit(message)
                            }
                        }
                    } finally {
                        activeSessions.remove(url)
                        CoreCmpLogger.logSocketEvent(
                            url = url,
                            event = "DISCONNECT",
                            direction = "DISCONNECT"
                        )
                        logStorage.logEvent(
                            url = url,
                            event = "DISCONNECT",
                            direction = "DISCONNECT"
                        )

                        val disconnectMsg = SocketMessage(
                            url = url,
                            event = "DISCONNECT",
                            data = "Disconnected from $url",
                            direction = "DISCONNECT"
                        )
                        sharedFlow.emit(disconnectMsg)
                        globalSharedFlow.emit(disconnectMsg)
                    }
                }
            } catch (e: Exception) {
                CoreCmpLogger.logSocketEvent(
                    url = url,
                    event = "ERROR",
                    direction = "ERROR",
                    error = e.message
                )
                logStorage.logEvent(
                    url = url,
                    event = "ERROR",
                    direction = "ERROR",
                    responseData = e.message
                )

                val errorMsg = SocketMessage(
                    url = url,
                    event = "ERROR",
                    data = e.message ?: "Socket error",
                    direction = "ERROR"
                )
                sharedFlow.emit(errorMsg)
                globalSharedFlow.emit(errorMsg)
            }

            if (manualDisconnects.contains(url) || !autoReconnect) {
                keepAlive = false
            } else {
                CoreCmpLogger.d("🔄 Socket disconnected unexpectedly. Reconnecting in ${reconnectDelayMs}ms...")
                delay(reconnectDelayMs)
            }
        }
    }

    /**
     * Send a raw string or frame to a WebSocket URL.
     * Logs the sent event to console & 10MB log storage and emits to event flows.
     */
    suspend fun send(
        url: String,
        message: String,
        eventName: String? = null,
        extraHeaders: Map<String, String> = emptyMap()
    ) {
        val event = eventName ?: extractEventName(message)

        // Log sent event
        CoreCmpLogger.logSocketEvent(
            url = url,
            event = event,
            direction = "SENT",
            requestData = message
        )
        logStorage.logEvent(
            url = url,
            event = event,
            direction = "SENT",
            requestData = message
        )

        val socketMsg = SocketMessage(
            url = url,
            event = event,
            data = message,
            direction = "SENT"
        )
        val sharedFlow = getOrCreateFlow(url)
        sharedFlow.emit(socketMsg)
        globalSharedFlow.emit(socketMsg)

        val session = getActiveSession(url)
        if (session != null && session.isActive) {
            session.send(Frame.Text(message))
        } else {
            // One-off send connection if no persistent connection is active
            client.webSocket(urlString = url, request = {
                extraHeaders.forEach { (key, value) -> headers.append(key, value) }
            }) {
                send(Frame.Text(message))
                for (frame in incoming) {
                    val rawText = when (frame) {
                        is Frame.Text -> frame.readText()
                        is Frame.Binary -> frame.readBytes().decodeToString()
                        else -> null
                    }
                    if (rawText != null) {
                        val echoEvent = extractEventName(rawText)
                        val echoMsg = SocketMessage(
                            url = url,
                            event = echoEvent,
                            data = rawText,
                            direction = "RECEIVED"
                        )
                        CoreCmpLogger.logSocketEvent(url = url, event = echoEvent, direction = "RECEIVED", responseData = rawText)
                        logStorage.logEvent(url = url, event = echoEvent, direction = "RECEIVED", responseData = rawText)
                        sharedFlow.emit(echoMsg)
                        globalSharedFlow.emit(echoMsg)
                        break
                    }
                }
            }
        }
    }

    /**
     * Send a structured event payload (serializable object, Map, List, or primitive) to a WebSocket URL.
     */
    suspend fun sendEvent(
        url: String,
        event: String,
        payload: Any,
        extraHeaders: Map<String, String> = emptyMap()
    ) {
        val payloadJson = encodePayloadToJson(payload)
        val eventObject = if (payloadJson.startsWith("{") && payloadJson.endsWith("}")) {
            "{\"event\":\"$event\"," + payloadJson.substring(1)
        } else {
            "{\"event\":\"$event\",\"data\":$payloadJson}"
        }

        send(url = url, message = eventObject, eventName = event, extraHeaders = extraHeaders)
    }

    /**
     * Safely converts any object, Map, List, String, or primitive into valid JSON string.
     */
    fun encodePayloadToJson(payload: Any?): String {
        if (payload == null) return "null"
        return when (payload) {
            is String -> {
                val trimmed = payload.trim()
                if ((trimmed.startsWith("{") && trimmed.endsWith("}")) || (trimmed.startsWith("[") && trimmed.endsWith("]"))) {
                    trimmed
                } else {
                    json.encodeToString(payload)
                }
            }
            is Number, is Boolean -> payload.toString()
            is Map<*, *> -> {
                val entries = payload.entries.joinToString(",") { (k, v) ->
                    "\"${k.toString()}\":${encodePayloadToJson(v)}"
                }
                "{$entries}"
            }
            is List<*> -> {
                val items = payload.joinToString(",") { encodePayloadToJson(it) }
                "[$items]"
            }
            else -> {
                runCatching { json.encodeToString(payload) }.getOrElse { json.encodeToString(payload.toString()) }
            }
        }
    }

    /**
     * Disconnect an active WebSocket URL session manually.
     * Prevents automatic reconnection for this URL.
     */
    suspend fun disconnect(url: String) {
        manualDisconnects.add(url)
        val session = activeSessions.remove(url)
        if (session != null) {
            try {
                session.close(CloseReason(CloseReason.Codes.NORMAL, "Disconnected by client"))
            } catch (_: Exception) {}
        }
        CoreCmpLogger.logSocketEvent(
            url = url,
            event = "DISCONNECT",
            direction = "DISCONNECT"
        )
        logStorage.logEvent(
            url = url,
            event = "DISCONNECT",
            direction = "DISCONNECT"
        )
    }

    /**
     * Disconnect all active socket sessions manually.
     */
    suspend fun disconnectAll() {
        val urls = activeSessions.keys.toList()
        urls.forEach { disconnect(it) }
    }

    /**
     * Check if a socket URL currently has an active connected session.
     */
    fun isConnected(url: String): Boolean {
        val session = activeSessions[url]
        return session != null && session.isActive
    }

    /**
     * Get all recorded socket log items from the 10MB persistent store.
     */
    fun getLogs(): List<SocketLogItem> = logStorage.getLogs()

    /**
     * Get recorded socket logs for a specific socket URL.
     */
    fun getLogsForUrl(url: String): List<SocketLogItem> = logStorage.getLogsForUrl(url)

    /**
     * Clear all recorded socket logs.
     */
    fun clearLogs() = logStorage.clearAll()

    /**
     * Export all socket logs formatted as a plain text string.
     */
    fun exportLogsAsText(): String = logStorage.exportLogsAsText()

    private suspend fun getActiveSession(url: String, timeoutMs: Long = 3000): DefaultClientWebSocketSession? {
        val startTime = Clock.System.now().toEpochMilliseconds()
        while (Clock.System.now().toEpochMilliseconds() - startTime < timeoutMs) {
            val session = activeSessions[url]
            if (session != null && session.isActive) return session
            delay(100)
        }
        return activeSessions[url]
    }

    private fun getOrCreateFlow(url: String): MutableSharedFlow<SocketMessage> {
        return messageFlows.getOrPut(url) { MutableSharedFlow(extraBufferCapacity = 128) }
    }

    private fun extractEventName(raw: String): String {
        if (!raw.startsWith("{") || !raw.endsWith("}")) return "MESSAGE"
        return runCatching {
            val element = json.parseToJsonElement(raw).jsonObject
            val eventVal = element["event"]?.jsonPrimitive?.content
                ?: element["type"]?.jsonPrimitive?.content
                ?: element["action"]?.jsonPrimitive?.content
            eventVal ?: "MESSAGE"
        }.getOrDefault("MESSAGE")
    }
}
