package com.corecmp.shared.network

import com.corecmp.shared.api.HttpClientProvider
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class SseEvent(
    val event: String? = null,
    val data: String = "",
    val id: String? = null,
)

class SseClient(private val client: io.ktor.client.HttpClient = HttpClientProvider.client) {

    fun connect(url: String, extraHeaders: Map<String, String> = emptyMap()): Flow<SseEvent> = flow {
        client.prepareGet(url) {
            extraHeaders.forEach { (key, value) -> headers.append(key, value) }
            headers.append("Accept", "text/event-stream")
            headers.append("Cache-Control", "no-cache")
        }.execute { response ->
            val channel = response.bodyAsChannel()
            var eventName: String? = null
            var eventId: String? = null
            val dataLines = mutableListOf<String>()
            while (!channel.isClosedForRead) {
                val line = channel.readUTF8Line() ?: break
                when {
                    line.isEmpty() -> {
                        if (dataLines.isNotEmpty()) {
                            emit(
                                SseEvent(
                                    event = eventName,
                                    data = dataLines.joinToString("\n"),
                                    id = eventId,
                                ),
                            )
                            dataLines.clear()
                            eventName = null
                        }
                    }
                    line.startsWith("data:") -> dataLines += line.removePrefix("data:").trimStart()
                    line.startsWith("event:") -> eventName = line.removePrefix("event:").trimStart()
                    line.startsWith("id:") -> eventId = line.removePrefix("id:").trimStart()
                }
            }
        }
    }
}
