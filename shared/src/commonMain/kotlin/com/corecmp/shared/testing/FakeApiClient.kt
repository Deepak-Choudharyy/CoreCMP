package com.corecmp.shared.testing

import com.corecmp.shared.api.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/**
 * In-memory API stub for unit tests — no real network calls.
 */
class FakeApiClient(
    private val responses: Map<String, String> = emptyMap(),
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    fun <T> get(
        endpoint: String,
        serializer: KSerializer<T>,
        delayMs: Long = 0,
    ): Flow<Resource<T>> = respond(endpoint, serializer, delayMs)

    fun <T> respond(
        endpoint: String,
        serializer: KSerializer<T>,
        delayMs: Long = 0,
    ): Flow<Resource<T>> = flow {
        emit(Resource.Loading())
        if (delayMs > 0) kotlinx.coroutines.delay(delayMs)
        val raw = responses[endpoint]
        if (raw == null) {
            emit(Resource.Error("No mock for $endpoint"))
            return@flow
        }
        runCatching { json.decodeFromString(serializer, raw) }
            .onSuccess { emit(Resource.Success(it)) }
            .onFailure { emit(Resource.Error("Mock parse failed: ${it.message}")) }
    }

    fun respondRaw(endpoint: String, delayMs: Long = 0): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        if (delayMs > 0) kotlinx.coroutines.delay(delayMs)
        val raw = responses[endpoint]
        if (raw == null) emit(Resource.Error("No mock for $endpoint"))
        else emit(Resource.Success(raw))
    }

    fun stub(endpoint: String, jsonBody: String): FakeApiClient =
        FakeApiClient(responses + (endpoint to jsonBody), json)
}
