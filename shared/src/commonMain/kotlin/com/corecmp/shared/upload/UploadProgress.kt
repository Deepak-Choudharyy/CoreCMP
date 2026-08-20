package com.corecmp.shared.upload

data class UploadProgress(
    val bytesSent: Long,
    val totalBytes: Long,
    val chunkIndex: Int = 0,
    val totalChunks: Int = 1,
    val speedKbps: Float = 0f,
) {
    val percent: Float
        get() = if (totalBytes <= 0L) 0f else (bytesSent.toFloat() / totalBytes * 100f).coerceIn(0f, 100f)

    fun withSpeed(bytesPerSecond: Float): UploadProgress =
        copy(speedKbps = (bytesPerSecond / 1024f).coerceAtLeast(0f))
}
