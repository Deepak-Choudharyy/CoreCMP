package com.corecmp.shared.api

import com.corecmp.shared.picker.PickedFile
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.FormBuilder
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.parameter
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.Parameters
import io.ktor.http.ParametersBuilder
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlin.let
import kotlin.time.Clock
import com.corecmp.shared.CoreCmp
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import com.corecmp.shared.picker.toBase64Image


class ApiClient(val client: HttpClient = HttpClientProvider.client) {
    inline fun <reified Req : Any, reified Res> request(
        base: String = ApiConfig.DEFAULT_BASE_NAME,
        endpoint: String,
        method: ApiMethod = ApiMethod.GET,
        body: Req? = null,
        query: Map<String, String> = emptyMap(),
        files: List<FilePart> = emptyList(),
        bodyType: BodyType = BodyType.JSON,
        options: RequestOptions = RequestOptions()
    ): Flow<Resource<Res>> = flow {
        emit(Resource.Loading())
        emitAll(
            priorityWrapper(
                requestFlow(
                    base,
                    endpoint,
                    method.toKtor(),
                    body,
                    query,
                    files,
                    bodyType,
                    options
                ),
                options.priority
            )
        )
    }

    inline fun <reified Res> get(
        endpoint: String,
        base: String = ApiConfig.DEFAULT_BASE_NAME,
        query: Map<String, String> = emptyMap(),
        options: RequestOptions = RequestOptions()
    ): Flow<Resource<Res>> = request<Unit, Res>(
        base = base,
        endpoint = endpoint,
        method = ApiMethod.GET,
        query = query,
        options = options
    )

    inline fun <reified Req : Any, reified Res> post(
        endpoint: String,
        body: Req? = null,
        base: String = ApiConfig.DEFAULT_BASE_NAME,
        query: Map<String, String> = emptyMap(),
        files: List<FilePart> = emptyList(),
        bodyType: BodyType = BodyType.JSON,
        options: RequestOptions = RequestOptions()
    ): Flow<Resource<Res>> = request<Req, Res>(
        base = base,
        endpoint = endpoint,
        method = ApiMethod.POST,
        body = body,
        query = query,
        files = files,
        bodyType = bodyType,
        options = options
    )

    inline fun <reified Req : Any, reified Res> put(
        endpoint: String,
        body: Req? = null,
        base: String = ApiConfig.DEFAULT_BASE_NAME,
        query: Map<String, String> = emptyMap(),
        files: List<FilePart> = emptyList(),
        bodyType: BodyType = BodyType.JSON,
        options: RequestOptions = RequestOptions()
    ): Flow<Resource<Res>> = request<Req, Res>(
        base = base,
        endpoint = endpoint,
        method = ApiMethod.PUT,
        body = body,
        query = query,
        files = files,
        bodyType = bodyType,
        options = options
    )

    inline fun <reified Res> delete(
        endpoint: String,
        base: String = ApiConfig.DEFAULT_BASE_NAME,
        query: Map<String, String> = emptyMap(),
        options: RequestOptions = RequestOptions()
    ): Flow<Resource<Res>> = request<Unit, Res>(
        base = base,
        endpoint = endpoint,
        method = ApiMethod.DELETE,
        query = query,
        options = options
    )

    inline fun <reified Req : Any, reified Res> patch(
        endpoint: String,
        body: Req? = null,
        base: String = ApiConfig.DEFAULT_BASE_NAME,
        query: Map<String, String> = emptyMap(),
        options: RequestOptions = RequestOptions()
    ): Flow<Resource<Res>> = request<Req, Res>(
        base = base,
        endpoint = endpoint,
        method = ApiMethod.PATCH,
        body = body,
        query = query,
        options = options
    )

    @PublishedApi
    internal inline fun <reified Req : Any, reified Res> requestFlow(
        base: String = ApiConfig.DEFAULT_BASE_NAME,
        endpoint: String,
        method: HttpMethod,
        body: Req?,
        query: Map<String, String>,
        files: List<FilePart>,
        bodyType: BodyType,
        options: RequestOptions
    ): Flow<Resource<Res>> = flow {

        val config = ApiConfig.getConfig(base)

        val url = buildUrl(base, endpoint)

        val mockJson = ApiConfig.getMockResponse(endpoint)
        if (mockJson != null) {
            kotlinx.coroutines.delay(500)
            try {
                val data: Res = json.decodeFromString(mockJson)
                emit(Resource.Success(data))
                return@flow
            } catch (e: Exception) {
                emit(Resource.Error("Mock deserialization failed: ${e.message}"))
                return@flow
            }
        }

        val startTime = Clock.System.now()
        val cacheKey = "${method.value}:$url"
        val mergedBody = mergeRequestBody<Req>(base, body)
        try {
            if (!CoreCmp.network.isOnline) {
                if (options.retryOnConnection) {
                    CoreCmp.network.connectivityFlow
                        .filter { it }
                        .first()
                } else if (options.useCacheFallback) {
                    val cachedText = CoreCmp.apiCache.getResponseBody(cacheKey)
                    if (cachedText != null) {
                        try {
                            val data: Res = json.decodeFromString(cachedText)
                            emit(Resource.Success(data))
                            return@flow
                        } catch (_: Exception) {}
                    }
                    emit(Resource.Error("No internet"))
                    return@flow
                } else if (method in listOf(HttpMethod.Post, HttpMethod.Put, HttpMethod.Patch, HttpMethod.Delete)) {
                    throw Exception("No internet - queueing mutation")
                } else {
                    emit(Resource.Error("No internet"))
                    return@flow
                }
            }

            var capturedReqBody: String? = null

            val response = client.request(buildUrl(base, endpoint)) {
                this.method = method
                applyDefaults(base)
                query.forEach { parameter(it.key, it.value) }

                if (files.isNotEmpty()) {
                    setBody(
                        MultiPartFormDataContent(
                            formData {
                                mergedBody?.let { appendFields(it) }
                                files.forEach { part ->
                                    part.file?.let { file -> appendFile(part.name, file) }
                                }
                            }
                        )
                    )
                } else if (mergedBody != null) {
                    when (bodyType) {
                        BodyType.JSON -> {
                            contentType(ContentType.Application.Json)
                            setBody(mergedBody)
                        }

                        BodyType.FORM_URLENCODED -> {
                            setBody(
                                FormDataContent(
                                    Parameters.build {
                                        appendFields(mergedBody)
                                    }
                                )
                            )
                        }
                        BodyType.FORM_DATA -> {
                            setBody(
                                MultiPartFormDataContent(
                                    formData {
                                        appendFields(mergedBody)
                                    }
                                )
                            )
                        }
                    }
                }

                // Log detailed request
                val reqHeaders = headers.build().entries().associate { it.key to it.value.joinToString(", ") }
                val requestBodyString = if (files.isNotEmpty()) {
                    "Multipart Request (${files.size} files)"
                } else if (mergedBody != null) {
                    try {
                        json.encodeToString(mergedBody)
                    } catch (e: Exception) {
                        mergedBody.toString()
                    }
                } else {
                    null
                }
                capturedReqBody = requestBodyString

                CoreCmpLogger.logApiRequest(
                    url = this.url.build().toString(),
                    method = this.method.value,
                    headers = reqHeaders,
                    body = requestBodyString
                )
            }

            val duration = Clock.System.now() - startTime
            val rawResponse = response.bodyAsText()
            val resHeaders = response.headers.entries().associate { it.key to it.value.joinToString(", ") }
            CoreCmpLogger.logApiResponse(
                url = url,
                statusCode = response.status.value,
                headers = resHeaders,
                durationMs = duration.inWholeMilliseconds,
                rawResponse = rawResponse
            )

            // Cache response text with timestamp, URL, and request JSON
            if (response.status.value in 200..299) {
                runCatching {
                    CoreCmp.apiCache.put(
                        key = cacheKey,
                        url = url,
                        requestBody = capturedReqBody,
                        responseBody = rawResponse
                    )
                }
            }

            val data: Res = json.decodeFromString(rawResponse)
            emit(Resource.Success(data))
        } catch (e: Exception) {
            val duration = Clock.System.now() - startTime

            CoreCmpLogger.logApiError(
                url = url,
                durationMs = duration.inWholeMilliseconds,
                error = e.message
            )

            if (options.useCacheFallback) {
                val cachedText = CoreCmp.apiCache.getResponseBody(cacheKey)
                if (cachedText != null) {
                    try {
                        val data: Res = json.decodeFromString(cachedText)
                        emit(Resource.Success(data))
                        return@flow
                    } catch (_: Exception) {}
                }
            }

            if (method in listOf(HttpMethod.Post, HttpMethod.Put, HttpMethod.Patch, HttpMethod.Delete)) {
                // Encode files
                val queuedFiles = files.mapNotNull { part ->
                    part.file?.let { file ->
                        com.corecmp.shared.network.QueuedFilePart(
                            name = part.name,
                            fileName = file.fileName,
                            mimeType = file.mimeType,
                            bytesBase64 = file.toBase64Image()
                        )
                    }
                }
                
                val bodyString = if (mergedBody != null) {
                    try { json.encodeToString(mergedBody) } catch (e: Exception) { null }
                } else null

                val payload = com.corecmp.shared.network.ApiQueuePayload(
                    bodyString = bodyString,
                    files = queuedFiles,
                    queryParams = query,
                    bodyType = bodyType.name
                )

                CoreCmp.offlineQueue.enqueue(
                    com.corecmp.shared.network.QueuedRequest(
                        tag = "offline_mutation",
                        payloadJson = json.encodeToString(payload),
                        endpoint = endpoint,
                        method = method.value,
                        headers = emptyMap() // Can include reqHeaders if needed
                    )
                )

                emit(Resource.Error("Offline: Request queued for later"))
                return@flow
            }

            emit(Resource.Error(e.message ?: "unknown error"))

        }
    }
    @PublishedApi
    internal fun <T> priorityWrapper(
        upstream: Flow<Resource<T>>,
        priority: ApiPriority
    ): Flow<Resource<T>> = channelFlow {

        ApiDispatcher.dispatch(priority) {

            upstream.collect {

                send(it)

            }

        }

    }
}

fun FormBuilder.appendFile(
    key: String,
    file: PickedFile
) {
    append(
        key,
        file.bytes,
        Headers.build {
            append(
                HttpHeaders.ContentType,
                file.mimeType ?: "application/octet-stream"
            )
            append(
                HttpHeaders.ContentDisposition,
                "filename=\"${file.fileName ?: "file"}\""
            )
        }
    )
}

@PublishedApi
internal inline fun <reified T : Any> FormBuilder.appendFields(obj: T) {

    val json = Json.encodeToString(obj)

    Json.parseToJsonElement(json)
        .jsonObject
        .forEach { (key, value) ->

            append(
                key,
                (value as? JsonPrimitive)
                    ?.content
                    ?: value.toString()
            )
        }
}

@PublishedApi
internal inline fun <reified T : Any> ParametersBuilder.appendFields(obj: T) {

    val json = Json.encodeToString(obj)

    Json.parseToJsonElement(json)
        .jsonObject
        .forEach { (key, value) ->

            append(
                key,
                (value as? JsonPrimitive)
                    ?.content
                    ?: value.toString()
            )
        }
}


enum class ApiMethod {

    GET,
    POST,
    PUT,
    DELETE,
    PATCH

}



fun ApiMethod.toKtor(): HttpMethod {

    return when (this) {

        ApiMethod.GET -> HttpMethod.Get

        ApiMethod.POST -> HttpMethod.Post

        ApiMethod.PUT -> HttpMethod.Put

        ApiMethod.DELETE -> HttpMethod.Delete

        ApiMethod.PATCH -> HttpMethod.Patch

    }

}