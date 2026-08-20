package com.corecmp.shared.upload

import com.corecmp.shared.picker.PickedFile

expect class FileCompressor() {
    suspend fun compress(file: PickedFile, config: CompressionConfig = CompressionConfig()): CompressionResult
}
