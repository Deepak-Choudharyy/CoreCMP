package com.corecmp.shared.upload

import com.corecmp.shared.CoreCmp
import com.corecmp.shared.api.ApiClient
import com.corecmp.shared.api.Resource
import com.corecmp.shared.api.appendFile
import com.corecmp.shared.api.buildUrl
import com.corecmp.shared.api.applyDefaults
import com.corecmp.shared.picker.PickedFile
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlin.random.Random
import kotlin.time.Clock

data class UploadValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null,
)

@Serializable
data class UploadResponse(
    val success: Boolean = true,
    val message: String? = null,
)

class UploadManager(
    private val apiClient: ApiClient = ApiClient(),
    private val compressor: FileCompressor = FileCompressor(),
    var wifiOnly: Boolean = false,
    val allowedMimeTypes: Set<String> = emptySet(),
    val maxSizeBytes: Int = TargetSizePreset.MB_10.maxBytes,
) {
    suspend fun compress(
        file: PickedFile,
        config: CompressionConfig = CompressionConfig(),
    ): CompressionResult = compressor.compress(file, config)

    fun validateBeforeUpload(file: PickedFile): UploadValidationResult {
        if (file.bytes.isEmpty()) {
            return UploadValidationResult(false, "File is empty")
        }
        if (file.bytes.size > maxSizeBytes) {
            val maxMb = maxSizeBytes / (1024 * 1024)
            return UploadValidationResult(false, "File exceeds ${maxMb}MB limit")
        }
        val mime = file.mimeType
        if (allowedMimeTypes.isNotEmpty() && mime != null && mime !in allowedMimeTypes) {
            return UploadValidationResult(false, "File type $mime is not allowed")
        }
        return UploadValidationResult(true)
    }

    fun upload(
        base: String,
        endpoint: String,
        file: PickedFile,
        fieldName: String = "file",
        extraFields: Map<String, String> = emptyMap(),
        onProgress: ((UploadProgress) -> Unit)? = null,
    ): Flow<Resource<UploadResponse>> = flow {
        if (!canUpload()) {
            emit(Resource.Error("Upload blocked: WiFi-only mode is enabled"))
            return@flow
        }

        val validation = validateBeforeUpload(file)
        if (!validation.isValid) {
            emit(Resource.Error(validation.errorMessage ?: "Validation failed"))
            return@flow
        }

        emit(Resource.Loading())

        if (!CoreCmp.network.isOnline) {
            emit(Resource.Error("No internet connection"))
            return@flow
        }

        val url = buildUrl(base, endpoint)
        val totalBytes = file.bytes.size.toLong()
        var lastProgressAt = Clock.System.now()
        var lastBytesSent = 0L

        try {
            val response = apiClient.client.request(url) {
                method = HttpMethod.Post
                applyDefaults(base)
                onUpload { bytesSent, _ ->
                    val now = Clock.System.now()
                    val elapsedMs = (now - lastProgressAt).inWholeMilliseconds.coerceAtLeast(1)
                    val delta = bytesSent - lastBytesSent
                    val speed = delta.toFloat() / elapsedMs * 1000f
                    lastBytesSent = bytesSent
                    lastProgressAt = now
                    onProgress?.invoke(
                        UploadProgress(
                            bytesSent = bytesSent,
                            totalBytes = totalBytes,
                        ).withSpeed(speed),
                    )
                }
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            extraFields.forEach { (key, value) -> append(key, value) }
                            appendFile(fieldName, file)
                        },
                    ),
                )
            }

            val body = response.bodyAsTextOrEmpty()
            emit(
                Resource.Success(
                    UploadResponse(
                        success = response.status.value in 200..299,
                        message = body.ifBlank { null },
                    ),
                ),
            )
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Upload failed"))
        }
    }

    fun uploadChunked(
        base: String,
        endpoint: String,
        file: PickedFile,
        config: ChunkedUploadConfig = ChunkedUploadConfig(),
        uploadId: String = generateUploadId(),
        extraFields: Map<String, String> = emptyMap(),
        onProgress: ((UploadProgress) -> Unit)? = null,
    ): Flow<Resource<UploadResponse>> = flow {
        if (!canUpload()) {
            emit(Resource.Error("Upload blocked: WiFi-only mode is enabled"))
            return@flow
        }

        val validation = validateBeforeUpload(file)
        if (!validation.isValid) {
            emit(Resource.Error(validation.errorMessage ?: "Validation failed"))
            return@flow
        }

        emit(Resource.Loading())

        if (!CoreCmp.network.isOnline) {
            emit(Resource.Error("No internet connection"))
            return@flow
        }

        val chunks = file.bytes
            .toList()
            .chunked(config.chunkSizeBytes)
            .map { it.toByteArray() }
            .ifEmpty { listOf(file.bytes) }
        val totalChunks = chunks.size
        val totalBytes = file.bytes.size.toLong()
        var uploadedBytes = 0L
        val url = buildUrl(base, endpoint)

        try {
            coroutineScope {
                chunks.withIndex().chunked(config.parallelChunks.coerceAtLeast(1)).forEach { batch ->
                    val jobs = batch.map { (chunkIndex, chunkBytes) ->
                        async {
                            uploadChunkWithRetry(
                                url = url,
                                base = base,
                                config = config,
                                uploadId = uploadId,
                                chunkIndex = chunkIndex,
                                totalChunks = totalChunks,
                                chunkBytes = chunkBytes,
                                fileName = file.fileName ?: "chunk",
                                mimeType = file.mimeType ?: "application/octet-stream",
                                extraFields = extraFields,
                                maxRetries = config.maxRetriesPerChunk,
                            )
                            chunkIndex to chunkBytes.size
                        }
                    }
                    jobs.awaitAll().forEach { (chunkIndex, chunkSize) ->
                        uploadedBytes += chunkSize
                        onProgress?.invoke(
                            UploadProgress(
                                bytesSent = uploadedBytes,
                                totalBytes = totalBytes,
                                chunkIndex = chunkIndex,
                                totalChunks = totalChunks,
                            ),
                        )
                    }
                }
            }

            emit(Resource.Success(UploadResponse(success = true, message = "Chunked upload complete")))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Chunked upload failed"))
        }
    }

    private suspend fun uploadChunkWithRetry(
        url: String,
        base: String,
        config: ChunkedUploadConfig,
        uploadId: String,
        chunkIndex: Int,
        totalChunks: Int,
        chunkBytes: ByteArray,
        fileName: String,
        mimeType: String,
        extraFields: Map<String, String>,
        maxRetries: Int,
    ) {
        var attempt = 0
        var lastError: Throwable? = null
        while (attempt <= maxRetries) {
            try {
                val response = apiClient.client.request(url) {
                    method = HttpMethod.Post
                    applyDefaults(base)
                    setBody(
                        MultiPartFormDataContent(
                            formData {
                                extraFields.forEach { (key, value) -> append(key, value) }
                                append(config.uploadIdField, uploadId)
                                append(config.chunkIndexField, chunkIndex.toString())
                                append(config.totalChunksField, totalChunks.toString())
                                append(
                                    config.fileField,
                                    chunkBytes,
                                    io.ktor.http.Headers.build {
                                        append(io.ktor.http.HttpHeaders.ContentType, mimeType)
                                        append(
                                            io.ktor.http.HttpHeaders.ContentDisposition,
                                            "filename=\"${fileName}_part$chunkIndex\"",
                                        )
                                    },
                                )
                            },
                        ),
                    )
                }
                if (response.status.value in 200..299) return
                lastError = IllegalStateException("Chunk $chunkIndex failed with status ${response.status.value}")
            } catch (e: Exception) {
                lastError = e
            }
            attempt++
        }
        throw lastError ?: IllegalStateException("Chunk $chunkIndex upload failed")
    }

    internal fun canUpload(): Boolean {
        if (!wifiOnly) return true
        return isOnWifi()
    }

    private fun generateUploadId(): String =
        "upload_${Clock.System.now().toEpochMilliseconds()}_${Random.nextInt(1000, 9999)}"

    /** Pick -> compress -> upload in one call. */
    fun pickCompressUpload(
        base: String,
        endpoint: String,
        file: PickedFile,
        compression: CompressionConfig = CompressionConfig(),
        fieldName: String = "file",
        extraFields: Map<String, String> = emptyMap(),
        onProgress: ((UploadProgress) -> Unit)? = null,
    ): Flow<Resource<UploadResponse>> = flow {
        val compressed = compress(file, compression)
        emitAll(
            upload(
                base = base,
                endpoint = endpoint,
                file = compressed.file,
                fieldName = fieldName,
                extraFields = extraFields,
                onProgress = onProgress,
            ),
        )
    }
}

private suspend fun io.ktor.client.statement.HttpResponse.bodyAsTextOrEmpty(): String =
    runCatching { this.bodyAsText() }.getOrDefault("")
