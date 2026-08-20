package com.corecmp.shared.upload

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.corecmp.shared.picker.PickedFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

actual class FileCompressor actual constructor() {

    actual suspend fun compress(
        file: PickedFile,
        config: CompressionConfig,
    ): CompressionResult = withContext(Dispatchers.Default) {
        val originalSize = file.bytes.size
        if (!file.isImage) {
            return@withContext passThrough(file, originalSize)
        }

        val decoded = BitmapFactory.decodeByteArray(file.bytes, 0, file.bytes.size)
            ?: return@withContext passThrough(file, originalSize)

        val scaled = scaleBitmap(decoded, config.maxWidth)
        if (scaled !== decoded) decoded.recycle()

        val compressFormat = when (config.format) {
            CompressionFormat.JPEG -> Bitmap.CompressFormat.JPEG
            CompressionFormat.WEBP -> Bitmap.CompressFormat.WEBP
            CompressionFormat.PNG -> Bitmap.CompressFormat.PNG
        }

        val mimeType = when (config.format) {
            CompressionFormat.JPEG -> "image/jpeg"
            CompressionFormat.WEBP -> "image/webp"
            CompressionFormat.PNG -> "image/png"
        }

        var quality = config.quality.coerceIn(1, 100)
        var output = compressToBytes(scaled, compressFormat, quality)
        val target = config.resolvedTargetBytes

        if (target != null) {
            while (output.size > target && quality > config.minQuality) {
                quality = (quality - 10).coerceAtLeast(config.minQuality)
                output = compressToBytes(scaled, compressFormat, quality)
            }
        }

        scaled.recycle()

        val compressedFile = PickedFile(
            bytes = output,
            fileName = file.fileName?.replaceAfterLast('.', config.format.name.lowercase()),
            mimeType = mimeType,
        )

        CompressionResult(
            file = compressedFile,
            originalSizeBytes = originalSize,
            compressedSizeBytes = output.size,
            wasCompressed = output.size < originalSize,
            qualityUsed = quality,
        )
    }

    private fun passThrough(file: PickedFile, size: Int): CompressionResult =
        CompressionResult(
            file = file,
            originalSizeBytes = size,
            compressedSizeBytes = size,
            wasCompressed = false,
        )

    private fun scaleBitmap(source: Bitmap, maxWidth: Int): Bitmap {
        if (source.width <= maxWidth) return source
        val ratio = maxWidth.toFloat() / source.width.toFloat()
        val height = (source.height * ratio).roundToInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(source, maxWidth, height, true)
    }

    private fun compressToBytes(
        bitmap: Bitmap,
        format: Bitmap.CompressFormat,
        quality: Int,
    ): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(format, quality, stream)
        return stream.toByteArray()
    }
}
