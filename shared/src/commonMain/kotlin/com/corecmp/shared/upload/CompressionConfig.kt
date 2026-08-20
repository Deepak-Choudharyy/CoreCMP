package com.corecmp.shared.upload

enum class CompressionFormat {
    JPEG,
    WEBP,
    PNG,
}

enum class TargetSizePreset(val maxBytes: Int) {
    MB_2(2 * 1024 * 1024),
    MB_4(4 * 1024 * 1024),
    MB_10(10 * 1024 * 1024),
}

enum class MaxWidthPreset(val pixels: Int) {
    W_1920(1920),
    W_1280(1280),
    W_800(800),
}

data class CompressionConfig(
    val quality: Int = 85,
    val maxWidth: Int = MaxWidthPreset.W_1920.pixels,
    val targetSizeBytes: Int? = null,
    val targetSizePreset: TargetSizePreset? = TargetSizePreset.MB_2,
    val format: CompressionFormat = CompressionFormat.JPEG,
    val minQuality: Int = 40,
) {
    val resolvedTargetBytes: Int?
        get() = targetSizeBytes ?: targetSizePreset?.maxBytes
}
