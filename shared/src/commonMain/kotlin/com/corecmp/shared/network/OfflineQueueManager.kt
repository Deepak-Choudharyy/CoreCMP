package com.corecmp.shared.network

import com.corecmp.shared.CoreCmp
import com.corecmp.shared.api.json
import com.corecmp.shared.db.CoreCmpDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.request.parameter
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.ContentType
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import com.corecmp.shared.api.appendFile
import com.corecmp.shared.api.applyDefaults
import com.corecmp.shared.api.buildUrl
import com.corecmp.shared.api.FilePart
import com.corecmp.shared.picker.PickedFile

@Serializable
data class QueuedFilePart(
    val name: String,
    val fileName: String?,
    val mimeType: String?,
    val bytesBase64: String
)

@Serializable
data class ApiQueuePayload(
    val bodyString: String? = null,
    val files: List<QueuedFilePart> = emptyList(),
    val queryParams: Map<String, String> = emptyMap(),
    val bodyType: String = "JSON"
)

@Serializable
data class QueuedRequest(
    val id: String = "",
    val tag: String,
    val payloadJson: String, // Will contain serialized ApiQueuePayload
    val endpoint: String = "",
    val method: String = "",
    val headers: Map<String, String> = emptyMap(),
    val createdAt: Long = kotlin.time.Clock.System.now().toEpochMilliseconds(),
    val dbId: Long = 0L
)

class OfflineQueueManager internal constructor() : KoinComponent {
    private val database: CoreCmpDatabase by inject()
    private val dbQuery = database.databaseQueries

    private val _flushSignal = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val flushSignal: SharedFlow<Unit> = _flushSignal.asSharedFlow()

    fun enqueue(request: QueuedRequest) {
        val headersStr = json.encodeToString(request.headers)
        dbQuery.insertApiQueue(
            endpoint = request.endpoint,
            method = request.method,
            bodyPayload = request.payloadJson,
            headers = headersStr,
            createdAt = request.createdAt
        )
    }

    fun remove(dbId: Long) {
        dbQuery.deleteApiQueueById(dbId)
    }

    fun pending(): List<QueuedRequest> {
        return dbQuery.getAllApiQueue().executeAsList().map { entity ->
            val headersMap: Map<String, String> = if (entity.headers.isNullOrBlank()) {
                emptyMap()
            } else {
                runCatching { json.decodeFromString<Map<String, String>>(entity.headers) }.getOrDefault(emptyMap())
            }
            QueuedRequest(
                tag = "", // Not used in new flow
                payloadJson = entity.bodyPayload ?: "",
                endpoint = entity.endpoint,
                method = entity.method,
                headers = headersMap,
                createdAt = entity.createdAt,
                dbId = entity.id
            )
        }
    }

    fun clear() {
        val pending = pending()
        for (item in pending) {
            remove(item.dbId)
        }
    }

    init {
        startAutoFlush()
    }

    private fun startAutoFlush() {
        kotlinx.coroutines.GlobalScope.launch {
            CoreCmp.network.connectivityFlow.collect { online ->
                if (!online) return@collect
                val queue = pending()
                if (queue.isEmpty()) return@collect
                
                // Flush logic
                for (req in queue) {
                    try {
                        val payload = runCatching { json.decodeFromString<ApiQueuePayload>(req.payloadJson) }.getOrNull()
                        if (payload != null) {
                            val formParts = payload.files.map { part ->
                                val decodedBytes = kotlin.io.encoding.Base64.decode(part.bytesBase64)
                                com.corecmp.shared.api.FilePart(
                                    name = part.name,
                                    file = com.corecmp.shared.picker.PickedFile(
                                        bytes = decodedBytes,
                                        fileName = part.fileName,
                                        mimeType = part.mimeType
                                    )
                                )
                            }
                            
                            val client: com.corecmp.shared.api.ApiClient by inject()
                            val bodyObj = if (payload.bodyString != null) {
                                // Since we encoded to string, we can pass the raw string if bodyType is JSON
                                payload.bodyString
                            } else null

                            client.client.request(buildUrl("001", req.endpoint)) {
                                this.method = HttpMethod(req.method)
                                applyDefaults("001")
                                
                                payload.queryParams.forEach { parameter(it.key, it.value) }

                                if (formParts.isNotEmpty()) {
                                    setBody(
                                        MultiPartFormDataContent(
                                            formData {
                                                // append body map if needed, skipped for simplicity
                                                formParts.forEach { part ->
                                                    part.file?.let { file -> appendFile(part.name, file) }
                                                }
                                            }
                                        )
                                    )
                                } else if (bodyObj != null) {
                                    contentType(ContentType.Application.Json)
                                    setBody(bodyObj)
                                }
                            }
                            
                            remove(req.dbId)
                        }
                    } catch (e: Exception) {
                        // Keep in DB if fails
                    }
                }
                
                _flushSignal.emit(Unit)
            }
        }
    }
}
