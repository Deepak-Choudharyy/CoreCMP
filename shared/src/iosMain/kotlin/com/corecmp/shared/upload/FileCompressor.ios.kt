package com.corecmp.shared.upload

import com.corecmp.shared.picker.PickedFile
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.posix.memcpy
import kotlin.math.roundToInt

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual class FileCompressor actual constructor() {

    actual suspend fun compress(
        file: PickedFile,
        config: CompressionConfig,
    ): CompressionResult = withContext(Dispatchers.Default) {
        val originalSize = file.bytes.size
        if (!file.isImage) {
            return@withContext passThrough(file, originalSize)
        }

        val image = file.bytes.toUIImage() ?: return@withContext passThrough(file, originalSize)
        val scaled = scaleImage(image, config.maxWidth)

        var quality = (config.quality / 100.0).coerceIn(0.1, 1.0)
        var output = UIImageJPEGRepresentation(scaled, quality)?.toByteArray() ?: file.bytes
        val target = config.resolvedTargetBytes

        if (target != null) {
            val minQuality = (config.minQuality / 100.0).coerceIn(0.1, 1.0)
            while (output.size > target && quality > minQuality) {
                quality = (quality - 0.1).coerceAtLeast(minQuality)
                output = UIImageJPEGRepresentation(scaled, quality)?.toByteArray() ?: output
            }
        }

        val compressedFile = PickedFile(
            bytes = output,
            fileName = file.fileName?.replaceAfterLast('.', "jpeg"),
            mimeType = "image/jpeg",
        )

        CompressionResult(
            file = compressedFile,
            originalSizeBytes = originalSize,
            compressedSizeBytes = output.size,
            wasCompressed = output.size < originalSize,
            qualityUsed = (quality * 100).roundToInt(),
        )
    }

    private fun passThrough(file: PickedFile, size: Int): CompressionResult =
        CompressionResult(
            file = file,
            originalSizeBytes = size,
            compressedSizeBytes = size,
            wasCompressed = false,
        )

    private fun scaleImage(image: UIImage, maxWidth: Int): UIImage {
        val width = image.size.useContents { width }
        if (width <= maxWidth) return image

        val height = image.size.useContents { height }
        val ratio = maxWidth.toDouble() / width
        val newHeight = (height * ratio).roundToInt().coerceAtLeast(1)

        UIGraphicsBeginImageContextWithOptions(
            CGSizeMake(maxWidth.toDouble(), newHeight.toDouble()),
            false,
            1.0,
        )
        image.drawInRect(CGRectMake(0.0, 0.0, maxWidth.toDouble(), newHeight.toDouble()))
        val scaled = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        return scaled ?: image
    }

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    private fun ByteArray.toUIImage(): UIImage? =
        usePinned { pinned ->
            val data = NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
            UIImage.imageWithData(data)
        }

    @OptIn(ExperimentalForeignApi::class)
    private fun NSData.toByteArray(): ByteArray {
        val size = length.toInt()
        val bytes = ByteArray(size)
        if (size > 0) {
            bytes.usePinned {
                memcpy(it.addressOf(0), this@toByteArray.bytes, size.convert())
            }
        }
        return bytes
    }
}
