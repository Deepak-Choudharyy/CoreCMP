package com.corecmp.shared.network

import com.corecmp.shared.api.HttpClientProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Convenience WebSocket client wrapper around CoreCmpSocketManager.
 * Automatically logs sent and received frames and stores up to 10MB of event logs.
 */
class WebSocketClient(
    private val socketManager: CoreCmpSocketManager = CoreCmpSocketManager(client = HttpClientProvider.client)
) {

    fun connect(
        url: String,
        extraHeaders: Map<String, String> = emptyMap(),
    ): Flow<String> {
        return socketManager.connect(url, extraHeaders).map { it.data }
    }

    suspend fun send(url: String, message: String, extraHeaders: Map<String, String> = emptyMap()) {
        socketManager.send(url = url, message = message, extraHeaders = extraHeaders)
    }

    suspend fun disconnect(url: String) {
        socketManager.disconnect(url)
    }
}
