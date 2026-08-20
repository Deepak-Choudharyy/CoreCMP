package com.corecmp.shared.upload

import com.corecmp.shared.api.HttpClientProvider
import com.corecmp.shared.api.Resource
import com.corecmp.shared.picker.PickedFile
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Upload directly to S3/GCS using a pre-signed PUT URL returned by your backend.
 */
fun UploadManager.uploadPreSigned(
    preSignedUrl: String,
    file: PickedFile,
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
    val totalBytes = file.bytes.size.toLong()
    try {
        val response = HttpClientProvider.client.put(preSignedUrl) {
            contentType(file.mimeType?.let { ContentType.parse(it) } ?: ContentType.Application.OctetStream)
            onUpload { sent, _ ->
                onProgress?.invoke(UploadProgress(bytesSent = sent, totalBytes = totalBytes))
            }
            setBody(file.bytes)
        }
        emit(
            Resource.Success(
                UploadResponse(
                    success = response.status.value in 200..299,
                    message = "Pre-signed upload complete",
                ),
            ),
        )
    } catch (e: Exception) {
        emit(Resource.Error(e.message ?: "Pre-signed upload failed"))
    }
}
