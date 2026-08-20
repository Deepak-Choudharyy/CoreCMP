package com.corecmp.shared.upload

data class ChunkedUploadConfig(
    val chunkSizeBytes: Int = 512 * 1024,
    val parallelChunks: Int = 3,
    val maxRetriesPerChunk: Int = 2,
    val uploadIdField: String = "uploadId",
    val chunkIndexField: String = "chunkIndex",
    val totalChunksField: String = "totalChunks",
    val fileField: String = "file",
)
