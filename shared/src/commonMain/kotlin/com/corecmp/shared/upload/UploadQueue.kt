package com.corecmp.shared.upload

import com.corecmp.shared.api.Resource
import com.corecmp.shared.picker.PickedFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

enum class UploadPriority { HIGH, NORMAL, LOW }

data class QueuedUpload(
    val id: String,
    val base: String,
    val endpoint: String,
    val file: PickedFile,
    val priority: UploadPriority = UploadPriority.NORMAL,
    val fieldName: String = "file",
    val extraFields: Map<String, String> = emptyMap(),
)

class UploadQueueManager(
    private val uploadManager: UploadManager = UploadManager(),
) {
    private val queue = mutableListOf<QueuedUpload>()
    private var idCounter = 0

    fun enqueue(item: QueuedUpload): String {
        queue += item
        queue.sortBy { it.priority.ordinal }
        return item.id
    }

    fun enqueue(
        base: String,
        endpoint: String,
        file: PickedFile,
        priority: UploadPriority = UploadPriority.NORMAL,
    ): String {
        val id = "q_${++idCounter}"
        return enqueue(
            QueuedUpload(
                id = id,
                base = base,
                endpoint = endpoint,
                file = file,
                priority = priority,
            ),
        )
    }

    fun processAll(
        onProgress: ((String, UploadProgress) -> Unit)? = null,
    ): Flow<Resource<Pair<String, UploadResponse>>> = flow {
        val items = queue.toList()
        queue.clear()
        for (item in items) {
            uploadManager.upload(
                base = item.base,
                endpoint = item.endpoint,
                file = item.file,
                fieldName = item.fieldName,
                extraFields = item.extraFields,
                onProgress = { progress -> onProgress?.invoke(item.id, progress) },
            ).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val data = result.data
                        if (data != null) emit(Resource.Success(item.id to data))
                    }
                    is Resource.Error -> emit(Resource.Error(result.message ?: "Upload failed"))
                    is Resource.Loading -> emit(Resource.Loading())
                }
            }
        }
    }

    fun pendingCount(): Int = queue.size
}
