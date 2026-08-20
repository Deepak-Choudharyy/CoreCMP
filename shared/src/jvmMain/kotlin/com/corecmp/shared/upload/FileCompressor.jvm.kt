package com.corecmp.shared.upload

import com.corecmp.shared.picker.PickedFile

actual class FileCompressor actual constructor() {

    actual suspend fun compress(
        file: PickedFile,
        config: CompressionConfig,
    ): CompressionResult {
        val size = file.bytes.size
        return CompressionResult(
            file = file,
            originalSizeBytes = size,
            compressedSizeBytes = size,
            wasCompressed = false,
        )
    }
}
