package com.corecmp.shared.platform

/**
 * Generates a PNG byte array for the given [text], or null if generation fails.
 */
expect fun generateQrPngBytes(text: String, sizePx: Int = 512): ByteArray?

expect class QrGenerator() {
    fun generatePng(text: String, sizePx: Int = 512): ByteArray?
}
